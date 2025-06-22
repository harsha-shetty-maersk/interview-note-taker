-- Create custom enums for the application
CREATE TYPE interview_status AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE round_status AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE recommendation_type AS ENUM ('STRONG_HIRE', 'HIRE', 'WEAK_HIRE', 'NO_HIRE');
CREATE TYPE round_type AS ENUM ('TECHNICAL_ROUND_1', 'TECHNICAL_ROUND_2', 'BEHAVIORAL_ROUND');

-- Create candidates table
CREATE TABLE candidates (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    position VARCHAR(200) NOT NULL,
    experience INTEGER,
    resume_url TEXT,
    source VARCHAR(50),
    notes TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create interviewers table
CREATE TABLE interviewers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(200),
    specializations TEXT[],
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create interviews table
CREATE TABLE interviews (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    position VARCHAR(200) NOT NULL,
    scheduled_date TIMESTAMP NOT NULL,
    duration INTEGER NOT NULL,
    status VARCHAR(32) DEFAULT 'SCHEDULED',
    notes TEXT,
    overall_score DECIMAL(3,1),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create interview_rounds table
CREATE TABLE interview_rounds (
    id BIGSERIAL PRIMARY KEY,
    interview_id BIGINT NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    interviewer_id BIGINT NOT NULL REFERENCES interviewers(id),
    round_type round_type NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,
    duration INTEGER NOT NULL,
    status round_status DEFAULT 'SCHEDULED',
    notes TEXT,
    overall_score DECIMAL(3,1),
    recommendation recommendation_type,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create interview_notes table
CREATE TABLE interview_notes (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES interview_rounds(id) ON DELETE CASCADE,
    technical_notes TEXT,
    coding_notes TEXT,
    communication_notes TEXT,
    strengths TEXT[],
    weaknesses TEXT[],
    overall_score DECIMAL(3,1),
    recommendation recommendation_type,
    feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create interview_questions table
CREATE TABLE interview_questions (
    id BIGSERIAL PRIMARY KEY,
    notes_id BIGINT NOT NULL REFERENCES interview_notes(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    response TEXT,
    score INTEGER CHECK (score >= 1 AND score <= 10),
    notes TEXT,
    question_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create evaluation_criteria table
CREATE TABLE evaluation_criteria (
    id BIGSERIAL PRIMARY KEY,
    round_type round_type NOT NULL,
    criteria_name VARCHAR(100) NOT NULL,
    description TEXT,
    weight DECIMAL(3,2) DEFAULT 1.0,
    max_score INTEGER DEFAULT 10,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create round_evaluations table
CREATE TABLE round_evaluations (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES interview_rounds(id) ON DELETE CASCADE,
    criteria_id BIGINT NOT NULL REFERENCES evaluation_criteria(id),
    score INTEGER CHECK (score >= 1 AND score <= 10),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_candidates_email ON candidates(email);
CREATE INDEX idx_candidates_status ON candidates(status);
CREATE INDEX idx_candidates_position ON candidates(position);
CREATE INDEX idx_candidates_created_at ON candidates(created_at);

CREATE INDEX idx_interviewers_email ON interviewers(email);
CREATE INDEX idx_interviewers_is_active ON interviewers(is_active);

CREATE INDEX idx_interviews_candidate_id ON interviews(candidate_id);
CREATE INDEX idx_interviews_status ON interviews(status);
CREATE INDEX idx_interviews_scheduled_date ON interviews(scheduled_date);
CREATE INDEX idx_interviews_created_at ON interviews(created_at);

CREATE INDEX idx_interview_rounds_interview_id ON interview_rounds(interview_id);
CREATE INDEX idx_interview_rounds_interviewer_id ON interview_rounds(interviewer_id);
CREATE INDEX idx_interview_rounds_round_type ON interview_rounds(round_type);
CREATE INDEX idx_interview_rounds_status ON interview_rounds(status);
CREATE INDEX idx_interview_rounds_scheduled_time ON interview_rounds(scheduled_time);

CREATE INDEX idx_interview_notes_round_id ON interview_notes(round_id);
CREATE INDEX idx_interview_notes_overall_score ON interview_notes(overall_score);

CREATE INDEX idx_interview_questions_notes_id ON interview_questions(notes_id);
CREATE INDEX idx_interview_questions_question_type ON interview_questions(question_type);

CREATE INDEX idx_evaluation_criteria_round_type ON evaluation_criteria(round_type);
CREATE INDEX idx_evaluation_criteria_is_active ON evaluation_criteria(is_active);

CREATE INDEX idx_round_evaluations_round_id ON round_evaluations(round_id);
CREATE INDEX idx_round_evaluations_criteria_id ON round_evaluations(criteria_id);

-- Create composite indexes for common query patterns
CREATE INDEX idx_interviews_candidate_status ON interviews(candidate_id, status);
CREATE INDEX idx_rounds_interview_type_status ON interview_rounds(interview_id, round_type, status);
CREATE INDEX idx_notes_round_score ON interview_notes(round_id, overall_score);

-- Create partial indexes for active records
CREATE INDEX idx_candidates_active ON candidates(id) WHERE status = 'ACTIVE';
CREATE INDEX idx_interviewers_active ON interviewers(id) WHERE is_active = true; 