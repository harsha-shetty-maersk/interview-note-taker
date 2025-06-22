-- Add interviewer_id column to interviews table
ALTER TABLE interviews
ADD COLUMN interviewer_id BIGINT;

-- Add foreign key constraint
ALTER TABLE interviews
ADD CONSTRAINT fk_interviewer
FOREIGN KEY (interviewer_id) REFERENCES users(id);

-- Optionally, set interviewer_id to NULL for existing rows (or update as needed)
UPDATE interviews SET interviewer_id = NULL WHERE interviewer_id IS NULL; 