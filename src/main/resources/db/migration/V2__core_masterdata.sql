-- Create cylinder_sizes table
CREATE TABLE cylinder_sizes (
                                id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                                name VARCHAR(50) NOT NULL UNIQUE,
                                weight_kg DECIMAL(10,2) NOT NULL,
                                tare_weight_kg DECIMAL(10,2),
                                description VARCHAR(255),
                                is_active BOOLEAN DEFAULT TRUE,
                                display_order INTEGER,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                created_by VARCHAR(50),
                                updated_by VARCHAR(50),
                                version BIGINT DEFAULT 0
);

-- Create suppliers table
CREATE TABLE suppliers (
                           id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                           name VARCHAR(100) NOT NULL,
                           code VARCHAR(50) UNIQUE,
                           contact_person VARCHAR(100),
                           phone VARCHAR(20),
                           email VARCHAR(100),
                           address VARCHAR(255),
                           tax_id VARCHAR(50),
                           payment_terms VARCHAR(255),
                           is_active BOOLEAN DEFAULT TRUE,
                           notes TEXT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           created_by VARCHAR(50),
                           updated_by VARCHAR(50),
                           version BIGINT DEFAULT 0
);

-- Create price_categories table
CREATE TABLE price_categories (
                                  id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  name VARCHAR(100) NOT NULL,
                                  cylinder_size_id VARCHAR(36) NOT NULL REFERENCES cylinder_sizes(id),
                                  product_type VARCHAR(20) NOT NULL,
                                  price DECIMAL(19,2) NOT NULL,
                                  effective_from DATE NOT NULL,
                                  effective_to DATE,
                                  is_active BOOLEAN DEFAULT TRUE,
                                  min_quantity INTEGER,
                                  max_quantity INTEGER,
                                  applicable_locations TEXT,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  created_by VARCHAR(50),
                                  updated_by VARCHAR(50),
                                  version BIGINT DEFAULT 0
);

-- Create indexes
CREATE INDEX idx_price_categories_cylinder_size ON price_categories(cylinder_size_id);
CREATE INDEX idx_price_categories_dates ON price_categories(effective_from, effective_to);
CREATE INDEX idx_suppliers_code ON suppliers(code);
CREATE INDEX idx_suppliers_name ON suppliers(name);
