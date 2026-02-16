-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create database if not exists (for local setup)
SELECT 'CREATE DATABASE lgdms'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lgdms')\gexec
