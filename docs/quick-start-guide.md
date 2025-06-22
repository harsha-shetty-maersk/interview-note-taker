# Quick Start Guide

## Overview

This quick start guide will help you get the Interview Note-Taking Application up and running in the shortest time possible. Follow these steps to have a working application in under 30 minutes.

## Prerequisites

Before starting, ensure you have the following installed:

- **Java 17** or higher
- **Node.js 18** or higher
- **PostgreSQL 15** or higher
- **Docker** (optional, for containerized deployment)
- **Git**

## Step 1: Clone and Setup (5 minutes)

```bash
# Clone the repository
git clone <your-repository-url>
cd interviewNoteTaker

# Create the project structure
mkdir -p backend/src/main/java/com/interviewnotes
mkdir -p backend/src/main/resources
mkdir -p frontend/src
mkdir -p k8s
mkdir -p docker
mkdir -p docs
```

## Step 2: Backend Setup (10 minutes)

### 2.1 Create Spring Boot Project

Create `backend/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.interviewnotes</groupId>
    <artifactId>interview-notes-backend</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <!-- PDF Generation -->
        <dependency>
            <groupId>com.itextpdf</groupId>
            <artifactId>itext7-core</artifactId>
            <version>7.2.5</version>
        </dependency>

        <!-- Development Tools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2.2 Create Application Configuration

Create `backend/src/main/resources/application.yml`:

```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/interview_notes
    username: interview_user
    password: interview_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  flyway:
    enabled: true
    baseline-on-migrate: true

server:
  port: 8080
  servlet:
    context-path: /api

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.interviewnotes: DEBUG
    org.springframework.web: DEBUG
```

### 2.3 Create Main Application Class

Create `backend/src/main/java/com/interviewnotes/InterviewNotesApplication.java`:

```java
package com.interviewnotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InterviewNotesApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterviewNotesApplication.class, args);
    }
}
```

### 2.4 Create Basic Entity

Create `backend/src/main/java/com/interviewnotes/model/Candidate.java`:

```java
package com.interviewnotes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String position;

    private Integer experience;

    @Column(name = "resume_url")
    private String resumeUrl;

    private String source;

    private String notes;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

### 2.5 Create Repository

Create `backend/src/main/java/com/interviewnotes/repository/CandidateRepository.java`:

```java
package com.interviewnotes.repository;

import com.interviewnotes.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByEmail(String email);
    List<Candidate> findByStatus(String status);
    List<Candidate> findByPositionContainingIgnoreCase(String position);
}
```

### 2.6 Create Controller

Create `backend/src/main/java/com/interviewnotes/controller/CandidateController.java`:

```java
package com.interviewnotes.controller;

import com.interviewnotes.model.Candidate;
import com.interviewnotes.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidates")
@CrossOrigin(origins = "*")
public class CandidateController {

    @Autowired
    private CandidateRepository candidateRepository;

    @GetMapping
    public ResponseEntity<List<Candidate>> getAllCandidates() {
        List<Candidate> candidates = candidateRepository.findAll();
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Candidate> getCandidateById(@PathVariable Long id) {
        return candidateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Candidate> createCandidate(@RequestBody Candidate candidate) {
        Candidate savedCandidate = candidateRepository.save(candidate);
        return ResponseEntity.ok(savedCandidate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Candidate> updateCandidate(@PathVariable Long id, @RequestBody Candidate candidate) {
        return candidateRepository.findById(id)
                .map(existingCandidate -> {
                    candidate.setId(id);
                    return ResponseEntity.ok(candidateRepository.save(candidate));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        return candidateRepository.findById(id)
                .map(candidate -> {
                    candidateRepository.delete(candidate);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
```

## Step 3: Database Setup (5 minutes)

### 3.1 Create Database

```sql
-- Connect to PostgreSQL and run:
CREATE DATABASE interview_notes;
CREATE USER interview_user WITH PASSWORD 'interview_password';
GRANT ALL PRIVILEGES ON DATABASE interview_notes TO interview_user;
```

### 3.2 Create Migration Script

Create `backend/src/main/resources/db/migration/V1__Initial_Schema.sql`:

```sql
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

-- Create indexes
CREATE INDEX idx_candidates_email ON candidates(email);
CREATE INDEX idx_candidates_status ON candidates(status);
CREATE INDEX idx_candidates_position ON candidates(position);
```

## Step 4: Frontend Setup (10 minutes)

### 4.1 Create React App

```bash
# Navigate to frontend directory
cd frontend

# Create React app with TypeScript
npx create-react-app . --template typescript

# Install additional dependencies
npm install @mui/material @emotion/react @emotion/styled
npm install @mui/icons-material
npm install axios
npm install react-router-dom
npm install @types/react-router-dom
```

### 4.2 Create Basic Components

Create `frontend/src/components/CandidateList.tsx`:

```tsx
import React, { useState, useEffect } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Button
} from '@mui/material';
import axios from 'axios';

interface Candidate {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  position: string;
  status: string;
}

const CandidateList: React.FC = () => {
  const [candidates, setCandidates] = useState<Candidate[]>([]);

  useEffect(() => {
    fetchCandidates();
  }, []);

  const fetchCandidates = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/candidates');
      setCandidates(response.data);
    } catch (error) {
      console.error('Error fetching candidates:', error);
    }
  };

  return (
    <div>
      <Typography variant="h4" gutterBottom>
        Candidates
      </Typography>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Position</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {candidates.map((candidate) => (
              <TableRow key={candidate.id}>
                <TableCell>{`${candidate.firstName} ${candidate.lastName}`}</TableCell>
                <TableCell>{candidate.email}</TableCell>
                <TableCell>{candidate.position}</TableCell>
                <TableCell>{candidate.status}</TableCell>
                <TableCell>
                  <Button variant="outlined" size="small">
                    View
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
};

export default CandidateList;
```

### 4.3 Update App Component

Update `frontend/src/App.tsx`:

```tsx
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Container, CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import CandidateList from './components/CandidateList';

const theme = createTheme();

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
          <Routes>
            <Route path="/" element={<CandidateList />} />
          </Routes>
        </Container>
      </Router>
    </ThemeProvider>
  );
}

export default App;
```

## Step 5: Test the Application (5 minutes)

### 5.1 Start Backend

```bash
# Navigate to backend directory
cd backend

# Build and run the application
mvn spring-boot:run
```

### 5.2 Start Frontend

```bash
# In a new terminal, navigate to frontend directory
cd frontend

# Start the development server
npm start
```

### 5.3 Test API

```bash
# Test the API endpoints
curl http://localhost:8080/api/candidates

# Create a test candidate
curl -X POST http://localhost:8080/api/candidates \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "position": "Software Engineer",
    "experience": 5
  }'
```

## Step 6: Verify Everything Works

1. **Backend**: Visit `http://localhost:8080/api/actuator/health` - should return healthy status
2. **Frontend**: Visit `http://localhost:3000` - should show the candidate list
3. **Database**: Check that the `candidates` table was created in PostgreSQL

## Next Steps

Now that you have a basic working application, you can:

1. **Add More Entities**: Follow the same pattern to add Interview, InterviewRound, and InterviewNotes entities
2. **Enhance UI**: Add forms for creating and editing candidates
3. **Add Authentication**: Implement JWT-based authentication
4. **Deploy**: Use the Docker and Kubernetes configurations from the deployment guide

## Troubleshooting

### Common Issues

1. **Database Connection Error**:
   - Ensure PostgreSQL is running
   - Check database credentials in `application.yml`
   - Verify database and user exist

2. **Port Already in Use**:
   - Change the port in `application.yml` or kill the process using the port

3. **CORS Issues**:
   - The backend already includes `@CrossOrigin(origins = "*")` for development
   - For production, configure specific origins

4. **Build Errors**:
   - Ensure Java 17+ is installed and set as JAVA_HOME
   - Run `mvn clean install` to rebuild

### Getting Help

- Check the detailed documentation in the `docs/` directory
- Review the step-by-step guide for comprehensive implementation
- Use the implementation checklist to track progress

## Production Deployment

Once you're ready for production:

1. Follow the deployment guide in `docs/deployment-guide.md`
2. Use the Docker configurations in the `docker/` directory
3. Deploy to Kubernetes using the manifests in the `k8s/` directory
4. Set up monitoring and logging as described in the documentation

This quick start guide gives you a working foundation that you can build upon to create a full-featured interview note-taking application. 