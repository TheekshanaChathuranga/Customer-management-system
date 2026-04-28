package com.example.backend.service;

import com.example.backend.model.BulkUploadJob;
import com.example.backend.repository.BulkUploadJobRepository;
import com.example.backend.repository.CustomerRepository;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class BulkUploadService {

    private static final Logger log = LoggerFactory.getLogger(BulkUploadService.class);
    private static final int BATCH_SIZE = 500;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BulkUploadJobRepository jobRepository;
    private final CustomerRepository customerRepository;
    private final JdbcTemplate jdbcTemplate;

    public BulkUploadService(BulkUploadJobRepository jobRepository,
                             CustomerRepository customerRepository,
                             JdbcTemplate jdbcTemplate) {
        this.jobRepository = jobRepository;
        this.customerRepository = customerRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public BulkUploadJob createJob(String fileName) {
        BulkUploadJob job = new BulkUploadJob();
        job.setJobId(UUID.randomUUID().toString());
        job.setFileName(fileName);
        job.setStatus(BulkUploadJob.Status.PENDING);
        return jobRepository.save(job);
    }

    public BulkUploadJob getJobStatus(String jobId) {
        return jobRepository.findByJobId(jobId).orElse(null);
    }

    @Async("bulkUploadExecutor")
    public void processFileAsync(String jobId, File file) {
        BulkUploadJob job = jobRepository.findByJobId(jobId).orElse(null);
        if (job == null) return;

        job.setStatus(BulkUploadJob.Status.PROCESSING);
        jobRepository.save(job);

        StringBuilder errorLog = new StringBuilder();
        int totalProcessed = 0;
        int successCount = 0;
        int failCount = 0;

        try (FileInputStream fis = new FileInputStream(file);
             OPCPackage opcPackage = OPCPackage.open(fis)) {

            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opcPackage);
            XSSFReader xssfReader = new XSSFReader(opcPackage);
            StylesTable styles = xssfReader.getStylesTable();

            // Collect rows using SAX streaming
            List<String[]> allRows = new ArrayList<>();
            RowCollector collector = new RowCollector(allRows);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();

            ContentHandler handler = new XSSFSheetXMLHandler(styles, strings, collector, false);
            xmlReader.setContentHandler(handler);

            // Process first sheet
            Iterator<InputStream> sheetsData = xssfReader.getSheetsData();
            if (sheetsData.hasNext()) {
                try (InputStream sheetStream = sheetsData.next()) {
                    xmlReader.parse(new InputSource(sheetStream));
                }
            }

            int totalRows = allRows.size();
            job.setTotalRecords(totalRows);
            jobRepository.save(job);

            // Pre-load existing NICs for duplicate checking
            Set<String> existingNics = new HashSet<>();

            List<String[]> currentBatch = new ArrayList<>();
            for (int i = 0; i < totalRows; i++) {
                currentBatch.add(allRows.get(i));

                if (currentBatch.size() >= BATCH_SIZE || i == totalRows - 1) {
                    // Check NICs in this batch
                    List<String> batchNics = new ArrayList<>();
                    for (String[] row : currentBatch) {
                        if (row.length >= 3 && row[2] != null) {
                            batchNics.add(row[2].trim());
                        }
                    }
                    if (!batchNics.isEmpty()) {
                        List<String> dbExisting = customerRepository.findExistingNicNumbers(batchNics);
                        existingNics.addAll(dbExisting);
                    }

                    int[] result = processBatch(currentBatch, existingNics, errorLog, totalProcessed);
                    successCount += result[0];
                    failCount += result[1];
                    totalProcessed += currentBatch.size();

                    // Update progress
                    job.setProcessed(totalProcessed);
                    job.setSuccessCount(successCount);
                    job.setFailCount(failCount);
                    if (errorLog.length() > 0) {
                        job.setErrorLog(errorLog.length() > 60000 ?
                                errorLog.substring(0, 60000) + "\n...truncated" : errorLog.toString());
                    }
                    jobRepository.save(job);
                    currentBatch.clear();
                }
            }

            job.setStatus(BulkUploadJob.Status.COMPLETED);

        } catch (Exception e) {
            log.error("Bulk upload failed for job: {}", jobId, e);
            job.setStatus(BulkUploadJob.Status.FAILED);
            errorLog.append("Fatal error: ").append(e.getMessage());
            job.setErrorLog(errorLog.toString());
        } finally {
            jobRepository.save(job);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private int[] processBatch(List<String[]> rows, Set<String> existingNics,
                               StringBuilder errorLog, int rowOffset) {
        int success = 0;
        int fail = 0;

        String sql = "INSERT INTO customer (name, date_of_birth, nic_number, created_at, updated_at) " +
                     "VALUES (?, ?, ?, NOW(), NOW())";

        List<Object[]> validRows = new ArrayList<>();
        List<String[]> validOriginalRows = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            int rowNum = rowOffset + i + 2; // +2 for header and 1-indexing

            try {
                if (row.length < 3) {
                    errorLog.append("Row ").append(rowNum).append(": Insufficient columns\n");
                    fail++;
                    continue;
                }

                String name = row[0] != null ? row[0].trim() : "";
                String dobStr = row[1] != null ? row[1].trim() : "";
                String nic = row[2] != null ? row[2].trim() : "";

                if (name.isEmpty()) {
                    errorLog.append("Row ").append(rowNum).append(": Name is empty\n");
                    fail++;
                    continue;
                }
                if (nic.isEmpty()) {
                    errorLog.append("Row ").append(rowNum).append(": NIC is empty\n");
                    fail++;
                    continue;
                }
                if (existingNics.contains(nic)) {
                    errorLog.append("Row ").append(rowNum).append(": Duplicate NIC ").append(nic).append("\n");
                    fail++;
                    continue;
                }

                LocalDate dob = parseDate(dobStr);
                if (dob == null) {
                    errorLog.append("Row ").append(rowNum).append(": Invalid date '").append(dobStr).append("'\n");
                    fail++;
                    continue;
                }

                validRows.add(new Object[]{name, Date.valueOf(dob), nic});
                validOriginalRows.add(row);
                existingNics.add(nic);
                success++;

            } catch (Exception e) {
                errorLog.append("Row ").append(rowNum).append(": ").append(e.getMessage()).append("\n");
                fail++;
            }
        }

        // JDBC batch insert
        if (!validRows.isEmpty()) {
            try {
                jdbcTemplate.batchUpdate(sql, validRows);
            } catch (Exception e) {
                log.error("Batch insert failed", e);
                errorLog.append("Batch insert error: ").append(e.getMessage()).append("\n");
                fail += validRows.size();
                success -= validRows.size();
            }
        }

        // Process mobile numbers (column 3, optional)
        String mobileSql = "INSERT INTO customer_mobile (customer_id, mobile_number, created_at) " +
                           "SELECT c.id, ?, NOW() FROM customer c WHERE c.nic_number = ?";
        for (String[] row : validOriginalRows) {
            if (row.length > 3 && row[3] != null && !row[3].trim().isEmpty()) {
                String nic = row[2] != null ? row[2].trim() : "";
                try {
                    jdbcTemplate.update(mobileSql, row[3].trim(), nic);
                } catch (Exception e) {
                    log.warn("Failed to add mobile for NIC {}: {}", nic, e.getMessage());
                }
            }
        }

        return new int[]{success, fail};
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        dateStr = dateStr.trim();

        try { return LocalDate.parse(dateStr, DATE_FMT); } catch (DateTimeParseException ignored) {}
        try { return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")); } catch (DateTimeParseException ignored) {}
        try { return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MM/dd/yyyy")); } catch (DateTimeParseException ignored) {}

        // Excel numeric date
        try {
            double numericDate = Double.parseDouble(dateStr);
            long days = (long) numericDate - 2;
            return LocalDate.of(1900, 1, 1).plusDays(days);
        } catch (NumberFormatException ignored) {}

        return null;
    }

    /**
     * SAX handler that collects all data rows (skipping header row).
     */
    private static class RowCollector implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final List<String[]> allRows;
        private List<String> currentRow;
        private boolean isFirstRow = true;

        RowCollector(List<String[]> allRows) {
            this.allRows = allRows;
        }

        @Override
        public void startRow(int rowNum) {
            currentRow = new ArrayList<>();
        }

        @Override
        public void endRow(int rowNum) {
            if (isFirstRow) {
                isFirstRow = false;
                return;
            }
            if (currentRow != null && !currentRow.isEmpty()) {
                allRows.add(currentRow.toArray(new String[0]));
            }
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            if (currentRow != null) {
                currentRow.add(formattedValue);
            }
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
        }
    }
}
