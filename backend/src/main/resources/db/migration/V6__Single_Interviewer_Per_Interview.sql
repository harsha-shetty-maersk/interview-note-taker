-- Drop the many-to-many join table
DROP TABLE IF EXISTS interview_interviewers;

-- interviewer_id column already exists in interviews table (see V4__Add_Interviewer_To_Interviews.sql)
-- No further changes needed for single interviewer support 