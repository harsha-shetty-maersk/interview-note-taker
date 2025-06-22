-- Purge all data from all tables (order matters due to foreign keys)
DELETE FROM interview_notes;
DELETE FROM interviews;
DELETE FROM candidates;
DELETE FROM users; 