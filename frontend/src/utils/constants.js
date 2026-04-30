export const API_BASE_URL = process.env.REACT_APP_API_URL || '/api';

export const PAGE_SIZE = 10;

export const CUSTOMER_FIELDS = {
  NAME: 'name',
  DATE_OF_BIRTH: 'dateOfBirth',
  NIC_NUMBER: 'nicNumber',
};

export const BULK_STATUS = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
};

export const EXCEL_COLUMNS = ['Name', 'Date of Birth (yyyy-MM-dd)', 'NIC Number', 'Mobile Number (optional)'];
