-- Create customers table
CREATE TABLE customers (
                           id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                           customer_number VARCHAR(50) NOT NULL UNIQUE,
                           name VARCHAR(100) NOT NULL,
                           phone VARCHAR(20),
                           email VARCHAR(100),
                           address VARCHAR(255),
                           customer_type VARCHAR(20) DEFAULT 'RETAIL',
                           tax_id VARCHAR(50),
                           credit_limit DECIMAL(19,2),
                           current_balance DECIMAL(19,2) DEFAULT 0,
                           is_active BOOLEAN DEFAULT TRUE,
                           notes TEXT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           created_by VARCHAR(50),
                           updated_by VARCHAR(50),
                           version BIGINT DEFAULT 0
);

-- Create sales table
CREATE TABLE sales (
                       id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                       invoice_number VARCHAR(50) NOT NULL UNIQUE,
                       location_id VARCHAR(36) NOT NULL REFERENCES locations(id),
                       customer_id VARCHAR(36) REFERENCES customers(id),
                       sales_person_id VARCHAR(36) NOT NULL REFERENCES users(id),
                       sale_date DATE NOT NULL,
                       sale_time TIMESTAMP NOT NULL,
                       subtotal DECIMAL(19,2) NOT NULL,
                       discount DECIMAL(19,2) DEFAULT 0,
                       tax DECIMAL(19,2) DEFAULT 0,
                       total_amount DECIMAL(19,2) NOT NULL,
                       paid_amount DECIMAL(19,2) DEFAULT 0,
                       balance_due DECIMAL(19,2) DEFAULT 0,
                       payment_method VARCHAR(20),
                       payment_reference VARCHAR(100),
                       status VARCHAR(20) NOT NULL,
                       is_credit_sale BOOLEAN DEFAULT FALSE,
                       notes TEXT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(50),
                       updated_by VARCHAR(50),
                       version BIGINT DEFAULT 0
);

-- Create sale_items table
CREATE TABLE sale_items (
                            id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                            sale_id VARCHAR(36) NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
                            cylinder_size_id VARCHAR(36) NOT NULL REFERENCES cylinder_sizes(id),
                            product_type VARCHAR(20) NOT NULL,
                            quantity INTEGER NOT NULL,
                            unit_price DECIMAL(19,2) NOT NULL,
                            discount DECIMAL(19,2) DEFAULT 0,
                            total_price DECIMAL(19,2) NOT NULL,
                            empty_returned BOOLEAN DEFAULT FALSE,
                            empty_quantity INTEGER,
                            batch_number VARCHAR(50),
                            notes VARCHAR(255),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            created_by VARCHAR(50),
                            updated_by VARCHAR(50),
                            version BIGINT DEFAULT 0
);

-- Create sale_payments table (for multi-payment support)
CREATE TABLE sale_payments (
                               id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                               sale_id VARCHAR(36) NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
                               payment_method VARCHAR(20) NOT NULL,
                               amount DECIMAL(19,2) NOT NULL,
                               reference_number VARCHAR(100),
                               payment_date TIMESTAMP NOT NULL,
                               received_by VARCHAR(100),
                               notes VARCHAR(255),
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               created_by VARCHAR(50),
                               updated_by VARCHAR(50),
                               version BIGINT DEFAULT 0
);

-- Create invoice_number_sequences table
CREATE TABLE invoice_number_sequences (
                                          id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                                          location_id VARCHAR(36) NOT NULL,
                                          sale_date DATE NOT NULL,
                                          sequence_number INTEGER NOT NULL,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create sale_status_history table
CREATE TABLE sale_status_history (
                                     id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                                     sale_id VARCHAR(36) NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
                                     previous_status VARCHAR(20),
                                     new_status VARCHAR(20) NOT NULL,
                                     changed_by VARCHAR(100),
                                     changed_at TIMESTAMP NOT NULL,
                                     reason VARCHAR(255),
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     created_by VARCHAR(50),
                                     updated_by VARCHAR(50),
                                     version BIGINT DEFAULT 0
);

-- Create indexes
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_number ON customers(customer_number);

CREATE INDEX idx_sales_invoice ON sales(invoice_number);
CREATE INDEX idx_sales_location ON sales(location_id);
CREATE INDEX idx_sales_customer ON sales(customer_id);
CREATE INDEX idx_sales_person ON sales(sales_person_id);
CREATE INDEX idx_sales_date ON sales(sale_date);
CREATE INDEX idx_sales_status ON sales(status);
CREATE INDEX idx_sales_credit ON sales(is_credit_sale) WHERE is_credit_sale = true;

CREATE INDEX idx_sale_items_sale ON sale_items(sale_id);
CREATE INDEX idx_sale_items_cylinder ON sale_items(cylinder_size_id);

CREATE INDEX idx_sale_payments_sale ON sale_payments(sale_id);
