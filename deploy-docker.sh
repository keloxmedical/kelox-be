#!/bin/bash

# Kelox Backend - Docker Build and Test Script
# This script builds and tests the Docker image locally

set -e

echo "🐳 Kelox Backend - Docker Build"
echo "================================"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

IMAGE_NAME="kelox-backend"
IMAGE_TAG="latest"

# Build Docker image
echo ""
echo "📦 Building Docker image..."
docker build -t $IMAGE_NAME:$IMAGE_TAG .

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Docker image built successfully${NC}"
else
    echo -e "${RED}❌ Docker build failed${NC}"
    exit 1
fi

echo ""
echo "🔍 Image details:"
docker images | grep $IMAGE_NAME

echo ""
echo -e "${GREEN}✅ Build completed!${NC}"
echo ""
echo "🚀 To run locally:"
echo "  docker run -p 8080:5000 \\"
echo "    -e DB_URL=jdbc:postgresql://host.docker.internal:5432/kelox_db \\"
echo "    -e DB_USERNAME=postgres \\"
echo "    -e DB_PASSWORD=postgres \\"
echo "    -e ADMIN_SECRET_CODE=your-secret \\"
echo "    -e JWT_SECRET=your-jwt-secret \\"
echo "    $IMAGE_NAME:$IMAGE_TAG"
echo ""
echo "📤 To push to AWS ECR:"
echo "  1. Create ECR repository: aws ecr create-repository --repository-name kelox-backend"
echo "  2. Login: aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <account-id>.dkr.ecr.<region>.amazonaws.com"
echo "  3. Tag: docker tag $IMAGE_NAME:$IMAGE_TAG <account-id>.dkr.ecr.<region>.amazonaws.com/kelox-backend:latest"
echo "  4. Push: docker push <account-id>.dkr.ecr.<region>.amazonaws.com/kelox-backend:latest"
echo ""

