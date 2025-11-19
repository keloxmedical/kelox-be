#!/bin/bash

# Kelox Backend - AWS Elastic Beanstalk Deployment Script
# This script automates the deployment process to AWS Elastic Beanstalk

set -e

echo "🚀 Kelox Backend - AWS Elastic Beanstalk Deployment"
echo "=================================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI is not installed. Please install it first.${NC}"
    echo "Visit: https://aws.amazon.com/cli/"
    exit 1
fi

# Check if EB CLI is installed
if ! command -v eb &> /dev/null; then
    echo -e "${RED}❌ Elastic Beanstalk CLI is not installed.${NC}"
    echo "Install it with: pip install awsebcli"
    exit 1
fi

echo -e "${GREEN}✓ Prerequisites checked${NC}"

# Clean and build the application
echo ""
echo "📦 Building application..."
./gradlew clean bootJar -x test

if [ ! -f "build/libs/kelox-be-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "${RED}❌ Build failed - JAR file not found${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Application built successfully${NC}"

# Check if EB is initialized
if [ ! -d ".elasticbeanstalk" ]; then
    echo ""
    echo -e "${YELLOW}⚠️  Elastic Beanstalk not initialized yet${NC}"
    echo "Run './deploy-elastic-beanstalk.sh init' first to set up your environment"
    exit 1
fi

# Deploy to Elastic Beanstalk
echo ""
echo "🚢 Deploying to AWS Elastic Beanstalk..."
eb deploy

echo ""
echo -e "${GREEN}✅ Deployment completed successfully!${NC}"
echo ""
echo "📊 Useful commands:"
echo "  eb status     - Check environment status"
echo "  eb health     - Check application health"
echo "  eb logs       - View recent logs"
echo "  eb open       - Open application in browser"
echo "  eb ssh        - SSH into the instance"
echo ""

