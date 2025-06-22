-- Insert default admin user (username: admin, password: admin123)
INSERT INTO users (username, password, email, first_name, last_name, role, enabled, created_at, updated_at)
VALUES (
  'admin',
  '$2a$10$7EqJtq98hPqEX7fNZaFWoO5rG6WzQ4b6h1uZ8g8y6Y4F5QJ5oG8Fe', -- bcrypt for 'admin123'
  'admin@example.com',
  'Admin',
  'User',
  'ADMIN',
  true,
  NOW(),
  NOW()
); 