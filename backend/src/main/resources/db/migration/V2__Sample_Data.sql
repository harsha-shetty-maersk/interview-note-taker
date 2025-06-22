-- Insert sample interviewers
INSERT INTO interviewers (name, email, role, specializations) VALUES
('Jane Smith', 'jane.smith@company.com', 'Senior Software Engineer', ARRAY['Backend', 'System Design']),
('Bob Johnson', 'bob.johnson@company.com', 'Tech Lead', ARRAY['Frontend', 'Architecture']),
('Alice Brown', 'alice.brown@company.com', 'Engineering Manager', ARRAY['Leadership', 'Behavioral']),
('David Wilson', 'david.wilson@company.com', 'Principal Engineer', ARRAY['Backend', 'DevOps', 'System Design']),
('Sarah Davis', 'sarah.davis@company.com', 'Senior Frontend Engineer', ARRAY['Frontend', 'React', 'TypeScript']);

-- Insert sample evaluation criteria
INSERT INTO evaluation_criteria (round_type, criteria_name, description, weight) VALUES
-- Technical Round 1
('TECHNICAL_ROUND_1', 'Problem Solving', 'Ability to approach and solve complex problems', 0.3),
('TECHNICAL_ROUND_1', 'Coding Skills', 'Code quality, structure, and best practices', 0.4),
('TECHNICAL_ROUND_1', 'Algorithm Knowledge', 'Understanding of data structures and algorithms', 0.2),
('TECHNICAL_ROUND_1', 'Communication', 'Ability to explain technical concepts clearly', 0.1),

-- Technical Round 2
('TECHNICAL_ROUND_2', 'System Design', 'Ability to design scalable systems', 0.4),
('TECHNICAL_ROUND_2', 'Architecture Knowledge', 'Understanding of design patterns and principles', 0.3),
('TECHNICAL_ROUND_2', 'Technical Leadership', 'Ability to lead technical decisions', 0.2),
('TECHNICAL_ROUND_2', 'Communication', 'Ability to present technical solutions', 0.1),

-- Behavioral Round
('BEHAVIORAL_ROUND', 'Leadership', 'Demonstrated leadership qualities', 0.3),
('BEHAVIORAL_ROUND', 'Teamwork', 'Ability to work effectively in teams', 0.2),
('BEHAVIORAL_ROUND', 'Problem Solving', 'Approach to real-world problems', 0.2),
('BEHAVIORAL_ROUND', 'Cultural Fit', 'Alignment with company values', 0.3);

-- Insert sample candidates
INSERT INTO candidates (first_name, last_name, email, phone, position, experience, source, notes) VALUES
('John', 'Doe', 'john.doe@example.com', '+1234567890', 'Senior Software Engineer', 5, 'LINKEDIN', 'Strong background in Java and Spring Boot'),
('Emily', 'Johnson', 'emily.johnson@example.com', '+1234567891', 'Frontend Developer', 3, 'REFERRAL', 'Excellent React skills, good communication'),
('Michael', 'Brown', 'michael.brown@example.com', '+1234567892', 'Full Stack Developer', 4, 'LINKEDIN', 'Experience with both frontend and backend'),
('Lisa', 'Wilson', 'lisa.wilson@example.com', '+1234567893', 'DevOps Engineer', 6, 'JOB_BOARD', 'Strong infrastructure and automation skills'),
('Robert', 'Davis', 'robert.davis@example.com', '+1234567894', 'Software Engineer', 2, 'REFERRAL', 'Recent graduate, shows potential');

-- Insert sample interviews
INSERT INTO interviews (candidate_id, position, scheduled_date, duration, status, notes) VALUES
(1, 'Senior Software Engineer', '2024-01-20 14:00:00', 60, 'SCHEDULED', 'Technical interview focusing on system design'),
(2, 'Frontend Developer', '2024-01-21 10:00:00', 45, 'COMPLETED', 'Frontend technical assessment'),
(3, 'Full Stack Developer', '2024-01-22 15:00:00', 60, 'SCHEDULED', 'Comprehensive technical evaluation'),
(4, 'DevOps Engineer', '2024-01-23 11:00:00', 60, 'COMPLETED', 'Infrastructure and automation focus'),
(5, 'Software Engineer', '2024-01-24 13:00:00', 45, 'SCHEDULED', 'Entry-level technical assessment');

-- Insert sample interview rounds
INSERT INTO interview_rounds (interview_id, interviewer_id, round_type, scheduled_time, duration, status) VALUES
(1, 1, 'TECHNICAL_ROUND_1', '2024-01-20 14:00:00', 45, 'SCHEDULED'),
(1, 4, 'TECHNICAL_ROUND_2', '2024-01-20 15:00:00', 45, 'SCHEDULED'),
(1, 3, 'BEHAVIORAL_ROUND', '2024-01-21 10:00:00', 30, 'SCHEDULED'),
(2, 5, 'TECHNICAL_ROUND_1', '2024-01-21 10:00:00', 45, 'SCHEDULED'),
(2, 3, 'BEHAVIORAL_ROUND', '2024-01-21 11:00:00', 30, 'SCHEDULED'),
(3, 1, 'TECHNICAL_ROUND_1', '2024-01-22 15:00:00', 45, 'SCHEDULED'),
(3, 4, 'TECHNICAL_ROUND_2', '2024-01-22 16:00:00', 45, 'SCHEDULED'),
(4, 4, 'TECHNICAL_ROUND_1', '2024-01-23 11:00:00', 45, 'SCHEDULED'),
(4, 3, 'BEHAVIORAL_ROUND', '2024-01-23 12:00:00', 30, 'SCHEDULED'),
(5, 1, 'TECHNICAL_ROUND_1', '2024-01-24 13:00:00', 45, 'SCHEDULED'); 