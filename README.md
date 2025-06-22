# Interview Notes Application

A comprehensive interview note-taking application built with Spring Boot, React, and PostgreSQL. This application helps interviewers manage candidates, schedule interviews, and take detailed notes during interviews with scoring and recommendations.

## 🚀 Features

- **User Management**: Role-based access control (Admin, Interviewer)
- **Candidate Management**: Add, edit, and track candidates
- **Interview Scheduling**: Schedule and manage interviews
- **Interview Notes**: Real-time note-taking with timestamps
- **Scoring System**: Multi-category scoring (Technical, Problem Solving, Communication, Cultural Fit)
- **Recommendations**: Final hiring recommendations with explanations
- **Reports**: Comprehensive interview reports and analytics
- **Responsive Design**: Modern, mobile-friendly UI

## 🏗️ Architecture

- **Backend**: Spring Boot 3.x with Java 17
- **Frontend**: React 18 with TypeScript
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Authentication**: JWT-based authentication
- **Containerization**: Docker with multi-stage builds
- **Orchestration**: Kubernetes
- **CI/CD**: GitHub Actions

## 📋 Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Docker and Docker Compose
- Kubernetes cluster (for production deployment)
- PostgreSQL 15
- Redis 7

## 🛠️ Local Development

### Option 1: Using Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd interviewNoteTaker
   ```

2. **Start the application**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Database: localhost:5432
   - Redis: localhost:6379

### Option 2: Manual Setup

1. **Backend Setup**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

2. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm start
   ```

3. **Database Setup**
   ```bash
   # Start PostgreSQL and Redis
   docker run -d --name postgres -e POSTGRES_DB=interview_notes -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:15
   docker run -d --name redis -p 6379:6379 redis:7
   ```

## 🐳 Docker

### Building Images

```bash
# Build backend image
docker build -f docker/Dockerfile.backend -t interview-notes-backend .

# Build frontend image
docker build -f docker/Dockerfile.frontend -t interview-notes-frontend .
```

### Running with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

## ☸️ Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (minikube, Docker Desktop, or cloud provider)
- kubectl configured
- Docker images pushed to a registry

### Local Deployment

1. **Build and push images**
   ```bash
   # Set your registry
   export DOCKER_REGISTRY=your-registry.com
   export IMAGE_TAG=latest
   
   # Build and push
   docker build -f docker/Dockerfile.backend -t $DOCKER_REGISTRY/interview-notes-backend:$IMAGE_TAG .
   docker build -f docker/Dockerfile.frontend -t $DOCKER_REGISTRY/interview-notes-frontend:$IMAGE_TAG .
   docker push $DOCKER_REGISTRY/interview-notes-backend:$IMAGE_TAG
   docker push $DOCKER_REGISTRY/interview-notes-frontend:$IMAGE_TAG
   ```

2. **Deploy to Kubernetes**
   ```bash
   # Set environment variables
   export DOCKER_REGISTRY=your-registry.com
   export IMAGE_TAG=latest
   
   # Run deployment script
   ./scripts/deploy.sh
   ```

### Manual Deployment

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Apply configuration
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# Deploy infrastructure
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/redis.yaml

# Deploy applications
envsubst < k8s/backend.yaml | kubectl apply -f -
envsubst < k8s/frontend.yaml | kubectl apply -f -
```

## 🔄 CI/CD Pipeline

The application uses GitHub Actions for continuous integration and deployment.

### Pipeline Stages

1. **Test**: Runs unit tests for both backend and frontend
2. **Build**: Builds and pushes Docker images to GitHub Container Registry
3. **Deploy Staging**: Deploys to staging environment on `develop` branch
4. **Deploy Production**: Deploys to production environment on `main` branch

### Setup

1. **Fork/Clone the repository**

2. **Configure GitHub Secrets**
   - `KUBE_CONFIG_STAGING`: Base64 encoded kubeconfig for staging cluster
   - `KUBE_CONFIG_PRODUCTION`: Base64 encoded kubeconfig for production cluster

3. **Update Configuration**
   - Update `DOCKER_REGISTRY` in `.github/workflows/ci-cd.yml`
   - Update domain in `k8s/frontend.yaml` ingress configuration
   - Update secrets in `k8s/secret.yaml`

### Workflow

```bash
# Development workflow
git checkout -b feature/new-feature
# Make changes
git commit -m "Add new feature"
git push origin feature/new-feature
# Create pull request

# Staging deployment
git checkout develop
git merge feature/new-feature
git push origin develop
# Automatic deployment to staging

# Production deployment
git checkout main
git merge develop
git push origin main
# Automatic deployment to production
```

## 🔧 Configuration

### Environment Variables

#### Backend
- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SPRING_REDIS_HOST`: Redis host
- `SPRING_REDIS_PORT`: Redis port
- `JWT_SECRET`: JWT signing secret
- `SPRING_PROFILES_ACTIVE`: Active Spring profile

#### Frontend
- `REACT_APP_API_URL`: Backend API URL

### Database Migration

The application uses Flyway for database migrations. Migrations are automatically applied on startup.

```bash
# Manual migration (if needed)
cd backend
mvn flyway:migrate
```

## 📊 Monitoring and Logging

### Health Checks

- Backend: `GET /actuator/health`
- Frontend: `GET /`

### Logs

```bash
# Kubernetes logs
kubectl logs -f deployment/backend -n interview-notes
kubectl logs -f deployment/frontend -n interview-notes

# Docker logs
docker-compose logs -f backend
docker-compose logs -f frontend
```

## 🔒 Security

- JWT-based authentication
- Role-based access control
- HTTPS enforcement (in production)
- Security headers
- Input validation and sanitization

## 🧪 Testing

### Backend Tests

```bash
cd backend
mvn test
```

### Frontend Tests

```bash
cd frontend
npm test
```

### Integration Tests

```bash
# Run with Docker Compose
docker-compose -f docker-compose.test.yml up --abort-on-container-exit
```

## 📝 API Documentation

API documentation is available at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the GitHub repository
- Check the documentation in the `docs/` folder
- Review the API documentation

## 🚀 Quick Start

```bash
# Clone and run with Docker Compose
git clone <your-repo-url>
cd interviewNoteTaker
docker-compose up -d

# Access the application
open http://localhost:3000
```

Default admin credentials:
- Username: admin
- Password: admin123 