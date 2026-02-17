-- Create locations table (DYNAMIC BRANCHES & VEHICLES!)
CREATE TABLE locations (
                           id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                           name VARCHAR(100) NOT NULL,
                           code VARCHAR(50) UNIQUE,
                           location_type VARCHAR(20) NOT NULL,
                           address VARCHAR(255),
                           city VARCHAR(100),
                           phone VARCHAR(20),
                           email VARCHAR(100),
                           latitude DECIMAL(10,8),
                           longitude DECIMAL(11,8),
                           is_active BOOLEAN DEFAULT TRUE,
                           opening_hours VARCHAR(255),
                           manager_name VARCHAR(100),
                           notes TEXT,
                           parent_location_id VARCHAR(36),
                           vehicle_registration VARCHAR(20),
                           vehicle_capacity INTEGER,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           created_by VARCHAR(50),
                           updated_by VARCHAR(50),
                           version BIGINT DEFAULT 0,
                           FOREIGN KEY (parent_location_id) REFERENCES locations(id)
);

-- Insert default locations from SRS
INSERT INTO locations (id, name, code, location_type, is_active) VALUES
    (uuid_generate_v4(), 'Headquarters', 'HQ001', 'HQ', true);

-- Insert 6 branches
INSERT INTO locations (id, name, code, location_type, is_active) VALUES
                                                                     (uuid_generate_v4(), 'Branch 1 - City Center', 'BR001', 'BRANCH', true),
                                                                     (uuid_generate_v4(), 'Branch 2 - Industrial Area', 'BR002', 'BRANCH', true),
                                                                     (uuid_generate_v4(), 'Branch 3 - Suburb', 'BR003', 'BRANCH', true),
                                                                     (uuid_generate_v4(), 'Branch 4 - North', 'BR004', 'BRANCH', true),
                                                                     (uuid_generate_v4(), 'Branch 5 - South', 'BR005', 'BRANCH', true),
                                                                     (uuid_generate_v4(), 'Branch 6 - East', 'BR006', 'BRANCH', true);

-- Insert 5 vehicles
INSERT INTO locations (id, name, code, location_type, vehicle_registration, vehicle_capacity, is_active) VALUES
                                                                                                             (uuid_generate_v4(), 'Vehicle 1 - Delivery Truck', 'VH001', 'VEHICLE', 'T123ABC', 100, true),
                                                                                                             (uuid_generate_v4(), 'Vehicle 2 - Van', 'VH002', 'VEHICLE', 'T456DEF', 50, true),
                                                                                                             (uuid_generate_v4(), 'Vehicle 3 - Truck', 'VH003', 'VEHICLE', 'T789GHI', 150, true),
                                                                                                             (uuid_generate_v4(), 'Vehicle 4 - Small Van', 'VH004', 'VEHICLE', 'T012JKL', 30, true),
                                                                                                             (uuid_generate_v4(), 'Vehicle 5 - Large Truck', 'VH005', 'VEHICLE', 'T345MNO', 200, true);

-- Create indexes
CREATE INDEX idx_locations_type ON locations(location_type);
CREATE INDEX idx_locations_code ON locations(code);
CREATE INDEX idx_locations_active ON locations(is_active);
CREATE INDEX idx_locations_parent ON locations(parent_location_id);
