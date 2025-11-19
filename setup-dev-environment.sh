#!/bin/bash

# Kelox Backend - Setup DEV Environment
# This script creates the development/test environment on AWS

set -e

echo "🧪 Kelox Backend - Setup DEV Environment"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

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

# Step 1: Create RDS Database for Dev
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Step 1: Create RDS PostgreSQL Database"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Ask for database password
echo "Enter a password for the DEV database (or press Enter for default 'kelox-dev-pass-2024'):"
read -sp "DB Password: " DB_PASSWORD
echo ""
if [ -z "$DB_PASSWORD" ]; then
    DB_PASSWORD="kelox-dev-pass-2024"
fi

echo ""
echo -e "${BLUE}Creating RDS instance (this takes ~5-10 minutes)...${NC}"

# Check if database already exists
DB_EXISTS=$(aws rds describe-db-instances --db-instance-identifier kelox-dev-db --region $AWS_REGION 2>/dev/null || echo "not_found")

if [[ "$DB_EXISTS" != "not_found" ]]; then
    echo -e "${YELLOW}⚠️  Database kelox-dev-db already exists!${NC}"
    echo ""
    DB_ENDPOINT=$(aws rds describe-db-instances \
        --db-instance-identifier kelox-dev-db \
        --region $AWS_REGION \
        --query 'DBInstances[0].Endpoint.Address' \
        --output text)
    echo -e "${GREEN}✓ Using existing database${NC}"
else
    # Create the database
    aws rds create-db-instance \
        --db-instance-identifier kelox-dev-db \
        --db-instance-class db.t3.micro \
        --engine postgres \
        --engine-version 15.4 \
        --master-username postgres \
        --master-user-password "$DB_PASSWORD" \
        --allocated-storage 20 \
        --storage-type gp3 \
        --publicly-accessible \
        --backup-retention-period 3 \
        --db-name kelox_db \
        --region $AWS_REGION \
        --tags Key=Environment,Value=dev Key=Project,Value=kelox

    echo ""
    echo -e "${BLUE}⏳ Waiting for database to be available (this may take 5-10 minutes)...${NC}"
    aws rds wait db-instance-available \
        --db-instance-identifier kelox-dev-db \
        --region $AWS_REGION

    DB_ENDPOINT=$(aws rds describe-db-instances \
        --db-instance-identifier kelox-dev-db \
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

# Step 4: Prepare dev configuration
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 Step 4: Prepare Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -d ".ebextensions-backup" ]; then
    rm -rf .ebextensions-backup
fi

if [ -d ".ebextensions" ]; then
    mv .ebextensions .ebextensions-backup
fi

cp -r .ebextensions-dev .ebextensions
echo -e "${GREEN}✓ Dev configuration prepared${NC}"

# Step 5: Create EB Environment
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 Step 5: Create EB Environment"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Generate secrets
ADMIN_SECRET="kelox-admin-dev-$(openssl rand -hex 8)"
JWT_SECRET="kelox-jwt-dev-$(openssl rand -hex 16)"

DB_URL="jdbc:postgresql://${DB_ENDPOINT}:5432/kelox_db"

echo -e "${BLUE}Creating kelox-dev environment (this takes ~5-10 minutes)...${NC}"

# Check if environment exists
ENV_EXISTS=$(eb list 2>/dev/null | grep "kelox-dev" || true)

if [ -n "$ENV_EXISTS" ]; then
    echo -e "${YELLOW}⚠️  Environment kelox-dev already exists!${NC}"
    echo ""
    read -p "Do you want to update it? (yes/no): " UPDATE_ENV
    
    if [ "$UPDATE_ENV" = "yes" ]; then
        eb use kelox-dev
        eb setenv \
            DB_URL="$DB_URL" \
            DB_USERNAME=postgres \
            DB_PASSWORD="$DB_PASSWORD" \
            ADMIN_SECRET_CODE="$ADMIN_SECRET" \
            JWT_SECRET="$JWT_SECRET" \
            SPRING_PROFILES_ACTIVE=dev
        
        echo -e "${GREEN}✓ Environment variables updated${NC}"
    fi
else
    # Create new environment with load balancer for HTTPS support
    eb create kelox-dev \
        --instance-type t3.micro \
        --region $AWS_REGION \
        --envvars \
DB_URL="$DB_URL",\
DB_USERNAME=postgres,\
DB_PASSWORD="$DB_PASSWORD",\
ADMIN_SECRET_CODE="$ADMIN_SECRET",\
JWT_SECRET="$JWT_SECRET",\
SPRING_PROFILES_ACTIVE=dev
    
    echo -e "${GREEN}✓ Environment created${NC}"
fi

# Restore production config
if [ -d ".ebextensions-backup" ]; then
    rm -rf .ebextensions
    mv .ebextensions-backup .ebextensions
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}✅ DEV Environment Setup Complete!${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📝 Environment Details:"
echo "  Environment: kelox-dev"
echo "  Database: kelox-dev-db"
echo "  Instance Type: t3.micro (Single Instance)"
echo "  Region: $AWS_REGION"
echo ""
echo "🔐 Credentials (SAVE THESE!):"
echo "  DB Endpoint: $DB_ENDPOINT"
echo "  DB Username: postgres"
echo "  DB Password: $DB_PASSWORD"
echo "  Admin Secret: $ADMIN_SECRET"
echo "  JWT Secret: $JWT_SECRET"
echo ""
echo "🌐 Application URL:"
eb status kelox-dev | grep "CNAME" | awk '{print "  http://"$3"/api"}' || echo "  (getting URL...)"
echo ""
echo "📊 Next Steps:"
echo "  1. Check status: eb health kelox-dev"
echo "  2. View logs: eb logs kelox-dev"
echo "  3. Open app: eb open kelox-dev"
echo "  4. Deploy updates: ./deploy-to-dev.sh"
echo ""
echo "💰 Estimated Cost: ~\$10-15/month"
echo ""

