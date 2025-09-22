-- For a clean development start, drop tables in reverse order of dependency
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS granted_permissions;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS modules;

-- ====================================================================================
-- STEP 1: CREATE TABLES (Your schema is correct, no changes needed here)
-- ====================================================================================

CREATE TABLE IF NOT EXISTS users
(
    user_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles
(
    role_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS modules
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_name VARCHAR(100) NOT NULL UNIQUE,
    module_key  VARCHAR(100) NOT NULL UNIQUE,
    url_path    VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS role_permissions
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id   BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES modules (id) ON DELETE CASCADE,
    UNIQUE KEY (role_id, module_id)
);

CREATE TABLE IF NOT EXISTS granted_permissions
(
    role_permission_id BIGINT      NOT NULL,
    permission         VARCHAR(20) NOT NULL,
    PRIMARY KEY (role_permission_id, permission),
    FOREIGN KEY (role_permission_id) REFERENCES role_permissions (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS patients
(
    id                VARCHAR(20) PRIMARY KEY,
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    email             VARCHAR(100) NOT NULL UNIQUE,
    date_of_birth     DATE         NOT NULL,
    gender            VARCHAR(10)  NOT NULL,
    address_line1     VARCHAR(255) NOT NULL,
    address_line2     VARCHAR(255),
    city              VARCHAR(100) NOT NULL,
    state             VARCHAR(100) NOT NULL,
    country           VARCHAR(100) NOT NULL,
    postal_code       VARCHAR(20)  NOT NULL,
    registration_date DATE         NOT NULL,
    user_id           BIGINT       NOT NULL UNIQUE,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);


-- ====================================================================================
-- STEP 2: INSERT CORE DATA (Modules, Roles, Users)
-- ====================================================================================

-- Insert Modules (The "what" can be controlled)
INSERT INTO modules (module_name, module_key, url_path)
VALUES ('Patient Management', 'PATIENT_MANAGEMENT', '/api/v1/patients'),
       ('User Management', 'USER_MANAGEMENT', '/api/v1/users'),
       ('Role Management', 'ROLE_MANAGEMENT', '/api/v1/roles');

-- Insert Roles (The "who" or job function)
INSERT INTO roles (role_name)
VALUES ('ROLE_ADMIN'),
       ('ROLE_PATIENT'),
       ('ROLE_RECEPTIONIST');

-- Insert Users (The individuals)
-- This Bcrypt hash is for the password 'password'
INSERT INTO users (username, password, email, enabled)
VALUES ('superadmin', '$2a$10$GRL1P5C5nOaB1aez1dG/NOXmEdx8kKk0iH9.G5HD3D8b2Yg3.8R/q', 'superadmin@clinic.com', true),
       ('janesmith', '$2a$10$GRL1P5C5nOaB1aez1dG/NOXmEdx8kKk0iH9.G5HD3D8b2Yg3.8R/q', 'jane.smith@example.com', true),
       ('frontdesk', '$2a$10$GRL1P5C5nOaB1aez1dG/NOXmEdx8kKk0iH9.G5HD3D8b2Yg3.8R/q', 'frontdesk@clinic.com', true);

-- Assign Roles to Users
INSERT INTO user_roles (user_id, role_id)
VALUES ((SELECT user_id FROM users WHERE username = 'superadmin'),
        (SELECT role_id FROM roles WHERE role_name = 'ROLE_ADMIN')),
       ((SELECT user_id FROM users WHERE username = 'janesmith'),
        (SELECT role_id FROM roles WHERE role_name = 'ROLE_PATIENT')),
       ((SELECT user_id FROM users WHERE username = 'frontdesk'),
        (SELECT role_id FROM roles WHERE role_name = 'ROLE_RECEPTIONIST'));


-- ====================================================================================
-- STEP 3: LINK ROLES TO MODULES
-- ====================================================================================

-- The ADMIN role has permissions related to ALL modules.
INSERT INTO role_permissions (role_id, module_id)
VALUES ((SELECT role_id FROM roles WHERE role_name = 'ROLE_ADMIN'),
        (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT')),
       ((SELECT role_id FROM roles WHERE role_name = 'ROLE_ADMIN'),
        (SELECT id FROM modules WHERE module_key = 'USER_MANAGEMENT')),
       ((SELECT role_id FROM roles WHERE role_name = 'ROLE_ADMIN'),
        (SELECT id FROM modules WHERE module_key = 'ROLE_MANAGEMENT'));

-- The RECEPTIONIST and PATIENT roles have permissions ONLY for the Patient Management module.
INSERT INTO role_permissions (role_id, module_id)
VALUES ((SELECT role_id FROM roles WHERE role_name = 'ROLE_RECEPTIONIST'),
        (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT')),
       ((SELECT role_id FROM roles WHERE role_name = 'ROLE_PATIENT'),
        (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT'));


-- ====================================================================================
-- STEP 4: GRANT PERMISSION *ACTIONS* TO EACH ROLE-MODULE LINK
-- ====================================================================================

-- Get the ID for the Admin's link to the Patient Management module
SET @admin_patient_perm_id = (SELECT id
                              FROM role_permissions
                              WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'ROLE_ADMIN')
                                AND module_id = (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT'));

-- Grant ADMIN full control over Patient Management
-- **CORRECTED**: We only insert the ACTION here. The Java code will prepend "PATIENT_MANAGEMENT:".
INSERT INTO granted_permissions (role_permission_id, permission)
VALUES (@admin_patient_perm_id, 'CREATE'),
       (@admin_patient_perm_id, 'VIEW'),
       (@admin_patient_perm_id, 'EDIT'),
       (@admin_patient_perm_id, 'DELETE'),
       (@admin_patient_perm_id, 'LIST');


-- Get the ID for the Receptionist's link to the Patient Management module
SET @receptionist_patient_perm_id = (SELECT id
                                     FROM role_permissions
                                     WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'ROLE_RECEPTIONIST')
                                       AND
                                         module_id = (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT'));

-- Grant RECEPTIONIST limited control over Patient Management (No Delete)
-- **CORRECTED**: Only the action is stored.
INSERT INTO granted_permissions (role_permission_id, permission)
VALUES (@receptionist_patient_perm_id, 'CREATE'),
       (@receptionist_patient_perm_id, 'VIEW'),
       (@receptionist_patient_perm_id, 'EDIT'),
       (@receptionist_patient_perm_id, 'LIST');


-- Get the ID for the Patient's link to the Patient Management module
SET @patient_perm_id = (SELECT id
                        FROM role_permissions
                        WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'ROLE_PATIENT')
                          AND module_id = (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT'));

-- Grant PATIENT very limited, self-service control.
-- **CORRECTED**: Only the action is stored.
INSERT INTO granted_permissions (role_permission_id, permission)
VALUES (@patient_perm_id, 'VIEW_OWN'),
       (@patient_perm_id, 'EDIT_OWN');


-- ====================================================================================
-- STEP 5: Insert application-specific data
-- ====================================================================================

INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, city, state,
                      country, postal_code, registration_date, user_id, created_at, updated_at)
VALUES ('PAT0001', 'Jane', 'Smith', 'janesmith', 'jane.smith@example.com', '1990-09-23', 'FEMALE', '456 Elm St',
        'Shelbyville', 'Illinois', 'USA', '62565', CURDATE(), (SELECT user_id FROM users WHERE username = 'janesmith'),
        NOW(), NOW());