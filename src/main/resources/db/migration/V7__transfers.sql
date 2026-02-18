-- Create transfer_requests table
CREATE TABLE transfer_requests (
                                   id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   request_number VARCHAR(50) NOT NULL UNIQUE,
                                   from_location_id VARCHAR(36) NOT NULL REFERENCES locations(id),
                                   to_location_id VARCHAR(36) NOT NULL REFERENCES locations(id),
                                   request_date DATE NOT NULL,
                                   requested_by VARCHAR(100) NOT NULL,
                                   expected_delivery_date DATE,
                                   status VARCHAR(20) NOT NULL,
                                   notes TEXT,
                                   reviewed_by VARCHAR(100),
                                   reviewed_at TIMESTAMP,
                                   rejection_reason VARCHAR(255),
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   created_by VARCHAR(50),
                                   updated_by VARCHAR(50),
                                   version BIGINT DEFAULT 0
);

-- Create transfer_request_items table
CREATE TABLE transfer_request_items (
                                        id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                                        transfer_request_id VARCHAR(36) NOT NULL REFERENCES transfer_requests(id) ON DELETE CASCADE,
                                        cylinder_size_id VARCHAR(36) NOT NULL REFERENCES cylinder_sizes(id),
                                        product_type VARCHAR(20) NOT NULL,
                                        requested_quantity INTEGER NOT NULL,
                                        approved_quantity INTEGER,
                                        notes VARCHAR(255),
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        created_by VARCHAR(50),
                                        updated_by VARCHAR(50),
                                        version BIGINT DEFAULT 0
);

-- Create transfers table
CREATE TABLE transfers (
                           id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                           transfer_number VARCHAR(50) NOT NULL UNIQUE,
                           transfer_request_id VARCHAR(36) UNIQUE REFERENCES transfer_requests(id),
                           from_location_id VARCHAR(36) NOT NULL REFERENCES locations(id),
                           to_location_id VARCHAR(36) NOT NULL REFERENCES locations(id),
                           transfer_date DATE NOT NULL,
                           dispatched_by VARCHAR(100),
                           dispatched_at TIMESTAMP,
                           received_by VARCHAR(100),
                           received_at TIMESTAMP,
                           status VARCHAR(20) NOT NULL,
                           notes TEXT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           created_by VARCHAR(50),
                           updated_by VARCHAR(50),
                           version BIGINT DEFAULT 0
);

-- Create transfer_items table
CREATE TABLE transfer_items (
                                id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                                transfer_id VARCHAR(36) NOT NULL REFERENCES transfers(id) ON DELETE CASCADE,
                                cylinder_size_id VARCHAR(36) NOT NULL REFERENCES cylinder_sizes(id),
                                product_type VARCHAR(20) NOT NULL,
                                quantity INTEGER NOT NULL,
                                empty_returned_quantity INTEGER,
                                batch_number VARCHAR(50),
                                notes VARCHAR(255),
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                created_by VARCHAR(50),
                                updated_by VARCHAR(50),
                                version BIGINT DEFAULT 0
);

-- Create indexes for performance
CREATE INDEX idx_transfer_requests_from_location ON transfer_requests(from_location_id);
CREATE INDEX idx_transfer_requests_to_location ON transfer_requests(to_location_id);
CREATE INDEX idx_transfer_requests_status ON transfer_requests(status);
CREATE INDEX idx_transfer_requests_date ON transfer_requests(request_date);

CREATE INDEX idx_transfers_from_location ON transfers(from_location_id);
CREATE INDEX idx_transfers_to_location ON transfers(to_location_id);
CREATE INDEX idx_transfers_status ON transfers(status);
CREATE INDEX idx_transfers_date ON transfers(transfer_date);
CREATE INDEX idx_transfers_request ON transfers(transfer_request_id);

CREATE INDEX idx_transfer_items_transfer ON transfer_items(transfer_id);
CREATE INDEX idx_transfer_items_cylinder ON transfer_items(cylinder_size_id);
