-- ============================================================
-- Customer Management System - DDL Script
-- Database: MariaDB 10.6+
-- Run this first before DML.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS customer_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE customer_db;

-- ------------------------------------------------------------
-- Master Data: Countries
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS country (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)  NOT NULL,
    code        VARCHAR(10)   NOT NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_country_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Master Data: Cities
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS city (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)  NOT NULL,
    country_id  BIGINT        NOT NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_city_country FOREIGN KEY (country_id) REFERENCES country(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Core: Customer
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(200)  NOT NULL,
    date_of_birth DATE          NOT NULL,
    nic_number    VARCHAR(20)   NOT NULL,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_customer_nic (nic_number),
    INDEX idx_customer_name (name),
    INDEX idx_customer_nic  (nic_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Customer Mobile Numbers (multiple per customer)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_mobile (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    customer_id  BIGINT       NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_mobile_customer (customer_id),
    CONSTRAINT fk_mobile_customer FOREIGN KEY (customer_id)
        REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Customer Addresses (multiple per customer)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_address (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    customer_id    BIGINT       NOT NULL,
    address_line1  VARCHAR(255) NOT NULL,
    address_line2  VARCHAR(255),
    city_id        BIGINT       NOT NULL,
    country_id     BIGINT       NOT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_addr_customer (customer_id),
    CONSTRAINT fk_addr_customer FOREIGN KEY (customer_id)
        REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_addr_city    FOREIGN KEY (city_id)    REFERENCES city(id),
    CONSTRAINT fk_addr_country FOREIGN KEY (country_id) REFERENCES country(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Family Members (self-referencing customer relationship)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_family (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    customer_id     BIGINT   NOT NULL,
    family_member_id BIGINT  NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_family_pair (customer_id, family_member_id),
    INDEX idx_family_customer (customer_id),
    INDEX idx_family_member   (family_member_id),
    CONSTRAINT fk_family_customer FOREIGN KEY (customer_id)
        REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_family_member FOREIGN KEY (family_member_id)
        REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Bulk Upload Job Tracking
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bulk_upload_job (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    job_id          VARCHAR(50)   NOT NULL,
    file_name       VARCHAR(255)  NOT NULL,
    total_records   INT           NOT NULL DEFAULT 0,
    processed       INT           NOT NULL DEFAULT 0,
    success_count   INT           NOT NULL DEFAULT 0,
    fail_count      INT           NOT NULL DEFAULT 0,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    error_log       TEXT,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_job_id (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Spring Batch meta tables (required for Spring Batch)
CREATE TABLE IF NOT EXISTS BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID BIGINT NOT NULL PRIMARY KEY,
    VERSION BIGINT,
    JOB_NAME VARCHAR(100) NOT NULL,
    JOB_KEY VARCHAR(32) NOT NULL,
    UNIQUE KEY JOB_INST_UN (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    VERSION BIGINT,
    JOB_INSTANCE_ID BIGINT NOT NULL,
    CREATE_TIME DATETIME NOT NULL,
    START_TIME DATETIME DEFAULT NULL,
    END_TIME DATETIME DEFAULT NULL,
    STATUS VARCHAR(10),
    EXIT_CODE VARCHAR(2500),
    EXIT_MESSAGE VARCHAR(2500),
    LAST_UPDATED DATETIME,
    JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
    CONSTRAINT JOB_INST_EXEC_FK FOREIGN KEY (JOB_INSTANCE_ID)
        REFERENCES BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID BIGINT NOT NULL,
    TYPE_CD VARCHAR(6) NOT NULL,
    KEY_NAME VARCHAR(100) NOT NULL,
    STRING_VAL VARCHAR(250),
    DATE_VAL DATETIME DEFAULT NULL,
    LONG_VAL BIGINT,
    DOUBLE_VAL DOUBLE PRECISION,
    IDENTIFYING CHAR(1) NOT NULL,
    CONSTRAINT JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID)
        REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION (
    STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    VERSION BIGINT NOT NULL,
    STEP_NAME VARCHAR(100) NOT NULL,
    JOB_EXECUTION_ID BIGINT NOT NULL,
    START_TIME DATETIME NOT NULL,
    END_TIME DATETIME DEFAULT NULL,
    STATUS VARCHAR(10),
    COMMIT_COUNT BIGINT,
    READ_COUNT BIGINT,
    FILTER_COUNT BIGINT,
    WRITE_COUNT BIGINT,
    READ_SKIP_COUNT BIGINT,
    WRITE_SKIP_COUNT BIGINT,
    PROCESS_SKIP_COUNT BIGINT,
    ROLLBACK_COUNT BIGINT,
    EXIT_CODE VARCHAR(2500),
    EXIT_MESSAGE VARCHAR(2500),
    LAST_UPDATED DATETIME,
    CONSTRAINT JOB_EXEC_STEP_FK FOREIGN KEY (JOB_EXECUTION_ID)
        REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION_CONTEXT (
    STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    CONSTRAINT STEP_EXEC_CTX_FK FOREIGN KEY (STEP_EXECUTION_ID)
        REFERENCES BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    CONSTRAINT JOB_EXEC_CTX_FK FOREIGN KEY (JOB_EXECUTION_ID)
        REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION_SEQ (
    ID BIGINT NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    CONSTRAINT UNIQUE_KEY_UN UNIQUE (UNIQUE_KEY)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_SEQ (
    ID BIGINT NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    CONSTRAINT UNIQUE_KEY_UN UNIQUE (UNIQUE_KEY)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS BATCH_JOB_SEQ (
    ID BIGINT NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    CONSTRAINT UNIQUE_KEY_UN UNIQUE (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT IGNORE INTO BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY) VALUES (0, '0');
INSERT IGNORE INTO BATCH_JOB_EXECUTION_SEQ  (ID, UNIQUE_KEY) VALUES (0, '0');
INSERT IGNORE INTO BATCH_JOB_SEQ            (ID, UNIQUE_KEY) VALUES (0, '0');