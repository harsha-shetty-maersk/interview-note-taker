# Database Schema Documentation

## Overview

The interview note-taking application uses PostgreSQL as the primary database with the following key entities:

- **Candidates** - Basic candidate information
- **Interviews** - Interview sessions
- **InterviewRounds** - Individual interview rounds
- **InterviewNotes** - Detailed notes and feedback
- **Interviewers** - Interviewer profiles
- **EvaluationCriteria** - Scoring templates

## Entity Relationship Diagram

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Candidates │────▶│  Interviews │────▶│InterviewRounds│
└─────────────┘     └─────────────┘     └─────────────┘
                            │                     │
                            │                     │
                            ▼                     ▼
                    ┌─────────────┐     ┌─────────────┐
                    │Interviewers │     │InterviewNotes│
                    └─────────────┘     └─────────────┘
                                               │
                                               │
                                               ▼
                                    ┌─────────────┐
                                    │Evaluation   │
                                    │Criteria     │
                                    └─────────────┘
```

## Table Definitions

### 1. candidates

Stores basic candidate information.

```sql
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

-- Indexes
CREATE INDEX idx_candidates_email ON candidates(email);
CREATE INDEX idx_candidates_status ON candidates(status);
CREATE INDEX idx_candidates_position ON candidates(position);
CREATE INDEX idx_candidates_created_at ON candidates(created_at);
```

### 2. interviews

Stores interview session information.

```sql
CREATE TABLE interviews (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT NOT NULL REFERENCES candidates(id),
    position VARCHAR(200) NOT NULL,
    scheduled_date TIMESTAMP NOT NULL,
    duration INTEGER NOT NULL, -- in minutes
    interview_type VARCHAR(20) NOT NULL, -- VIRTUAL, IN_PERSON, PHONE
    status VARCHAR(20) DEFAULT 'SCHEDULED', -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    notes TEXT,
    overall_score DECIMAL(3,1),
    final_recommendation VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_interviews_candidate_id ON interviews(candidate_id);
CREATE INDEX idx_interviews_status ON interviews(status);
CREATE INDEX idx_interviews_scheduled_date ON interviews(scheduled_date);
CREATE INDEX idx_interviews_created_at ON interviews(created_at);
```

### 3. interviewers

Stores interviewer information.

```sql
CREATE TABLE interviewers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(200),
    specializations TEXT[], -- Array of specializations
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_interviewers_email ON interviewers(email);
CREATE INDEX idx_interviewers_is_active ON interviewers(is_active);
```

### 4. interview_rounds

Stores individual interview round information.

```sql
CREATE TABLE interview_rounds (
    id BIGSERIAL PRIMARY KEY,
    interview_id BIGINT NOT NULL REFERENCES interviews(id),
    interviewer_id BIGINT NOT NULL REFERENCES interviewers(id),
    round_type VARCHAR(30) NOT NULL, -- TECHNICAL_ROUND_1, TECHNICAL_ROUND_2, BEHAVIORAL_ROUND
    scheduled_time TIMESTAMP NOT NULL,
    duration INTEGER NOT NULL, -- in minutes
    status VARCHAR(20) DEFAULT 'SCHEDULED', -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    notes TEXT,
    overall_score DECIMAL(3,1),
    recommendation VARCHAR(20), -- STRONG_HIRE, HIRE, WEAK_HIRE, NO_HIRE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_interview_rounds_interview_id ON interview_rounds(interview_id);
CREATE INDEX idx_interview_rounds_interviewer_id ON interview_rounds(interviewer_id);
CREATE INDEX idx_interview_rounds_round_type ON interview_rounds(round_type);
CREATE INDEX idx_interview_rounds_status ON interview_rounds(status);
CREATE INDEX idx_interview_rounds_scheduled_time ON interview_rounds(scheduled_time);
```

### 5. interview_notes

Stores detailed notes and feedback for each round.

```sql
CREATE TABLE interview_notes (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES interview_rounds(id),
    technical_notes TEXT,
    coding_notes TEXT,
    communication_notes TEXT,
    strengths TEXT[], -- Array of strengths
    weaknesses TEXT[], -- Array of weaknesses
    overall_score DECIMAL(3,1),
    recommendation VARCHAR(20),
    feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_interview_notes_round_id ON interview_notes(round_id);
CREATE INDEX idx_interview_notes_overall_score ON interview_notes(overall_score);
```

### 6. interview_questions

Stores individual questions and responses during interviews.

```sql
CREATE TABLE interview_questions (
    id BIGSERIAL PRIMARY KEY,
    notes_id BIGINT NOT NULL REFERENCES interview_notes(id),
    question TEXT NOT NULL,
    response TEXT,
    score INTEGER CHECK (score >= 1 AND score <= 10),
    notes TEXT,
    question_type VARCHAR(50), -- CODING, SYSTEM_DESIGN, BEHAVIORAL, etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_interview_questions_notes_id ON interview_questions(notes_id);
CREATE INDEX idx_interview_questions_question_type ON interview_questions(question_type);
```

### 7. evaluation_criteria

Stores scoring criteria templates for different round types.

```sql
CREATE TABLE evaluation_criteria (
    id BIGSERIAL PRIMARY KEY,
    round_type VARCHAR(30) NOT NULL,
    criteria_name VARCHAR(100) NOT NULL,
    description TEXT,
    weight DECIMAL(3,2) DEFAULT 1.0, -- Weight in overall scoring
    max_score INTEGER DEFAULT 10,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_evaluation_criteria_round_type ON evaluation_criteria(round_type);
CREATE INDEX idx_evaluation_criteria_is_active ON evaluation_criteria(is_active);
```

### 8. round_evaluations

Stores detailed evaluations for each criterion.

```sql
CREATE TABLE round_evaluations (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES interview_rounds(id),
    criteria_id BIGINT NOT NULL REFERENCES evaluation_criteria(id),
    score INTEGER CHECK (score >= 1 AND score <= 10),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_round_evaluations_round_id ON round_evaluations(round_id);
CREATE INDEX idx_round_evaluations_criteria_id ON round_evaluations(criteria_id);
```

## Enums and Constraints

### Status Enums

```sql
-- Interview Status
CREATE TYPE interview_status AS ENUM (
    'SCHEDULED',
    'IN_PROGRESS', 
    'COMPLETED',
    'CANCELLED'
);

-- Round Status
CREATE TYPE round_status AS ENUM (
    'SCHEDULED',
    'IN_PROGRESS',
    'COMPLETED', 
    'CANCELLED'
);

-- Recommendation Types
CREATE TYPE recommendation_type AS ENUM (
    'STRONG_HIRE',
    'HIRE',
    'WEAK_HIRE',
    'NO_HIRE'
);

-- Round Types
CREATE TYPE round_type AS ENUM (
    'TECHNICAL_ROUND_1',
    'TECHNICAL_ROUND_2',
    'BEHAVIORAL_ROUND'
);

-- Interview Types
CREATE TYPE interview_type AS ENUM (
    'VIRTUAL',
    'IN_PERSON',
    'PHONE'
);
```

## Sample Data

### Insert Sample Interviewers

```sql
INSERT INTO interviewers (name, email, role, specializations) VALUES
('Jane Smith', 'jane.smith@company.com', 'Senior Software Engineer', ARRAY['Backend', 'System Design']),
('Bob Johnson', 'bob.johnson@company.com', 'Tech Lead', ARRAY['Frontend', 'Architecture']),
('Alice Brown', 'alice.brown@company.com', 'Engineering Manager', ARRAY['Leadership', 'Behavioral']);
```

### Insert Sample Evaluation Criteria

```sql
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
```

## Migration Scripts

### V1__Initial_Schema.sql

```sql
-- Create enums
CREATE TYPE interview_status AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE round_status AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE recommendation_type AS ENUM ('STRONG_HIRE', 'HIRE', 'WEAK_HIRE', 'NO_HIRE');
CREATE TYPE round_type AS ENUM ('TECHNICAL_ROUND_1', 'TECHNICAL_ROUND_2', 'BEHAVIORAL_ROUND');
CREATE TYPE interview_type AS ENUM ('VIRTUAL', 'IN_PERSON', 'PHONE');

-- Create tables
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

CREATE TABLE interviews (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT NOT NULL REFERENCES candidates(id),
    position VARCHAR(200) NOT NULL,
    scheduled_date TIMESTAMP NOT NULL,
    duration INTEGER NOT NULL,
    interview_type interview_type NOT NULL,
    status interview_status DEFAULT 'SCHEDULED',
    notes TEXT,
    overall_score DECIMAL(3,1),
    final_recommendation recommendation_type,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE interview_rounds (
    id BIGSERIAL PRIMARY KEY,
    interview_id BIGINT NOT NULL REFERENCES interviews(id),
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

CREATE TABLE interview_notes (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES interview_rounds(id),
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

CREATE TABLE interview_questions (
    id BIGSERIAL PRIMARY KEY,
    notes_id BIGINT NOT NULL REFERENCES interview_notes(id),
    question TEXT NOT NULL,
    response TEXT,
    score INTEGER CHECK (score >= 1 AND score <= 10),
    notes TEXT,
    question_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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

CREATE TABLE round_evaluations (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES interview_rounds(id),
    criteria_id BIGINT NOT NULL REFERENCES evaluation_criteria(id),
    score INTEGER CHECK (score >= 1 AND score <= 10),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
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
```

### V2__Sample_Data.sql

```sql
-- Insert sample interviewers
INSERT INTO interviewers (name, email, role, specializations) VALUES
('Jane Smith', 'jane.smith@company.com', 'Senior Software Engineer', ARRAY['Backend', 'System Design']),
('Bob Johnson', 'bob.johnson@company.com', 'Tech Lead', ARRAY['Frontend', 'Architecture']),
('Alice Brown', 'alice.brown@company.com', 'Engineering Manager', ARRAY['Leadership', 'Behavioral']);

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
```

## Performance Considerations

### Query Optimization

1. **Composite Indexes** for frequently used query combinations
2. **Partial Indexes** for filtered queries
3. **Covering Indexes** to avoid table lookups

### Recommended Indexes

```sql
-- Composite indexes for common query patterns
CREATE INDEX idx_interviews_candidate_status ON interviews(candidate_id, status);
CREATE INDEX idx_rounds_interview_type_status ON interview_rounds(interview_id, round_type, status);
CREATE INDEX idx_notes_round_score ON interview_notes(round_id, overall_score);

-- Partial indexes for active records
CREATE INDEX idx_candidates_active ON candidates(id) WHERE status = 'ACTIVE';
CREATE INDEX idx_interviewers_active ON interviewers(id) WHERE is_active = true;
```

### Partitioning Strategy

For high-volume applications, consider partitioning by date:

```sql
-- Partition interviews table by month
CREATE TABLE interviews_2024_01 PARTITION OF interviews
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE interviews_2024_02 PARTITION OF interviews
FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
```

## Backup and Recovery

### Backup Strategy

1. **Full Backup**: Daily full database backup
2. **Incremental Backup**: Hourly incremental backups
3. **WAL Archiving**: Continuous WAL archiving for point-in-time recovery

### Recovery Procedures

1. **Point-in-Time Recovery**: Restore to any specific timestamp
2. **Disaster Recovery**: Cross-region backup replication
3. **Data Retention**: 7 years for compliance requirements

## Security Considerations

1. **Row Level Security (RLS)** for multi-tenant scenarios
2. **Encryption at rest** for sensitive data
3. **Audit logging** for all data modifications
4. **Connection encryption** using SSL/TLS 