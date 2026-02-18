-- Create credit_accounts table
CREATE TABLE credit_accounts (
                                 id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 account_number VARCHAR(50) NOT NULL UNIQUE,
                                 customer_id VARCHAR(36) REFERENCES customers(id),
                                 location_id VARCHAR(36) REFERENCES locations(id),
                                 account_type VARCHAR(20) NOT NULL,
                                 credit_limit DECIMAL(19,2) NOT NULL DEFAULT 0,
                                 current_balance DECIMAL(19,2) NOT NULL DEFAULT 0,
                                 available_credit DECIMAL(19,2) GENERATED ALWAYS AS (credit_limit - current_balance) STORED,
                                 payment_terms VARCHAR(50),
                                 interest_rate DECIMAL(5,2),
                                 last_statement_date TIMESTAMP,
                                 next_due_date TIMESTAMP,
                                 is_active BOOLEAN DEFAULT TRUE,
                                 notes TEXT,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 created_by VARCHAR(50),
                                 updated_by VARCHAR(50),
                                 version BIGINT DEFAULT 0,
                                 CONSTRAINT check_customer_xor_location CHECK (
                                         (customer_id IS NOT NULL AND location_id IS NULL) OR
                                         (customer_id IS NULL AND location_id IS NOT NULL)
                                     )
);

-- Create credit_limits table (history of limit changes)
CREATE TABLE credit_limits (
                               id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                               customer_id VARCHAR(36) REFERENCES customers(id),
                               location_id VARCHAR(36) REFERENCES locations(id),
                               limit_amount DECIMAL(19,2) NOT NULL,
                               effective_from TIMESTAMP NOT NULL,
                               effective_to TIMESTAMP,
                               approved_by VARCHAR(100) NOT NULL,
                               approved_at TIMESTAMP,
                               reason VARCHAR(255),
                               is_current BOOLEAN DEFAULT TRUE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               created_by VARCHAR(50),
                               updated_by VARCHAR(50),
                               version BIGINT DEFAULT 0
);

-- Create credit_transactions table
CREATE TABLE credit_transactions (
                                     id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                                     transaction_number VARCHAR(50) NOT NULL UNIQUE,
                                     credit_account_id VARCHAR(36) NOT NULL REFERENCES credit_accounts(id),
                                     sale_id VARCHAR(36) REFERENCES sales(id),
                                     payment_id VARCHAR(36) REFERENCES payments(id),
                                     transaction_type VARCHAR(20) NOT NULL,
                                     amount DECIMAL(19,2) NOT NULL,
                                     balance_after DECIMAL(19,2) NOT NULL,
                                     description VARCHAR(255),
                                     transaction_date TIMESTAMP NOT NULL,
                                     status VARCHAR(20) DEFAULT 'COMPLETED',
                                     reference_number VARCHAR(100),
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     created_by VARCHAR(50),
                                     updated_by VARCHAR(50),
                                     version BIGINT DEFAULT 0
);

-- Create payments table
CREATE TABLE payments (
                          id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                          payment_number VARCHAR(50) NOT NULL UNIQUE,
                          location_id VARCHAR(36) NOT NULL REFERENCES locations(id),
                          customer_id VARCHAR(36) REFERENCES customers(id),
                          received_by VARCHAR(36) NOT NULL REFERENCES users(id),
                          payment_date TIMESTAMP NOT NULL,
                          payment_method VARCHAR(20) NOT NULL,
                          amount DECIMAL(19,2) NOT NULL,
                          reference_number VARCHAR(100),
                          bank_name VARCHAR(100),
                          cheque_date TIMESTAMP,
                          status VARCHAR(20) NOT NULL,
                          notes TEXT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          created_by VARCHAR(50),
                          updated_by VARCHAR(50),
                          version BIGINT DEFAULT 0
);

-- Create payment_allocations table
CREATE TABLE payment_allocations (
                                     id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                                     payment_id VARCHAR(36) NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
                                     sale_id VARCHAR(36) NOT NULL REFERENCES sales(id),
                                     credit_transaction_id VARCHAR(36) REFERENCES credit_transactions(id),
                                     allocated_amount DECIMAL(19,2) NOT NULL,
                                     allocation_date TIMESTAMP NOT NULL,
                                     notes VARCHAR(255),
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     created_by VARCHAR(50),
                                     updated_by VARCHAR(50),
                                     version BIGINT DEFAULT 0
);

-- Create cashbook_entries table
CREATE TABLE cashbook_entries (
                                  id VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  entry_number VARCHAR(50) NOT NULL UNIQUE,
                                  location_id VARCHAR(36) NOT NULL REFERENCES locations(id),
                                  entry_date TIMESTAMP NOT NULL,
                                  entry_type VARCHAR(20) NOT NULL,
                                  payment_method VARCHAR(20) NOT NULL,
                                  amount DECIMAL(19,2) NOT NULL,
                                  reference_type VARCHAR(50),
                                  reference_id VARCHAR(36),
                                  reference_number VARCHAR(100),
                                  description VARCHAR(255),
                                  created_by VARCHAR(100),
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  version BIGINT DEFAULT 0
);

-- Create indexes
CREATE INDEX idx_credit_accounts_customer ON credit_accounts(customer_id);
CREATE INDEX idx_credit_accounts_location ON credit_accounts(location_id);
CREATE INDEX idx_credit_accounts_type ON credit_accounts(account_type);

CREATE INDEX idx_credit_limits_customer ON credit_limits(customer_id);
CREATE INDEX idx_credit_limits_location ON credit_limits(location_id);
CREATE INDEX idx_credit_limits_current ON credit_limits(is_current);

CREATE INDEX idx_credit_transactions_account ON credit_transactions(credit_account_id);
CREATE INDEX idx_credit_transactions_sale ON credit_transactions(sale_id);
CREATE INDEX idx_credit_transactions_payment ON credit_transactions(payment_id);
CREATE INDEX idx_credit_transactions_date ON credit_transactions(transaction_date);

CREATE INDEX idx_payments_number ON payments(payment_number);
CREATE INDEX idx_payments_location ON payments(location_id);
CREATE INDEX idx_payments_customer ON payments(customer_id);
CREATE INDEX idx_payments_date ON payments(payment_date);
CREATE INDEX idx_payments_method ON payments(payment_method);
CREATE INDEX idx_payments_status ON payments(status);

CREATE INDEX idx_allocations_payment ON payment_allocations(payment_id);
CREATE INDEX idx_allocations_sale ON payment_allocations(sale_id);

CREATE INDEX idx_cashbook_location ON cashbook_entries(location_id);
CREATE INDEX idx_cashbook_date ON cashbook_entries(entry_date);
