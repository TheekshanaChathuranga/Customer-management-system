# Customer Management System

A full-stack customer management application built with **Spring Boot** (Java 8) and **React JS**.

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend | Spring Boot | 2.7.18 |
| Language | Java | 8 |
| Frontend | React JS | 19.x |
| Database | MariaDB | 10.6+ |
| HTTP Client | Axios | 1.7.x |
| Build Tool | Maven | 3.x |
| Testing | JUnit 5 + Mockito | - |
| Excel Processing | Apache POI | 5.2.5 |

## Features

- **Create / Update / View / Delete** customers
- **Table view** with search and pagination
- **Multiple mobile numbers** per customer
- **Multiple addresses** with City/Country master data
- **Family member** linking (self-referencing customers)
- **Bulk upload** via Excel (.xlsx) — handles up to 1,000,000 records
  - Async processing with progress tracking
  - SAX-based streaming for minimal memory usage
  - JDBC batch inserts for performance
  - Duplicate NIC detection

## Prerequisites

- Java 8 (JDK)
- Node.js 16+ & npm
- MariaDB 10.6+
- Maven 3.x

## Database Setup

1. Start MariaDB on port **3300**
2. Run the DDL script to create tables:

```bash
mysql -u root -p1234 --port=3300 < database/DDL.sql
```

3. Run the DML script to load master data:

```bash
mysql -u root -p1234 --port=3300 < database/DML.sql
```

## Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The API will start at `http://localhost:8080`

## Frontend Setup

```bash
cd frontend
npm install
npm start
```

The app will open at `http://localhost:3000`

## API Endpoints

### Customers

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/customers` | List customers (paginated) |
| GET | `/api/customers/{id}` | Get customer details |
| POST | `/api/customers` | Create customer |
| PUT | `/api/customers/{id}` | Update customer |
| DELETE | `/api/customers/{id}` | Delete customer |

Query params for GET list: `page`, `size`, `search`, `sortBy`, `sortDir`

### Bulk Upload

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bulk-upload` | Upload Excel file |
| GET | `/api/bulk-upload/{jobId}` | Check upload status |

### Master Data

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/master/countries` | List all countries |
| GET | `/api/master/cities?countryId={id}` | List cities by country |

## Bulk Upload Excel Format

| Column A | Column B | Column C | Column D |
|----------|----------|----------|----------|
| Name (required) | Date of Birth (required, yyyy-MM-dd) | NIC Number (required, unique) | Mobile (optional) |

## Project Structure

```
customer-management/
├── README.md
├── database/
│   ├── DDL.sql
│   └── DML.sql
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/backend/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── model/
│       │   ├── dto/
│       │   ├── exception/
│       │   └── config/
│       └── test/
└── frontend/
    ├── package.json
    └── src/
        ├── components/
        ├── pages/
        ├── services/
        └── utils/
```

## Running Tests

```bash
cd backend
mvn test
```

## Configuration

Backend config in `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3300/customer_db
spring.datasource.username=root
spring.datasource.password=1234
```
