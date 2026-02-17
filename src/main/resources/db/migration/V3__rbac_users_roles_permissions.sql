-- Create roles table
CREATE TABLE roles (
                       id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(255),
                       is_system_role BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(50),
                       updated_by VARCHAR(50)
);

-- Create permissions table
CREATE TABLE permissions (
                             id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                             name VARCHAR(100) NOT NULL UNIQUE,
                             resource VARCHAR(50),
                             action VARCHAR(20),
                             description VARCHAR(255),
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create role_permissions junction table
CREATE TABLE role_permissions (
                                  role_id VARCHAR(36) REFERENCES roles(id) ON DELETE CASCADE,
                                  permission_id VARCHAR(36) REFERENCES permissions(id) ON DELETE CASCADE,
                                  PRIMARY KEY (role_id, permission_id)
);

-- Create users table
CREATE TABLE users (
                       id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       first_name VARCHAR(50),
                       last_name VARCHAR(50),
                       phone VARCHAR(20),
                       is_active BOOLEAN DEFAULT TRUE,
                       last_login TIMESTAMP,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(50),
                       updated_by VARCHAR(50)
);

-- Create user_roles junction table
CREATE TABLE user_roles (
                            user_id VARCHAR(36) REFERENCES users(id) ON DELETE CASCADE,
                            role_id VARCHAR(36) REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

-- Create audit_logs table
CREATE TABLE audit_logs (
                            id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                            user_id VARCHAR(36) REFERENCES users(id),
                            action VARCHAR(50) NOT NULL,
                            entity_type VARCHAR(50),
                            entity_id VARCHAR(36),
                            old_value TEXT,
                            new_value TEXT,
                            ip_address VARCHAR(45),
                            user_agent TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default roles (matching SRS)
INSERT INTO roles (id, name, description, is_system_role) VALUES
                                                              (uuid_generate_v4(), 'SYSTEM_ADMIN', 'Full system access', TRUE),
                                                              (uuid_generate_v4(), 'HQ_MANAGER', 'Headquarters management', TRUE),
                                                              (uuid_generate_v4(), 'BRANCH_MANAGER', 'Branch operations management', TRUE),
                                                              (uuid_generate_v4(), 'VEHICLE_OPERATOR', 'Vehicle sales and distribution', TRUE),
                                                              (uuid_generate_v4(), 'ACCOUNTANT', 'Financial management', TRUE),
                                                              (uuid_generate_v4(), 'AUDITOR', 'Read-only audit access', TRUE);

-- Insert basic permissions
INSERT INTO permissions (id, name, resource, action) VALUES
                                                         -- User management
                                                         (uuid_generate_v4(), 'user:create', 'users', 'create'),
                                                         (uuid_generate_v4(), 'user:read', 'users', 'read'),
                                                         (uuid_generate_v4(), 'user:update', 'users', 'update'),
                                                         (uuid_generate_v4(), 'user:delete', 'users', 'delete'),

                                                         -- Inventory
                                                         (uuid_generate_v4(), 'inventory:view', 'inventory', 'view'),
                                                         (uuid_generate_v4(), 'inventory:adjust', 'inventory', 'adjust'),
                                                         (uuid_generate_v4(), 'inventory:transfer', 'inventory', 'transfer'),

                                                         -- Sales
                                                         (uuid_generate_v4(), 'sale:create', 'sales', 'create'),
                                                         (uuid_generate_v4(), 'sale:view', 'sales', 'view'),
                                                         (uuid_generate_v4(), 'sale:void', 'sales', 'void'),

                                                         -- Credit
                                                         (uuid_generate_v4(), 'credit:view', 'credit', 'view'),
                                                         (uuid_generate_v4(), 'credit:approve', 'credit', 'approve'),
                                                         (uuid_generate_v4(), 'credit:limit:set', 'credit', 'set_limit'),

                                                         -- Reports
                                                         (uuid_generate_v4(), 'report:view', 'reports', 'view'),
                                                         (uuid_generate_v4(), 'report:export', 'reports', 'export');

-- Create indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
