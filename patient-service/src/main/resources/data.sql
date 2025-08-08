CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(20) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    username VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    registration_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

--- Example: 30 random 14-character alphanumeric IDs
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'A1b2C3d4E5f6G7', 'John', 'Doe', 'johndoe', 'john.doe@example.com', '1985-06-15', 'MALE', '123 Main St', NULL, 'Springfield', 'Illinois', 'USA', '62701', '2024-01-10', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'A1b2C3d4E5f6G7');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'H8i9J0k1L2m3N4', 'Jane', 'Smith', 'janesmith', 'jane.smith@example.com', '1990-09-23', 'FEMALE', '456 Elm St', 'Apt 2B', 'Shelbyville', 'Illinois', 'USA', '62565', '2023-12-01', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'H8i9J0k1L2m3N4');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'O5p6Q7r8S9t0U1', 'Alice', 'Johnson', 'alicej', 'alice.johnson@example.com', '1978-03-12', 'FEMALE', '789 Oak St', NULL, 'Capital City', 'Illinois', 'USA', '62629', '2022-06-20', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'O5p6Q7r8S9t0U1');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'V2w3X4y5Z6a7B8', 'Bob', 'Brown', 'bobbrown', 'bob.brown@example.com', '1982-11-30', 'MALE', '321 Pine St', NULL, 'Springfield', 'Illinois', 'USA', '62702', '2023-05-14', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'V2w3X4y5Z6a7B8');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'C9d0E1f2G3h4I5', 'Emily', 'Davis', 'emilyd', 'emily.davis@example.com', '1995-02-05', 'FEMALE', '654 Maple St', NULL, 'Shelbyville', 'Illinois', 'USA', '62566', '2024-03-01', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'C9d0E1f2G3h4I5');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'J6k7L8m9N0o1P2', 'Michael', 'Green', 'mikegreen', 'michael.green@example.com', '1988-07-25', 'MALE', '987 Cedar St', NULL, 'Springfield', 'Illinois', 'USA', '62703', '2024-02-15', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'J6k7L8m9N0o1P2');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'Q3r4S5t6U7v8W9', 'Sarah', 'Taylor', 'saraht', 'sarah.taylor@example.com', '1992-04-18', 'FEMALE', '123 Birch St', 'Suite 5', 'Shelbyville', 'Illinois', 'USA', '62567', '2023-08-25', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'Q3r4S5t6U7v8W9');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'X0y1Z2a3B4c5D6', 'David', 'Wilson', 'davidw', 'david.wilson@example.com', '1975-01-11', 'MALE', '456 Ash St', NULL, 'Capital City', 'Illinois', 'USA', '62630', '2022-10-10', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'X0y1Z2a3B4c5D6');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'E7f8G9h0I1j2K3', 'Laura', 'White', 'lauraw', 'laura.white@example.com', '1989-09-02', 'FEMALE', '789 Palm St', NULL, 'Springfield', 'Illinois', 'USA', '62704', '2024-04-20', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'E7f8G9h0I1j2K3');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'L4m5N6o7P8q9R0', 'James', 'Harris', 'jamesh', 'james.harris@example.com', '1993-11-15', 'MALE', '321 Cherry St', NULL, 'Shelbyville', 'Illinois', 'USA', '62568', '2023-06-30', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'L4m5N6o7P8q9R0');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'S1t2U3v4W5x6Y7', 'Emma', 'Moore', 'emmam', 'emma.moore@example.com', '1980-08-09', 'FEMALE', '654 Spruce St', NULL, 'Capital City', 'Illinois', 'USA', '62631', '2023-01-22', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'S1t2U3v4W5x6Y7');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'Z8a9B0c1D2e3F4', 'Ethan', 'Martinez', 'ethanm', 'ethan.martinez@example.com', '1984-05-03', 'MALE', '987 Redwood St', NULL, 'Springfield', 'Illinois', 'USA', '62705', '2024-05-12', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'Z8a9B0c1D2e3F4');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'G5h6I7j8K9l0M1', 'Sophia', 'Clark', 'sophiac', 'sophia.clark@example.com', '1991-12-25', 'FEMALE', '123 Hickory St', NULL, 'Shelbyville', 'Illinois', 'USA', '62569', '2022-11-11', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'G5h6I7j8K9l0M1');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'N2o3P4q5R6s7T8', 'Daniel', 'Lewis', 'danlewis', 'daniel.lewis@example.com', '1976-06-08', 'MALE', '456 Cypress St', NULL, 'Capital City', 'Illinois', 'USA', '62632', '2023-09-19', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'N2o3P4q5R6s7T8');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'U9v0W1x2Y3z4A5', 'Isabella', 'Walker', 'isabellaw', 'isabella.walker@example.com', '1987-10-17', 'FEMALE', '789 Willow St', NULL, 'Springfield', 'Illinois', 'USA', '62706', '2024-03-29', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'U9v0W1x2Y3z4A5');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'B6c7D8e9F0g1H2', 'Oliver', 'King', 'oliverk', 'oliver.king@example.com', '1983-03-22', 'MALE', '321 Aspen St', NULL, 'Shelbyville', 'Illinois', 'USA', '62570', '2023-07-15', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'B6c7D8e9F0g1H2');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'I3j4K5l6M7n8O9', 'Mia', 'Scott', 'mias', 'mia.scott@example.com', '1996-08-14', 'FEMALE', '654 Poplar St', NULL, 'Capital City', 'Illinois', 'USA', '62633', '2024-02-28', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'I3j4K5l6M7n8O9');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'P0q1R2s3T4u5V6', 'William', 'Young', 'willyoung', 'william.young@example.com', '1981-12-19', 'MALE', '987 Fir St', NULL, 'Springfield', 'Illinois', 'USA', '62707', '2023-04-18', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'P0q1R2s3T4u5V6');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'W7x8Y9z0A1b2C3', 'Charlotte', 'Hall', 'charhall', 'charlotte.hall@example.com', '1994-05-27', 'FEMALE', '123 Magnolia St', NULL, 'Shelbyville', 'Illinois', 'USA', '62571', '2022-12-05', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'W7x8Y9z0A1b2C3');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'D4e5F6g7H8i9J0', 'Benjamin', 'Allen', 'benallen', 'benjamin.allen@example.com', '1986-09-10', 'MALE', '456 Dogwood St', NULL, 'Capital City', 'Illinois', 'USA', '62634', '2024-01-20', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'D4e5F6g7H8i9J0');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'K1l2M3n4O5p6Q7', 'Amelia', 'Wright', 'ameliaw', 'amelia.wright@example.com', '1997-11-03', 'FEMALE', '789 Sycamore St', NULL, 'Springfield', 'Illinois', 'USA', '62708', '2023-03-12', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'K1l2M3n4O5p6Q7');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'R8s9T0u1V2w3X4', 'Lucas', 'King', 'lucasking', 'lucas.king@example.com', '1987-04-16', 'MALE', '321 Beech St', NULL, 'Shelbyville', 'Illinois', 'USA', '62572', '2024-05-05', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'R8s9T0u1V2w3X4');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'Y5z6A7b8C9d0E1', 'Harper', 'Baker', 'harperb', 'harper.baker@example.com', '1993-07-29', 'OTHER', '654 Walnut St', NULL, 'Capital City', 'Illinois', 'USA', '62635', '2022-08-17', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'Y5z6A7b8C9d0E1');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'F2g3H4i5J6k7L8', 'Henry', 'Adams', 'henrya', 'henry.adams@example.com', '1982-10-21', 'MALE', '987 Chestnut St', NULL, 'Springfield', 'Illinois', 'USA', '62709', '2023-02-24', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'F2g3H4i5J6k7L8');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'M9n0O1p2Q3r4S5', 'Evelyn', 'Nelson', 'evelynn', 'evelyn.nelson@example.com', '1998-01-07', 'FEMALE', '123 Alder St', NULL, 'Shelbyville', 'Illinois', 'USA', '62573', '2024-04-02', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'M9n0O1p2Q3r4S5');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'T6u7V8w9X0y1Z2', 'Jack', 'Carter', 'jackc', 'jack.carter@example.com', '1986-06-11', 'MALE', '456 Hawthorn St', NULL, 'Capital City', 'Illinois', 'USA', '62636', '2022-09-09', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'T6u7V8w9X0y1Z2');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'A3b4C5d6E7f8G9', 'Grace', 'Mitchell', 'gracem', 'grace.mitchell@example.com', '1992-03-30', 'FEMALE', '789 Larch St', NULL, 'Springfield', 'Illinois', 'USA', '62710', '2023-06-21', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'A3b4C5d6E7f8G9');
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, address_line2, city, state, country, postal_code, registration_date, created_at, updated_at)
SELECT 'H0i1J2k3L4m5N6', 'Alexander', 'Perez', 'alexp', 'alexander.perez@example.com', '1989-12-13', 'MALE', '321 Sequoia St', NULL, 'Shelbyville', 'Illinois', 'USA', '62574', '2024-05-18', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM patients WHERE id = 'H0i1J2k3L4m5N6');