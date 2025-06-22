-- Drop the old single interviewer column
ALTER TABLE interviews DROP COLUMN IF EXISTS interviewer_id;

-- Create the join table for many-to-many relationship
CREATE TABLE interview_interviewers (
    interview_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (interview_id, user_id),
    CONSTRAINT fk_interview FOREIGN KEY (interview_id) REFERENCES interviews(id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
); 