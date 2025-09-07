-- For a clean start, drop tables in reverse order of dependency
DROP TABLE IF EXISTS granted_permissions;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS modules;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- ====================================================================================
-- STEP 1: CORE USER AND ROLE TABLES
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

-- The 'roles' table is now simpler. It just defines the role's name.
-- The direct link to permissions is now in the 'role_permissions' table.
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


-- ====================================================================================
-- STEP 2: NEW DYNAMIC PERMISSION TABLES (MODULE, ROLE_PERMISSIONS, GRANTED_PERMISSIONS)
-- ====================================================================================

CREATE TABLE IF NOT EXISTS modules
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_name VARCHAR(100) NOT NULL UNIQUE,
    module_key  VARCHAR(100) NOT NULL UNIQUE, -- e.g., 'PATIENT_MANAGEMENT'
    url_path    VARCHAR(255) NOT NULL         -- e.g., '/api/patients'
);

CREATE TABLE IF NOT EXISTS role_permissions
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id   BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES modules (id) ON DELETE CASCADE,
    UNIQUE KEY (role_id, module_id) -- A role can only have one set of permissions per module
);

-- This table holds the actual permissions (CREATE, VIEW, etc.) for a specific role-module link.
CREATE TABLE IF NOT EXISTS granted_permissions
(
    role_permission_id BIGINT      NOT NULL,
    permission         VARCHAR(20) NOT NULL, -- e.g., 'CREATE', 'EDIT', 'VIEW'
    PRIMARY KEY (role_permission_id, permission),
    FOREIGN KEY (role_permission_id) REFERENCES role_permissions (id) ON DELETE CASCADE
);


-- ====================================================================================
-- STEP 3: APPLICATION-SPECIFIC TABLES (e.g., Patients)
-- ====================================================================================

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
    user_id           BIGINT       NOT NULL UNIQUE, -- A user can only be linked to one patient profile
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);


-- ====================================================================================
-- STEP 4: INSERTING SAMPLE DATA
-- ====================================================================================

-- Insert Modules
INSERT INTO modules (module_name, module_key, url_path)
VALUES ('Patient Management', 'PATIENT_MANAGEMENT', '/api/patients'),
       ('User Management', 'USER_MANAGEMENT', '/api/users'),
       ('Role Management', 'ROLE_MANAGEMENT', '/api/roles');

-- Insert Roles
-- These are now just names. The power comes from the permissions we assign next.
INSERT INTO roles (role_name)
VALUES ('SUPER_ADMIN'),
       ('PATIENT'),
       ('FRONT_DESK_STAFF');
-- A new dynamic role

-- Insert Users
INSERT INTO users (username, password, email)
VALUES ('superadmin', '$2a$10$pS.m2gDna3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD', 'admin@example.com'),
       ('johndoe', '$2a$10$pS.m2gDna3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD', 'john.doe@example.com'),
       ('janesmith', '$2a$10$pS.m2gDna3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD', 'jane.smith@example.com'),
       ('frontdesk', '$2a$10$pS.m2gDna3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD.z8a3aD', 'frontdesk@clinic.com');


-- Assign Roles to Users
INSERT INTO user_roles (user_id, role_id)
VALUES ((SELECT user_id FROM users WHERE username = 'superadmin'),
        (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN')),
       ((SELECT user_id FROM users WHERE username = 'johndoe'),
        (SELECT role_id FROM roles WHERE role_name = 'PATIENT')),
       ((SELECT user_id FROM users WHERE username = 'janesmith'),
        (SELECT role_id FROM roles WHERE role_name = 'PATIENT')),
       ((SELECT user_id FROM users WHERE username = 'frontdesk'),
        (SELECT role_id FROM roles WHERE role_name = 'FRONT_DESK_STAFF'));


-- ====================================================================================
-- STEP 5: ASSIGNING DYNAMIC PERMISSIONS TO ROLES
-- This is the key part of the new system.
-- ====================================================================================

-- Grant 'FRONT_DESK_STAFF' permissions for 'Patient Management'
-- 1. Create the link between the role and the module
INSERT INTO role_permissions (role_id, module_id)
VALUES ((SELECT role_id FROM roles WHERE role_name = 'FRONT_DESK_STAFF'),
        (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT'));

-- 2. Grant the specific permissions for that link
INSERT INTO granted_permissions (role_permission_id, permission)
VALUES ((SELECT id
         FROM role_permissions
         WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'FRONT_DESK_STAFF')
           AND module_id = (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT')), 'CREATE'),
       ((SELECT id
         FROM role_permissions
         WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'FRONT_DESK_STAFF')
           AND module_id = (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT')), 'VIEW'),
       ((SELECT id
         FROM role_permissions
         WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'FRONT_DESK_STAFF')
           AND module_id = (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT')), 'EDIT');
-- NOTE: We did NOT grant 'DELETE' permission to the front desk staff.

-- Grant 'SUPER_ADMIN' ALL permissions for ALL modules
-- For Patient Management
INSERT INTO role_permissions (role_id, module_id)
VALUES ((SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN'),
        (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT'));
INSERT INTO granted_permissions (role_permission_id, permission)
VALUES ((SELECT id
         FROM role_permissions
         WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN')
           AND module_id = (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT')), 'CREATE'),
       ((SELECT id
         FROM role_permissions
         WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN')
           AND module_id = (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT')), 'VIEW'),
       ((SELECT id
         FROM role_permissions
         WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN')
           AND module_id = (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT')), 'EDIT'),
       ((SELECT id
         FROM role_permissions
         WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN')
           AND module_id = (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT')), 'DELETE'),
       ((SELECT id
         FROM role_permissions
         WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN')
           AND module_id = (SELECT id FROM modules WHERE module_key = 'PATIENT_MANAGEMENT')), 'LIST');
-- (You would repeat this for User Management and Role Management for the SUPER_ADMIN)


-- ====================================================================================
-- STEP 6: Insert data into the child table (patients)
-- This part remains largely the same.
-- ====================================================================================

INSERT INTO patients (id, first_name, last_name, username, email, date_of_birth, gender, address_line1, city, state,
                      country, postal_code, registration_date, user_id, created_at, updated_at)
VALUES ('A1b2C3d4E5f6G7', 'John', 'Doe', 'johndoe', 'john.doe@example.com', '1985-06-15', 'MALE', '123 Main St',
        'Springfield', 'Illinois', 'USA', '62701', '2024-01-10',
        (SELECT user_id FROM users WHERE username = 'johndoe'),
        NOW(), NOW()),
       ('H8i9J0k1L2m3N4', 'Jane', 'Smith', 'janesmith', 'jane.smith@example.com', '1990-09-23', 'FEMALE', '456 Elm St',
        'Shelbyville', 'Illinois', 'USA', '62565', '2023-12-01',
        (SELECT user_id FROM users WHERE username = 'janesmith'),
        NOW(), NOW());
