-- For a clean start, drop tables in reverse order of dependency
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- STEP 1: Create all tables with the correct schema and constraints
CREATE TABLE IF NOT EXISTS users (
                                     user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS roles (
                                     role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     role_name VARCHAR(20) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL,
                                          PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
    );

CREATE TABLE IF NOT EXISTS patients (
                                        id VARCHAR(20) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    registration_date DATE NOT NULL,
    user_id BIGINT NOT NULL, -- This column MUST have a value
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    );
INSERT INTO roles (role_name) VALUES ('ROLE_ADMIN'), ('ROLE_PATIENT');

INSERT INTO users (username, password, email) VALUES
                                                  ('admin1', '$2a$10$pS.m2gDna3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD', 'admin@example1.com'),
                                                  ('johndoe', '$2a$10$pS.m2gDna3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD', 'john.doe@example.com'),
                                                  ('janesmith', '$2a$10$pS.m2gDna3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD', 'jane.smith@example.com');

INSERT INTO user_roles (user_id, role_id) VALUES
                                              ((SELECT user_id FROM users WHERE username = 'admin1'), (SELECT role_id FROM roles WHERE role_name = 'ROLE_ADMIN')),
                                              ((SELECT user_id FROM users WHERE username = 'johndoe'), (SELECT role_id FROM roles WHERE role_name = 'ROLE_PATIENT')),
                                              ((SELECT user_id FROM users WHERE username = 'janesmith'), (SELECT role_id FROM roles WHERE role_name = 'ROLE_PATIENT'));



-- STEP 3: Insert data into the child table (patients), providing the required user_id
INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, city, state, country, postal_code, registration_date, user_id, created_at, updated_at) VALUES
                                                                                                                                                                                                   (
                                                                                                                                                                                                       'A1b2C3d4E5f6G7', 'John', 'Doe', 'johndoe', 'john.doe@example.com', '1985-06-15', 'MALE', '123 Main St',
                                                                                                                                                                                                       'Springfield', 'Illinois', 'USA', '62701', '2024-01-10',
                                                                                                                                                                                                       (SELECT user_id FROM users WHERE username = 'johndoe'), -- ✅ Providing the user_id
                                                                                                                                                                                                       NOW(), NOW()
                                                                                                                                                                                                   ),
                                                                                                                                                                                                   (
                                                                                                                                                                                                       'H8i9J0k1L2m3N4', 'Jane', 'Smith', 'janesmith', 'jane.smith@example.com', '1990-09-23', 'FEMALE', '456 Elm St',
                                                                                                                                                                                                       'Shelbyville', 'Illinois', 'USA', '62565', '2023-12-01',
                                                                                                                                                                                                       (SELECT user_id FROM users WHERE username = 'janesmith'), -- ✅ Providing the user_id
                                                                                                                                                                                                       NOW(), NOW()
                                                                                                                                                                                                   );