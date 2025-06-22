-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'INTERVIEWER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on username for faster lookups
CREATE INDEX idx_users_username ON users(username);

-- Create index on email for faster lookups
CREATE INDEX idx_users_email ON users(email);

-- Add sample admin user (password: admin123)
INSERT INTO users (username, email, password, first_name, last_name, role, enabled)
VALUES (
    'admin',
    'admin@interviewnotes.com',
    '$2a$10$Eog3Gr/IrEgEu5SyM.lvjeUprrPeFYEflH5P2IBbi481SZjFRvqVO', -- admin123
    'Admin',
    'User',
    'ADMIN',
    true
);

-- Add sample interviewer user (password: interviewer123)
INSERT INTO users (username, email, password, first_name, last_name, role, enabled)
VALUES (
    'interviewer',
    'interviewer@interviewnotes.com',
    '$2a$10$kPALZaz1mKEcqxBWYJERTeQWgIpAWekwDQ8wD5aYpPN6ofrt5HFSW', -- interviewer123
    'John',
    'Interviewer',
    'INTERVIEWER',
    true
); 