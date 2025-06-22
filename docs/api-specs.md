# API Specifications

## Base URL
```
http://localhost:8080/api/v1
```

## Authentication
All API endpoints require authentication using JWT tokens in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

## Common Response Format

### Success Response
```json
{
  "success": true,
  "data": {},
  "message": "Operation completed successfully",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "email",
        "message": "Email is required"
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 1. Candidate Management APIs

### 1.1 Create Candidate
**POST** `/candidates`

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "position": "Software Engineer",
  "experience": 5,
  "resumeUrl": "https://example.com/resume.pdf",
  "source": "LINKEDIN",
  "notes": "Strong background in Java and Spring Boot"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "position": "Software Engineer",
    "experience": 5,
    "resumeUrl": "https://example.com/resume.pdf",
    "source": "LINKEDIN",
    "notes": "Strong background in Java and Spring Boot",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

### 1.2 Get All Candidates
**GET** `/candidates?page=0&size=10&sort=createdAt,desc`

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 10, max: 100)
- `sort`: Sort field and direction (default: createdAt,desc)
- `search`: Search term for name or email
- `status`: Filter by candidate status

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "position": "Software Engineer",
        "status": "ACTIVE",
        "createdAt": "2024-01-15T10:30:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "currentPage": 0,
    "size": 10
  }
}
```

### 1.3 Get Candidate by ID
**GET** `/candidates/{id}`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "position": "Software Engineer",
    "experience": 5,
    "resumeUrl": "https://example.com/resume.pdf",
    "source": "LINKEDIN",
    "notes": "Strong background in Java and Spring Boot",
    "status": "ACTIVE",
    "interviews": [
      {
        "id": 1,
        "status": "IN_PROGRESS",
        "createdAt": "2024-01-15T10:30:00Z"
      }
    ],
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

## 2. Interview Management APIs

### 2.1 Create Interview
**POST** `/interviews`

**Request Body:**
```json
{
  "candidateId": 1,
  "position": "Senior Software Engineer",
  "scheduledDate": "2024-01-20T14:00:00Z",
  "duration": 60,
  "interviewType": "VIRTUAL",
  "notes": "Technical interview focusing on system design"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "candidateId": 1,
    "position": "Senior Software Engineer",
    "scheduledDate": "2024-01-20T14:00:00Z",
    "duration": 60,
    "interviewType": "VIRTUAL",
    "status": "SCHEDULED",
    "notes": "Technical interview focusing on system design",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

### 2.2 Get Interview Details
**GET** `/interviews/{id}`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "candidate": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "position": "Software Engineer"
    },
    "position": "Senior Software Engineer",
    "scheduledDate": "2024-01-20T14:00:00Z",
    "duration": 60,
    "interviewType": "VIRTUAL",
    "status": "IN_PROGRESS",
    "notes": "Technical interview focusing on system design",
    "rounds": [
      {
        "id": 1,
        "roundType": "TECHNICAL_ROUND_1",
        "status": "COMPLETED",
        "interviewer": "Jane Smith",
        "scheduledTime": "2024-01-20T14:00:00Z",
        "duration": 45
      }
    ],
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

### 2.3 Update Interview Status
**PUT** `/interviews/{id}/status`

**Request Body:**
```json
{
  "status": "IN_PROGRESS"
}
```

## 3. Interview Round APIs

### 3.1 Create Interview Round
**POST** `/interviews/{interviewId}/rounds`

**Request Body:**
```json
{
  "roundType": "TECHNICAL_ROUND_1",
  "interviewerId": 1,
  "scheduledTime": "2024-01-20T14:00:00Z",
  "duration": 45,
  "notes": "Focus on DSA and coding skills"
}
```

**Round Types:**
- `TECHNICAL_ROUND_1`: Problem solving, DSA, coding
- `TECHNICAL_ROUND_2`: LLD, HLD, system design
- `BEHAVIORAL_ROUND`: Behavioral assessment

### 3.2 Get Round Details
**GET** `/rounds/{roundId}`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "interviewId": 1,
    "roundType": "TECHNICAL_ROUND_1",
    "interviewer": {
      "id": 1,
      "name": "Jane Smith",
      "email": "jane.smith@company.com"
    },
    "scheduledTime": "2024-01-20T14:00:00Z",
    "duration": 45,
    "status": "COMPLETED",
    "notes": "Focus on DSA and coding skills",
    "evaluation": {
      "overallScore": 8.5,
      "problemSolving": 9,
      "codingSkills": 8,
      "communication": 8,
      "recommendation": "STRONG_HIRE"
    },
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

## 4. Interview Notes APIs

### 4.1 Create/Update Notes
**POST** `/rounds/{roundId}/notes`

**Request Body:**
```json
{
  "technicalNotes": "Strong problem-solving skills. Solved the binary tree problem efficiently.",
  "codingNotes": "Clean code structure. Good understanding of time complexity.",
  "communicationNotes": "Clear communication. Explained thought process well.",
  "strengths": ["Problem solving", "Clean code", "Communication"],
  "weaknesses": ["Could improve on edge cases"],
  "questions": [
    {
      "question": "Implement a binary search tree",
      "response": "Implemented correctly with proper time complexity",
      "score": 9
    }
  ],
  "overallScore": 8.5,
  "recommendation": "STRONG_HIRE",
  "feedback": "Excellent candidate with strong technical skills"
}
```

### 4.2 Get Round Notes
**GET** `/rounds/{roundId}/notes`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "roundId": 1,
    "technicalNotes": "Strong problem-solving skills. Solved the binary tree problem efficiently.",
    "codingNotes": "Clean code structure. Good understanding of time complexity.",
    "communicationNotes": "Clear communication. Explained thought process well.",
    "strengths": ["Problem solving", "Clean code", "Communication"],
    "weaknesses": ["Could improve on edge cases"],
    "questions": [
      {
        "id": 1,
        "question": "Implement a binary search tree",
        "response": "Implemented correctly with proper time complexity",
        "score": 9,
        "notes": "Good implementation"
      }
    ],
    "overallScore": 8.5,
    "recommendation": "STRONG_HIRE",
    "feedback": "Excellent candidate with strong technical skills",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

## 5. Report Generation APIs

### 5.1 Generate Interview Report
**GET** `/interviews/{interviewId}/report?format=pdf`

**Query Parameters:**
- `format`: Report format (pdf, html) - default: pdf
- `includeNotes`: Include detailed notes (true/false) - default: true
- `includeScores`: Include scoring breakdown (true/false) - default: true

**Response:**
- For PDF: Returns PDF file with `Content-Type: application/pdf`
- For HTML: Returns JSON with HTML content

### 5.2 Get Report Preview
**GET** `/interviews/{interviewId}/report/preview`

**Response:**
```json
{
  "success": true,
  "data": {
    "candidate": {
      "name": "John Doe",
      "position": "Senior Software Engineer",
      "email": "john.doe@example.com"
    },
    "interview": {
      "scheduledDate": "2024-01-20T14:00:00Z",
      "status": "COMPLETED",
      "overallScore": 8.2
    },
    "rounds": [
      {
        "roundType": "TECHNICAL_ROUND_1",
        "score": 8.5,
        "recommendation": "STRONG_HIRE",
        "interviewer": "Jane Smith"
      },
      {
        "roundType": "TECHNICAL_ROUND_2",
        "score": 8.0,
        "recommendation": "HIRE",
        "interviewer": "Bob Johnson"
      }
    ],
    "summary": {
      "totalRounds": 2,
      "averageScore": 8.25,
      "finalRecommendation": "STRONG_HIRE"
    }
  }
}
```

## 6. Interviewer Management APIs

### 6.1 Get All Interviewers
**GET** `/interviewers`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Jane Smith",
      "email": "jane.smith@company.com",
      "role": "Senior Software Engineer",
      "specializations": ["Backend", "System Design"],
      "activeInterviews": 3
    }
  ]
}
```

### 6.2 Get Interviewer Schedule
**GET** `/interviewers/{id}/schedule?date=2024-01-20`

**Response:**
```json
{
  "success": true,
  "data": {
    "interviewer": {
      "id": 1,
      "name": "Jane Smith"
    },
    "schedule": [
      {
        "id": 1,
        "candidateName": "John Doe",
        "roundType": "TECHNICAL_ROUND_1",
        "scheduledTime": "2024-01-20T14:00:00Z",
        "duration": 45,
        "status": "SCHEDULED"
      }
    ]
  }
}
```

## 7. Analytics APIs

### 7.1 Get Interview Statistics
**GET** `/analytics/interviews?period=month`

**Query Parameters:**
- `period`: Time period (week, month, quarter, year)
- `startDate`: Start date (ISO format)
- `endDate`: End date (ISO format)

**Response:**
```json
{
  "success": true,
  "data": {
    "totalInterviews": 45,
    "completedInterviews": 42,
    "averageScore": 7.8,
    "hireRate": 0.67,
    "roundBreakdown": {
      "TECHNICAL_ROUND_1": 45,
      "TECHNICAL_ROUND_2": 38,
      "BEHAVIORAL_ROUND": 25
    },
    "recommendationBreakdown": {
      "STRONG_HIRE": 15,
      "HIRE": 13,
      "WEAK_HIRE": 5,
      "NO_HIRE": 9
    }
  }
}
```

### 7.2 Get Interviewer Performance
**GET** `/analytics/interviewers/{id}/performance`

**Response:**
```json
{
  "success": true,
  "data": {
    "interviewer": {
      "id": 1,
      "name": "Jane Smith"
    },
    "totalInterviews": 25,
    "averageScore": 8.2,
    "hireRate": 0.72,
    "averageInterviewDuration": 42,
    "roundTypeBreakdown": {
      "TECHNICAL_ROUND_1": 15,
      "TECHNICAL_ROUND_2": 10
    }
  }
}
```

## Error Codes

| Code | Description |
|------|-------------|
| `VALIDATION_ERROR` | Request validation failed |
| `RESOURCE_NOT_FOUND` | Requested resource not found |
| `UNAUTHORIZED` | Authentication required |
| `FORBIDDEN` | Insufficient permissions |
| `INTERNAL_SERVER_ERROR` | Server error |
| `CONFLICT` | Resource conflict |
| `BAD_REQUEST` | Invalid request |

## Rate Limiting

- **Standard endpoints**: 100 requests per minute
- **Report generation**: 10 requests per minute
- **File uploads**: 20 requests per minute

## Pagination

All list endpoints support pagination with the following parameters:
- `page`: Page number (0-based)
- `size`: Page size (1-100)
- `sort`: Sort field and direction (e.g., `createdAt,desc`)

## File Upload

For file uploads (resumes, attachments), use multipart/form-data:
```
POST /candidates/{id}/resume
Content-Type: multipart/form-data

file: [binary file data]
```

## WebSocket Endpoints

For real-time features:
```
ws://localhost:8080/ws/interviews/{interviewId}
```

WebSocket events:
- `notes_updated`: When notes are updated
- `status_changed`: When interview status changes
- `round_completed`: When a round is completed 