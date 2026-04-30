-- ============================================================
-- Customer Management System - DML Script (Master Data)
-- Run after DDL.sql
-- ============================================================

USE customer_db;

-- ------------------------------------------------------------
-- Countries
-- ------------------------------------------------------------
INSERT INTO country (name, code) VALUES
('Sri Lanka',       'LK'),
('India',           'IN'),
('United States',   'US'),
('United Kingdom',  'GB'),
('Australia',       'AU'),
('Canada',          'CA'),
('Germany',         'DE'),
('France',          'FR'),
('Singapore',       'SG'),
('Japan',           'JP'),
('China',           'CN'),
('South Korea',     'KR'),
('Malaysia',        'MY'),
('Thailand',        'TH'),
('Indonesia',       'ID'),
('Pakistan',        'PK'),
('Bangladesh',      'BD'),
('Nepal',           'NP'),
('Maldives',        'MV'),
('United Arab Emirates', 'AE');

-- ------------------------------------------------------------
-- Cities — Sri Lanka
-- ------------------------------------------------------------
INSERT INTO city (name, country_id) VALUES
('Colombo',       1),
('Kandy',         1),
('Galle',         1),
('Jaffna',        1),
('Negombo',       1),
('Ratnapura',     1),
('Kurunegala',    1),
('Anuradhapura',  1),
('Trincomalee',   1),
('Batticaloa',    1),
('Matara',        1),
('Hambantota',    1),
('Badulla',       1),
('Nuwara Eliya',  1),
('Kegalle',       1);

-- ------------------------------------------------------------
-- Cities — India
-- ------------------------------------------------------------
INSERT INTO city (name, country_id) VALUES
('Mumbai',     2),
('Delhi',      2),
('Bangalore',  2),
('Chennai',    2),
('Kolkata',    2),
('Hyderabad',  2),
('Pune',       2),
('Ahmedabad',  2);

-- ------------------------------------------------------------
-- Cities — United States
-- ------------------------------------------------------------
INSERT INTO city (name, country_id) VALUES
('New York',    3),
('Los Angeles', 3),
('Chicago',     3),
('Houston',     3),
('Phoenix',     3),
('Philadelphia',3),
('San Antonio', 3),
('San Diego',   3),
('Dallas',      3),
('San Jose',    3);

-- ------------------------------------------------------------
-- Cities — United Kingdom
-- ------------------------------------------------------------
INSERT INTO city (name, country_id) VALUES
('London',       4),
('Birmingham',   4),
('Manchester',   4),
('Leeds',        4),
('Glasgow',      4),
('Edinburgh',    4),
('Liverpool',    4),
('Bristol',      4);

-- ------------------------------------------------------------
-- Cities — Australia
-- ------------------------------------------------------------
INSERT INTO city (name, country_id) VALUES
('Sydney',      5),
('Melbourne',   5),
('Brisbane',    5),
('Perth',       5),
('Adelaide',    5),
('Canberra',    5);

-- ------------------------------------------------------------
-- Cities — UAE
-- ------------------------------------------------------------
INSERT INTO city (name, country_id) VALUES
('Dubai',       20),
('Abu Dhabi',   20),
('Sharjah',     20),
('Ajman',       20);

-- ------------------------------------------------------------
-- Cities — Singapore, Malaysia, Japan
-- ------------------------------------------------------------
INSERT INTO city (name, country_id) VALUES
('Singapore City', 9),
('Kuala Lumpur',   13),
('Penang',         13),
('Johor Bahru',    13),
('Tokyo',          10),
('Osaka',          10),
('Kyoto',          10);

-- ------------------------------------------------------------
-- Sample Customers (for testing)
-- ------------------------------------------------------------
INSERT INTO customer (name, date_of_birth, nic_number) VALUES
('Kamal Perera',    '1985-03-15', '851234567V'),
('Nimal Silva',     '1990-07-22', '902345678V'),
('Saman Fernando',  '1978-11-30', '783456789V'),
('Kumari Dissanayake', '1995-01-10', '954567890V'),
('Ruwan Jayasinghe','1982-06-05', '825678901V');

-- Sample mobile numbers
INSERT INTO customer_mobile (customer_id, mobile_number) VALUES
(1, '0771234567'),
(1, '0112345678'),
(2, '0769876543'),
(3, '0758765432'),
(4, '0777654321'),
(5, '0762345678');

-- Sample addresses
INSERT INTO customer_address (customer_id, address_line1, address_line2, city_id, country_id) VALUES
(1, '123 Main Street',   'Colombo 03',    1, 1),
(2, '456 Kandy Road',    NULL,            2, 1),
(3, '789 Galle Road',    'Apartment 5B',  3, 1),
(4, '321 Temple Street', NULL,            1, 1),
(5, '654 Beach Road',    'House 12',      5, 1);

-- Sample family relationships
INSERT INTO customer_family (customer_id, family_member_id) VALUES
(1, 2),
(2, 1),
(1, 3),
(3, 1);