-- Add interviewer_id column if it doesn't exist
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS interviewer_id BIGINT;

-- Drop the constraint if it exists (ignore errors if it doesn't)
DO $$
BEGIN
    ALTER TABLE interviews DROP CONSTRAINT IF EXISTS fk_interviewer;
EXCEPTION
    WHEN undefined_object THEN NULL;
END $$;

-- Now add the constraint
ALTER TABLE interviews
  ADD CONSTRAINT fk_interviewer
  FOREIGN KEY (interviewer_id)
  REFERENCES users(id);

-- Drop the old many-to-many join table if it exists
DROP TABLE IF EXISTS interview_interviewers; 