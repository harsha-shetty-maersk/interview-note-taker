# Deployment Guide

## Overview

This guide covers the complete deployment process for the Interview Note-Taking Application, including Docker containerization, Kubernetes deployment, and production considerations.

## Prerequisites

### Required Tools
- Docker Desktop or Docker Engine
- kubectl (Kubernetes CLI)
- minikube (for local development)
- helm (optional, for package management)
- git

### Required Accounts/Services
- Container registry (Docker Hub, AWS ECR, GCR, etc.)
- Kubernetes cluster (EKS, GKE, AKS, or local minikube)
- PostgreSQL database (managed service or self-hosted)

## Phase 1: Docker Configuration

### 1.1 Backend Dockerfile

Create `docker/Dockerfile.backend`:

```dockerfile
# Multi-stage build for Spring Boot application
FROM maven:3.8.6-openjdk-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY backend/src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

# Create app user
RUN addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --ingroup appuser appuser

# Set working directory
WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to app user
RUN chown -R appuser:appuser /app

# Switch to app user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 1.2 Frontend Dockerfile

Create `docker/Dockerfile.frontend`:

```dockerfile
# Build stage
FROM node:18-alpine AS builder

# Set working directory
WORKDIR /app

# Copy package files
COPY frontend/package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy source code
COPY frontend/src ./src
COPY frontend/public ./public
COPY frontend/tsconfig.json ./
COPY frontend/vite.config.ts ./

# Build the application
RUN npm run build

# Production stage
FROM nginx:alpine

# Copy built files
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY docker/nginx.conf /etc/nginx/nginx.conf

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
```

### 1.3 Nginx Configuration

Create `docker/nginx.conf`:

```nginx
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    # Logging
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;
    error_log /var/log/nginx/error.log warn;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

    server {
        listen 80;
        server_name localhost;

        # Health check endpoint
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }

        # API proxy
        location /api/ {
            limit_req zone=api burst=20 nodelay;
            
            proxy_pass http://backend:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # Timeouts
            proxy_connect_timeout 30s;
            proxy_send_timeout 30s;
            proxy_read_timeout 30s;
        }

        # Static files
        location / {
            root /usr/share/nginx/html;
            index index.html index.htm;
            try_files $uri $uri/ /index.html;
            
            # Cache static assets
            location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
                expires 1y;
                add_header Cache-Control "public, immutable";
            }
        }
    }
}
```

### 1.4 Docker Compose for Development

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  # PostgreSQL Database
  postgres:
    image: postgres:15-alpine
    container_name: interview-notes-postgres
    environment:
      POSTGRES_DB: interview_notes
      POSTGRES_USER: interview_user
      POSTGRES_PASSWORD: interview_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backend/src/main/resources/db/migration:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U interview_user -d interview_notes"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Backend Application
  backend:
    build:
      context: .
      dockerfile: docker/Dockerfile.backend
    container_name: interview-notes-backend
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/interview_notes
      SPRING_DATASOURCE_USERNAME: interview_user
      SPRING_DATASOURCE_PASSWORD: interview_password
      SPRING_JPA_HIBERNATE_DDL_AUTO: validate
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    volumes:
      - ./backend/logs:/app/logs
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Frontend Application
  frontend:
    build:
      context: .
      dockerfile: docker/Dockerfile.frontend
    container_name: interview-notes-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Redis (for caching and sessions)
  redis:
    image: redis:7-alpine
    container_name: interview-notes-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
  redis_data:

networks:
  default:
    name: interview-notes-network
```

## Phase 2: Kubernetes Deployment

### 2.1 Namespace Configuration

Create `k8s/namespace.yaml`:

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: interview-notes
  labels:
    name: interview-notes
    environment: production
```

### 2.2 ConfigMap for Application Configuration

Create `k8s/configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: interview-notes-config
  namespace: interview-notes
data:
  application.yml: |
    spring:
      profiles:
        active: prod
      datasource:
        url: jdbc:postgresql://interview-notes-postgres:5432/interview_notes
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
        hikari:
          maximum-pool-size: 20
          minimum-idle: 5
          connection-timeout: 30000
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
      redis:
        host: interview-notes-redis
        port: 6379
        timeout: 2000ms
        lettuce:
          pool:
            max-active: 8
            max-idle: 8
            min-idle: 0
    server:
      port: 8080
      servlet:
        context-path: /api
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: when-authorized
    logging:
      level:
        com.interviewnotes: INFO
        org.springframework.web: INFO
        org.hibernate.SQL: DEBUG
        org.hibernate.type.descriptor.sql.BasicBinder: TRACE
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
        file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
      file:
        name: /app/logs/application.log
        max-size: 100MB
        max-history: 30
```

### 2.3 Secrets Configuration

Create `k8s/secrets.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: interview-notes-secrets
  namespace: interview-notes
type: Opaque
data:
  # Base64 encoded values (use: echo -n "your-value" | base64)
  DB_USERNAME: aW50ZXJ2aWV3X3VzZXI=  # interview_user
  DB_PASSWORD: aW50ZXJ2aWV3X3Bhc3N3b3Jk  # interview_password
  JWT_SECRET: eW91ci1qd3Qtc2VjcmV0LWtleS1oZXJl  # your-jwt-secret-key-here
  REDIS_PASSWORD: cmVkaXNfcGFzc3dvcmQ=  # redis_password
```

### 2.4 PostgreSQL Deployment

Create `k8s/postgres.yaml`:

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: interview-notes-postgres
  namespace: interview-notes
spec:
  serviceName: interview-notes-postgres
  replicas: 1
  selector:
    matchLabels:
      app: interview-notes-postgres
  template:
    metadata:
      labels:
        app: interview-notes-postgres
    spec:
      containers:
      - name: postgres
        image: postgres:15-alpine
        ports:
        - containerPort: 5432
        env:
        - name: POSTGRES_DB
          value: interview_notes
        - name: POSTGRES_USER
          valueFrom:
            secretKeyRef:
              name: interview-notes-secrets
              key: DB_USERNAME
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: interview-notes-secrets
              key: DB_PASSWORD
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
        - name: postgres-init
          mountPath: /docker-entrypoint-initdb.d
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          exec:
            command:
            - pg_isready
            - -U
            - interview_user
            - -d
            - interview_notes
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          exec:
            command:
            - pg_isready
            - -U
            - interview_user
            - -d
            - interview_notes
          initialDelaySeconds: 5
          periodSeconds: 5
      volumes:
      - name: postgres-init
        configMap:
          name: postgres-init-scripts
  volumeClaimTemplates:
  - metadata:
      name: postgres-storage
    spec:
      accessModes: [ "ReadWriteOnce" ]
      resources:
        requests:
          storage: 10Gi
---
apiVersion: v1
kind: Service
metadata:
  name: interview-notes-postgres
  namespace: interview-notes
spec:
  selector:
    app: interview-notes-postgres
  ports:
  - port: 5432
    targetPort: 5432
  type: ClusterIP
```

### 2.5 Redis Deployment

Create `k8s/redis.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: interview-notes-redis
  namespace: interview-notes
spec:
  replicas: 1
  selector:
    matchLabels:
      app: interview-notes-redis
  template:
    metadata:
      labels:
        app: interview-notes-redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        command:
        - redis-server
        - --requirepass
        - $(REDIS_PASSWORD)
        env:
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: interview-notes-secrets
              key: REDIS_PASSWORD
        volumeMounts:
        - name: redis-storage
          mountPath: /data
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
        livenessProbe:
          exec:
            command:
            - redis-cli
            - -a
            - $(REDIS_PASSWORD)
            - ping
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          exec:
            command:
            - redis-cli
            - -a
            - $(REDIS_PASSWORD)
            - ping
          initialDelaySeconds: 5
          periodSeconds: 5
      volumes:
      - name: redis-storage
        persistentVolumeClaim:
          claimName: redis-pvc
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: redis-pvc
  namespace: interview-notes
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
---
apiVersion: v1
kind: Service
metadata:
  name: interview-notes-redis
  namespace: interview-notes
spec:
  selector:
    app: interview-notes-redis
  ports:
  - port: 6379
    targetPort: 6379
  type: ClusterIP
```

### 2.6 Backend Deployment

Create `k8s/backend.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: interview-notes-backend
  namespace: interview-notes
spec:
  replicas: 3
  selector:
    matchLabels:
      app: interview-notes-backend
  template:
    metadata:
      labels:
        app: interview-notes-backend
    spec:
      containers:
      - name: backend
        image: your-registry/interview-notes-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: prod
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: interview-notes-secrets
              key: DB_USERNAME
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: interview-notes-secrets
              key: DB_PASSWORD
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: interview-notes-secrets
              key: JWT_SECRET
        volumeMounts:
        - name: logs
          mountPath: /app/logs
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /api/actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /api/actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
      volumes:
      - name: logs
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: interview-notes-backend
  namespace: interview-notes
spec:
  selector:
    app: interview-notes-backend
  ports:
  - port: 8080
    targetPort: 8080
  type: ClusterIP
```

### 2.7 Frontend Deployment

Create `k8s/frontend.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: interview-notes-frontend
  namespace: interview-notes
spec:
  replicas: 2
  selector:
    matchLabels:
      app: interview-notes-frontend
  template:
    metadata:
      labels:
        app: interview-notes-frontend
    spec:
      containers:
      - name: frontend
        image: your-registry/interview-notes-frontend:latest
        ports:
        - containerPort: 80
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
        livenessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 30
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /health
            port: 80
          initialDelaySeconds: 10
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
---
apiVersion: v1
kind: Service
metadata:
  name: interview-notes-frontend
  namespace: interview-notes
spec:
  selector:
    app: interview-notes-frontend
  ports:
  - port: 80
    targetPort: 80
  type: ClusterIP
```

### 2.8 Ingress Configuration

Create `k8s/ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: interview-notes-ingress
  namespace: interview-notes
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
spec:
  tls:
  - hosts:
    - interview-notes.yourdomain.com
    secretName: interview-notes-tls
  rules:
  - host: interview-notes.yourdomain.com
    http:
      paths:
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: interview-notes-backend
            port:
              number: 8080
      - path: /
        pathType: Prefix
        backend:
          service:
            name: interview-notes-frontend
            port:
              number: 80
```

### 2.9 Horizontal Pod Autoscaler

Create `k8s/hpa.yaml`:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: interview-notes-backend-hpa
  namespace: interview-notes
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: interview-notes-backend
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: interview-notes-frontend-hpa
  namespace: interview-notes
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: interview-notes-frontend
  minReplicas: 2
  maxReplicas: 5
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

## Phase 3: Deployment Scripts

### 3.1 Build and Push Script

Create `scripts/build-and-push.sh`:

```bash
#!/bin/bash

# Configuration
REGISTRY="your-registry"
BACKEND_IMAGE="interview-notes-backend"
FRONTEND_IMAGE="interview-notes-frontend"
VERSION=$(git rev-parse --short HEAD)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Building and pushing Docker images...${NC}"

# Build backend image
echo -e "${GREEN}Building backend image...${NC}"
docker build -f docker/Dockerfile.backend -t ${REGISTRY}/${BACKEND_IMAGE}:${VERSION} .
docker tag ${REGISTRY}/${BACKEND_IMAGE}:${VERSION} ${REGISTRY}/${BACKEND_IMAGE}:latest

# Build frontend image
echo -e "${GREEN}Building frontend image...${NC}"
docker build -f docker/Dockerfile.frontend -t ${REGISTRY}/${FRONTEND_IMAGE}:${VERSION} .
docker tag ${REGISTRY}/${FRONTEND_IMAGE}:${VERSION} ${REGISTRY}/${FRONTEND_IMAGE}:latest

# Push images
echo -e "${GREEN}Pushing images to registry...${NC}"
docker push ${REGISTRY}/${BACKEND_IMAGE}:${VERSION}
docker push ${REGISTRY}/${BACKEND_IMAGE}:latest
docker push ${REGISTRY}/${FRONTEND_IMAGE}:${VERSION}
docker push ${REGISTRY}/${FRONTEND_IMAGE}:latest

echo -e "${GREEN}Build and push completed successfully!${NC}"
echo -e "${YELLOW}Version: ${VERSION}${NC}"
```

### 3.2 Deployment Script

Create `scripts/deploy.sh`:

```bash
#!/bin/bash

# Configuration
NAMESPACE="interview-notes"
VERSION=$(git rev-parse --short HEAD)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Deploying Interview Notes Application...${NC}"

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}kubectl is not installed or not in PATH${NC}"
    exit 1
fi

# Create namespace
echo -e "${GREEN}Creating namespace...${NC}"
kubectl apply -f k8s/namespace.yaml

# Apply secrets
echo -e "${GREEN}Applying secrets...${NC}"
kubectl apply -f k8s/secrets.yaml

# Apply ConfigMap
echo -e "${GREEN}Applying ConfigMap...${NC}"
kubectl apply -f k8s/configmap.yaml

# Deploy PostgreSQL
echo -e "${GREEN}Deploying PostgreSQL...${NC}"
kubectl apply -f k8s/postgres.yaml

# Wait for PostgreSQL to be ready
echo -e "${YELLOW}Waiting for PostgreSQL to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app=interview-notes-postgres -n ${NAMESPACE} --timeout=300s

# Deploy Redis
echo -e "${GREEN}Deploying Redis...${NC}"
kubectl apply -f k8s/redis.yaml

# Wait for Redis to be ready
echo -e "${YELLOW}Waiting for Redis to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app=interview-notes-redis -n ${NAMESPACE} --timeout=300s

# Deploy backend
echo -e "${GREEN}Deploying backend...${NC}"
kubectl apply -f k8s/backend.yaml

# Deploy frontend
echo -e "${GREEN}Deploying frontend...${NC}"
kubectl apply -f k8s/frontend.yaml

# Deploy HPA
echo -e "${GREEN}Deploying Horizontal Pod Autoscaler...${NC}"
kubectl apply -f k8s/hpa.yaml

# Deploy Ingress
echo -e "${GREEN}Deploying Ingress...${NC}"
kubectl apply -f k8s/ingress.yaml

# Wait for all deployments to be ready
echo -e "${YELLOW}Waiting for all deployments to be ready...${NC}"
kubectl wait --for=condition=available deployment/interview-notes-backend -n ${NAMESPACE} --timeout=300s
kubectl wait --for=condition=available deployment/interview-notes-frontend -n ${NAMESPACE} --timeout=300s

echo -e "${GREEN}Deployment completed successfully!${NC}"
echo -e "${YELLOW}Version: ${VERSION}${NC}"

# Show service URLs
echo -e "${GREEN}Service URLs:${NC}"
kubectl get svc -n ${NAMESPACE}
echo -e "${GREEN}Ingress:${NC}"
kubectl get ingress -n ${NAMESPACE}
```

### 3.3 Rollback Script

Create `scripts/rollback.sh`:

```bash
#!/bin/bash

# Configuration
NAMESPACE="interview-notes"
PREVIOUS_VERSION=$1

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

if [ -z "$PREVIOUS_VERSION" ]; then
    echo -e "${RED}Please provide the previous version to rollback to${NC}"
    echo "Usage: ./rollback.sh <previous-version>"
    exit 1
fi

echo -e "${YELLOW}Rolling back to version: ${PREVIOUS_VERSION}${NC}"

# Rollback backend deployment
echo -e "${GREEN}Rolling back backend...${NC}"
kubectl rollout undo deployment/interview-notes-backend -n ${NAMESPACE} --to-revision=${PREVIOUS_VERSION}

# Rollback frontend deployment
echo -e "${GREEN}Rolling back frontend...${NC}"
kubectl rollout undo deployment/interview-notes-frontend -n ${NAMESPACE} --to-revision=${PREVIOUS_VERSION}

# Wait for rollback to complete
echo -e "${YELLOW}Waiting for rollback to complete...${NC}"
kubectl rollout status deployment/interview-notes-backend -n ${NAMESPACE}
kubectl rollout status deployment/interview-notes-frontend -n ${NAMESPACE}

echo -e "${GREEN}Rollback completed successfully!${NC}"
```

## Phase 4: Monitoring and Logging

### 4.1 Prometheus Configuration

Create `k8s/monitoring/prometheus-config.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: interview-notes
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s

    rule_files:
      - "alert_rules.yml"

    alerting:
      alertmanagers:
        - static_configs:
            - targets:
              - alertmanager:9093

    scrape_configs:
      - job_name: 'kubernetes-pods'
        kubernetes_sd_configs:
          - role: pod
        relabel_configs:
          - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
            action: keep
            regex: true
          - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
            action: replace
            target_label: __metrics_path__
            regex: (.+)
          - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
            action: replace
            regex: ([^:]+)(?::\d+)?;(\d+)
            replacement: $1:$2
            target_label: __address__
          - action: labelmap
            regex: __meta_kubernetes_pod_label_(.+)
          - source_labels: [__meta_kubernetes_namespace]
            action: replace
            target_label: kubernetes_namespace
          - source_labels: [__meta_kubernetes_pod_name]
            action: replace
            target_label: kubernetes_pod_name

      - job_name: 'interview-notes-backend'
        static_configs:
          - targets: ['interview-notes-backend:8080']
        metrics_path: '/api/actuator/prometheus'
```

### 4.2 Grafana Dashboard

Create `k8s/monitoring/grafana-dashboard.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-dashboard
  namespace: interview-notes
data:
  interview-notes-dashboard.json: |
    {
      "dashboard": {
        "id": null,
        "title": "Interview Notes Dashboard",
        "tags": ["interview-notes"],
        "timezone": "browser",
        "panels": [
          {
            "id": 1,
            "title": "Request Rate",
            "type": "graph",
            "targets": [
              {
                "expr": "rate(http_requests_total[5m])",
                "legendFormat": "{{method}} {{endpoint}}"
              }
            ]
          },
          {
            "id": 2,
            "title": "Response Time",
            "type": "graph",
            "targets": [
              {
                "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
                "legendFormat": "95th percentile"
              }
            ]
          },
          {
            "id": 3,
            "title": "Error Rate",
            "type": "graph",
            "targets": [
              {
                "expr": "rate(http_requests_total{status=~\"5..\"}[5m])",
                "legendFormat": "5xx errors"
              }
            ]
          }
        ]
      }
    }
```

## Phase 5: Production Considerations

### Security Best Practices

1. **Network Policies**
2. **RBAC Configuration**
3. **Pod Security Policies**
4. **Secret Management**
5. **Image Scanning**

### Backup Strategy

1. **Database Backups**
2. **Configuration Backups**
3. **Disaster Recovery Plan**

### Performance Optimization

1. **Resource Limits**
2. **Horizontal Scaling**
3. **Caching Strategy**
4. **CDN Integration**

## Quick Start Commands

```bash
# 1. Build and push images
./scripts/build-and-push.sh

# 2. Deploy to Kubernetes
./scripts/deploy.sh

# 3. Check deployment status
kubectl get pods -n interview-notes

# 4. View logs
kubectl logs -f deployment/interview-notes-backend -n interview-notes

# 5. Access the application
kubectl get ingress -n interview-notes

# 6. Scale deployment
kubectl scale deployment interview-notes-backend --replicas=5 -n interview-notes

# 7. Rollback if needed
./scripts/rollback.sh <previous-version>
```

This comprehensive deployment guide provides everything needed to deploy the interview note-taking application to production with proper monitoring, scaling, and security considerations. 