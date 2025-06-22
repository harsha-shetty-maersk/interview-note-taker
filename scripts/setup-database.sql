-- Database setup script for Interview Notes application
-- Run this script as a PostgreSQL superuser (usually 'postgres')

-- Create the database (if it doesn't exist)
-- Note: PostgreSQL doesn't have CREATE DATABASE IF NOT EXISTS, so we'll handle this manually
-- You can run: createdb interview_notes (from command line) or use pgAdmin

-- Create the user (if it doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'interview_user') THEN
        CREATE USER interview_user WITH PASSWORD 'interview_password';
    END IF;
END
$$;

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON DATABASE interview_notes TO interview_user;

-- Connect to the interview_notes database and grant schema privileges
-- Note: You'll need to run this part after connecting to the interview_notes database
-- \c interview_notes
-- GRANT ALL ON SCHEMA public TO interview_user;
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO interview_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO interview_user; 