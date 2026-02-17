-- Insert default cylinder sizes (from SRS)
INSERT INTO cylinder_sizes (id, name, weight_kg, tare_weight_kg, display_order) VALUES
                                                                                    (uuid_generate_v4(), '3kg', 3.0, 3.5, 1),
                                                                                    (uuid_generate_v4(), '6kg', 6.0, 6.5, 2),
                                                                                    (uuid_generate_v4(), '15kg', 15.0, 15.5, 3),
                                                                                    (uuid_generate_v4(), '38kg', 38.0, 38.5, 4),
                                                                                    (uuid_generate_v4(), '100kg', 100.0, 100.5, 5);

-- Insert default supplier (Taifa Gas)
INSERT INTO suppliers (id, name, code, is_active) VALUES
    (uuid_generate_v4(), 'Taifa Gas', 'TAIFA001', true);

-- Insert sample price categories
DO $$
DECLARE
cyl_3kg_id VARCHAR(36);
    cyl_6kg_id VARCHAR(36);
    cyl_15kg_id VARCHAR(36);
BEGIN
SELECT id INTO cyl_3kg_id FROM cylinder_sizes WHERE name = '3kg';
SELECT id INTO cyl_6kg_id FROM cylinder_sizes WHERE name = '6kg';
SELECT id INTO cyl_15kg_id FROM cylinder_sizes WHERE name = '15kg';

-- Complete prices
INSERT INTO price_categories (name, cylinder_size_id, product_type, price, effective_from) VALUES
                                                                                               ('Retail - 3kg Complete', cyl_3kg_id, 'COMPLETE', 35000, CURRENT_DATE),
                                                                                               ('Retail - 6kg Complete', cyl_6kg_id, 'COMPLETE', 65000, CURRENT_DATE),
                                                                                               ('Retail - 15kg Complete', cyl_15kg_id, 'COMPLETE', 150000, CURRENT_DATE);

-- Refill prices
INSERT INTO price_categories (name, cylinder_size_id, product_type, price, effective_from) VALUES
                                                                                               ('Retail - 3kg Refill', cyl_3kg_id, 'REFILL', 25000, CURRENT_DATE),
                                                                                               ('Retail - 6kg Refill', cyl_6kg_id, 'REFILL', 50000, CURRENT_DATE),
                                                                                               ('Retail - 15kg Refill', cyl_15kg_id, 'REFILL', 120000, CURRENT_DATE);
END $$;
