#!/bin/bash

# Kelox Backend - Setup PRODUCTION Environment
# This script creates the production environment on AWS

set -e

echo "🚀 Kelox Backend - Setup PRODUCTION Environment"
echo "==============================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Safety warning
echo -e "${RED}⚠️  WARNING: You are about to create a PRODUCTION environment!${NC}"
echo ""
read -p "Have you tested in DEV first? (type 'yes' to continue): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo -e "${YELLOW}Setup cancelled. Please test in DEV first.${NC}"
    exit 0
fi

# Check prerequisites
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI is not installed.${NC}"
    echo "Install: brew install awscli (Mac) or visit https://aws.amazon.com/cli/"
    exit 1
fi

if ! command -v eb &> /dev/null; then
    echo -e "${RED}❌ Elastic Beanstalk CLI is not installed.${NC}"
    echo "Install: pip install awsebcli"
    exit 1
fi

echo -e "${GREEN}✓ Prerequisites checked${NC}"

# Get AWS region
AWS_REGION=$(aws configure get region)
if [ -z "$AWS_REGION" ]; then
    echo ""
    echo "Enter your AWS region (e.g., us-east-1, eu-west-1):"
    read -p "Region: " AWS_REGION
    if [ -z "$AWS_REGION" ]; then
        AWS_REGION="us-east-1"
        echo -e "${YELLOW}Using default region: $AWS_REGION${NC}"
    fi
fi

echo ""
echo "🔧 Configuration:"
echo "  Region: $AWS_REGION"
echo ""

# Step 1: Create RDS Database for Production
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Step 1: Create RDS PostgreSQL Database"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Require strong password for production
echo -e "${YELLOW}Enter a STRONG password for the PRODUCTION database:${NC}"
echo "(Minimum 12 characters, include numbers and special characters)"
read -sp "DB Password: " DB_PASSWORD
echo ""
if [ -z "$DB_PASSWORD" ] || [ ${#DB_PASSWORD} -lt 12 ]; then
    echo -e "${RED}❌ Password must be at least 12 characters!${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}Creating RDS instance (this takes ~5-10 minutes)...${NC}"

# Check if database already exists
DB_EXISTS=$(aws rds describe-db-instances --db-instance-identifier kelox-prod-db --region $AWS_REGION 2>/dev/null || echo "not_found")

if [[ "$DB_EXISTS" != "not_found" ]]; then
    echo -e "${YELLOW}⚠️  Database kelox-prod-db already exists!${NC}"
    echo ""
    DB_ENDPOINT=$(aws rds describe-db-instances \
        --db-instance-identifier kelox-prod-db \
        --region $AWS_REGION \
        --query 'DBInstances[0].Endpoint.Address' \
        --output text)
    echo -e "${GREEN}✓ Using existing database${NC}"
else
    # Create the database with production settings
    aws rds create-db-instance \
        --db-instance-identifier kelox-prod-db \
        --db-instance-class db.t3.small \
        --engine postgres \
        --engine-version 15.4 \
        --master-username postgres \
        --master-user-password "$DB_PASSWORD" \
        --allocated-storage 50 \
        --storage-type gp3 \
        --storage-encrypted \
        --publicly-accessible \
        --backup-retention-period 7 \
        --db-name kelox_db \
        --region $AWS_REGION \
        --multi-az \
        --enable-cloudwatch-logs-exports '["postgresql"]' \
        --tags Key=Environment,Value=production Key=Project,Value=kelox

    echo ""
    echo -e "${BLUE}⏳ Waiting for database to be available (this may take 10-15 minutes)...${NC}"
    aws rds wait db-instance-available \
        --db-instance-identifier kelox-prod-db \
        --region $AWS_REGION

    DB_ENDPOINT=$(aws rds describe-db-instances \
        --db-instance-identifier kelox-prod-db \
        --region $AWS_REGION \
        --query 'DBInstances[0].Endpoint.Address' \
        --output text)
    
    echo -e "${GREEN}✓ Database created successfully${NC}"
fi

echo ""
echo "Database Endpoint: $DB_ENDPOINT"
echo ""

# Step 2: Build Application
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 Step 2: Build Application"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Run tests first
echo "Running tests..."
./gradlew test

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Tests failed! Fix tests before deploying to production.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Tests passed${NC}"

./gradlew clean bootJar -x test

if [ ! -f "build/libs/kelox-be-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Application built${NC}"

# Step 3: Initialize Elastic Beanstalk
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔧 Step 3: Initialize Elastic Beanstalk"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ ! -d ".elasticbeanstalk" ]; then
    eb init -p "Corretto 17 running on 64bit Amazon Linux 2023" -r $AWS_REGION kelox-backend
    echo -e "${GREEN}✓ EB initialized${NC}"
else
    echo -e "${YELLOW}⚠️  EB already initialized${NC}"
fi

# Step 4: Create EB Environment
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 Step 4: Create EB Environment"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Generate strong secrets
ADMIN_SECRET="kelox-admin-prod-$(openssl rand -hex 16)"
JWT_SECRET="kelox-jwt-prod-$(openssl rand -hex 32)"

DB_URL="jdbc:postgresql://${DB_ENDPOINT}:5432/kelox_db"

echo -e "${BLUE}Creating kelox-prod environment (this takes ~10-15 minutes)...${NC}"

# Check if environment exists
ENV_EXISTS=$(eb list 2>/dev/null | grep "kelox-prod" || true)

if [ -n "$ENV_EXISTS" ]; then
    echo -e "${RED}❌ Environment kelox-prod already exists!${NC}"
    echo "Use ./deploy-to-prod.sh to deploy updates."
    exit 1
fi

# Create production environment with load balancer
eb create kelox-prod \
    --instance-type t3.small \
    --region $AWS_REGION \
    --envvars \
DB_URL="$DB_URL",\
DB_USERNAME=postgres,\
DB_PASSWORD="$DB_PASSWORD",\
ADMIN_SECRET_CODE="$ADMIN_SECRET",\
JWT_SECRET="$JWT_SECRET",\
SPRING_PROFILES_ACTIVE=prod

echo -e "${GREEN}✓ Environment created${NC}"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}✅ PRODUCTION Environment Setup Complete!${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📝 Environment Details:"
echo "  Environment: kelox-prod"
echo "  Database: kelox-prod-db (Multi-AZ, Encrypted)"
echo "  Instance Type: t3.small (Load Balanced)"
echo "  Region: $AWS_REGION"
echo ""
echo "🔐 Credentials (SAVE THESE SECURELY!):"
echo "  DB Endpoint: $DB_ENDPOINT"
echo "  DB Username: postgres"
echo "  DB Password: [HIDDEN]"
echo "  Admin Secret: $ADMIN_SECRET"
echo "  JWT Secret: $JWT_SECRET"
echo ""
echo -e "${RED}⚠️  IMPORTANT: Save these credentials to a secure location (e.g., AWS Secrets Manager)${NC}"
echo ""
echo "🌐 Application URL:"
eb status kelox-prod | grep "CNAME" | awk '{print "  https://"$3"/api"}' || echo "  (getting URL...)"
echo ""
echo "📊 Next Steps:"
echo "  1. Configure SSL certificate in AWS Certificate Manager"
echo "  2. Set up custom domain in Route 53"
echo "  3. Configure CloudWatch alarms"
echo "  4. Set up automated backups"
echo "  5. Review security groups and restrict database access"
echo "  6. Deploy updates: ./deploy-to-prod.sh"
echo ""
echo "💰 Estimated Cost: ~\$50-70/month"
echo ""

