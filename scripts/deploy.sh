#!/bin/bash

# Interview Notes Application Deployment Script
# This script deploys the application to a local Kubernetes cluster

set -e

# Configuration
NAMESPACE="interview-notes"
DOCKER_REGISTRY=${DOCKER_REGISTRY:-"localhost:5000"}
IMAGE_TAG=${IMAGE_TAG:-"latest"}

echo "🚀 Starting deployment of Interview Notes Application..."

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed. Please install kubectl first."
    exit 1
fi

# Check if cluster is accessible
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Cannot connect to Kubernetes cluster. Please ensure your cluster is running."
    exit 1
fi

echo "✅ Kubernetes cluster is accessible"

# Create namespace if it doesn't exist
echo "📦 Creating namespace..."
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Apply ConfigMap and Secret
echo "🔧 Applying configuration..."
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# Deploy database and cache
echo "🗄️  Deploying PostgreSQL..."
kubectl apply -f k8s/postgres.yaml

echo "🔴 Deploying Redis..."
kubectl apply -f k8s/redis.yaml

# Wait for database and cache to be ready
echo "⏳ Waiting for database and cache to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n $NAMESPACE --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n $NAMESPACE --timeout=300s

# Deploy backend and frontend
echo "🔧 Deploying backend..."
envsubst < k8s/backend.yaml | kubectl apply -f -

echo "🎨 Deploying frontend..."
envsubst < k8s/frontend.yaml | kubectl apply -f -

# Wait for deployments to be ready
echo "⏳ Waiting for deployments to be ready..."
kubectl rollout status deployment/backend -n $NAMESPACE --timeout=300s
kubectl rollout status deployment/frontend -n $NAMESPACE --timeout=300s

# Get service URLs
echo "🌐 Getting service URLs..."
BACKEND_URL=$(kubectl get service backend-service -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
FRONTEND_URL=$(kubectl get service frontend-service -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

if [ -z "$BACKEND_URL" ]; then
    BACKEND_URL=$(kubectl get service backend-service -n $NAMESPACE -o jsonpath='{.spec.clusterIP}')
fi

if [ -z "$FRONTEND_URL" ]; then
    FRONTEND_URL=$(kubectl get service frontend-service -n $NAMESPACE -o jsonpath='{.spec.clusterIP}')
fi

echo "✅ Deployment completed successfully!"
echo ""
echo "📊 Deployment Status:"
kubectl get pods -n $NAMESPACE
echo ""
echo "🌐 Service URLs:"
echo "   Backend API: http://$BACKEND_URL:8080"
echo "   Frontend: http://$FRONTEND_URL:80"
echo ""
echo "📝 To access the application:"
echo "   1. If using minikube: run 'minikube service frontend-service -n $NAMESPACE'"
echo "   2. If using Docker Desktop: access via localhost"
echo "   3. If using cloud provider: check your load balancer configuration"
echo ""
echo "🔍 To check logs:"
echo "   kubectl logs -f deployment/backend -n $NAMESPACE"
echo "   kubectl logs -f deployment/frontend -n $NAMESPACE" 