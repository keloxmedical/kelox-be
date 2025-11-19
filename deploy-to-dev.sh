#!/bin/bash

# Kelox Backend - Deploy to DEV Environment
# This script deploys to the development/test environment on AWS

set -e

echo "🧪 Kelox Backend - Deploy to DEV Environment"
echo "============================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# Copy dev-specific .ebextensions
echo ""
echo "📋 Preparing dev configuration..."
if [ -d ".ebextensions-backup" ]; then
    rm -rf .ebextensions-backup
fi

# Backup prod config if exists
if [ -d ".ebextensions" ]; then
    mv .ebextensions .ebextensions-backup
fi

# Copy dev config
cp -r .ebextensions-dev .ebextensions
echo -e "${GREEN}✓ Dev configuration prepared${NC}"

# Check if EB is initialized
if [ ! -d ".elasticbeanstalk" ]; then
    echo ""
    echo -e "${YELLOW}⚠️  Elastic Beanstalk not initialized yet${NC}"
    echo -e "${BLUE}Initializing EB for the first time...${NC}"
    
    # Get AWS region from AWS CLI config
    AWS_REGION=$(aws configure get region)
    if [ -z "$AWS_REGION" ]; then
        AWS_REGION="us-east-1"
        echo -e "${YELLOW}No AWS region configured, using default: $AWS_REGION${NC}"
    fi
    
    # Initialize EB
    eb init -p "Corretto 17 running on 64bit Amazon Linux 2023" -r $AWS_REGION kelox-backend --profile default
    
    echo -e "${GREEN}✓ EB initialized${NC}"
fi

# Check if dev environment exists
ENV_EXISTS=$(eb list 2>/dev/null | grep "kelox-dev" || true)

if [ -z "$ENV_EXISTS" ]; then
    echo ""
    echo -e "${YELLOW}⚠️  Dev environment doesn't exist yet${NC}"
    echo -e "${BLUE}Please create it first by running: ./setup-dev-environment.sh${NC}"
    
    # Restore prod config
    if [ -d ".ebextensions-backup" ]; then
        rm -rf .ebextensions
        mv .ebextensions-backup .ebextensions
    fi
    
    exit 1
fi

# Deploy to dev environment
echo ""
echo -e "${BLUE}🚢 Deploying to kelox-dev environment...${NC}"
eb use kelox-dev
eb deploy kelox-dev

# Restore prod config
if [ -d ".ebextensions-backup" ]; then
    rm -rf .ebextensions
    mv .ebextensions-backup .ebextensions
fi

echo ""
echo -e "${GREEN}✅ Deployment to DEV completed successfully!${NC}"
echo ""
echo "📊 Useful commands:"
echo "  eb status kelox-dev       - Check environment status"
echo "  eb health kelox-dev       - Check application health"
echo "  eb logs kelox-dev         - View recent logs"
echo "  eb open kelox-dev         - Open application in browser"
echo "  eb ssh kelox-dev          - SSH into the instance"
echo ""
echo -e "${BLUE}🌐 Your dev API is available at:${NC}"
eb status kelox-dev | grep "CNAME" | awk '{print "  http://"$3"/api"}'
echo ""

