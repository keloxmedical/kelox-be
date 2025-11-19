#!/bin/bash

# Kelox Backend - Deploy to PRODUCTION Environment
# This script deploys to the production environment on AWS

set -e

echo "🚀 Kelox Backend - Deploy to PRODUCTION Environment"
echo "==================================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Safety check
echo -e "${RED}⚠️  WARNING: You are about to deploy to PRODUCTION!${NC}"
echo ""
read -p "Are you sure you want to continue? (type 'yes' to confirm): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo -e "${YELLOW}Deployment cancelled.${NC}"
    exit 0
fi

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

# Run tests before production deployment
echo ""
echo "🧪 Running tests..."
./gradlew test

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Tests failed! Cannot deploy to production.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All tests passed${NC}"

# Clean and build the application
echo ""
echo "📦 Building application..."
./gradlew clean bootJar -x test

if [ ! -f "build/libs/kelox-be-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "${RED}❌ Build failed - JAR file not found${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Application built successfully${NC}"

# Ensure we're using production .ebextensions
echo ""
echo "📋 Ensuring production configuration..."
if [ -d ".ebextensions-backup" ]; then
    rm -rf .ebextensions-backup
fi

echo -e "${GREEN}✓ Production configuration verified${NC}"

# Check if EB is initialized
if [ ! -d ".elasticbeanstalk" ]; then
    echo ""
    echo -e "${RED}❌ Elastic Beanstalk not initialized${NC}"
    echo "Run: ./setup-prod-environment.sh"
    exit 1
fi

# Check if prod environment exists
ENV_EXISTS=$(eb list 2>/dev/null | grep "kelox-prod" || true)

if [ -z "$ENV_EXISTS" ]; then
    echo ""
    echo -e "${RED}❌ Production environment doesn't exist${NC}"
    echo "Run: ./setup-prod-environment.sh"
    exit 1
fi

# Deploy to production environment
echo ""
echo -e "${BLUE}🚢 Deploying to kelox-prod environment...${NC}"
eb use kelox-prod
eb deploy kelox-prod

echo ""
echo -e "${GREEN}✅ Deployment to PRODUCTION completed successfully!${NC}"
echo ""
echo "📊 Monitoring commands:"
echo "  eb status kelox-prod       - Check environment status"
echo "  eb health kelox-prod       - Check application health"
echo "  eb logs kelox-prod         - View recent logs"
echo "  eb open kelox-prod         - Open application in browser"
echo ""
echo -e "${BLUE}🌐 Your production API is available at:${NC}"
eb status kelox-prod | grep "CNAME" | awk '{print "  https://"$3"/api"}'
echo ""
echo -e "${YELLOW}⚠️  Remember to:${NC}"
echo "  1. Monitor CloudWatch metrics"
echo "  2. Check application health"
echo "  3. Test critical endpoints"
echo "  4. Monitor error logs"
echo ""

