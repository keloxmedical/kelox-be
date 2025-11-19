# 🧪 Kelox Backend - DEV Environment Deployment Guide

**Deploy to DEV/Test environment first before going to production!**

This guide walks you through setting up and deploying to your development environment on AWS.

## 🎯 Quick Start - Deploy to DEV in 15 Minutes

```bash
# 1. Install prerequisites (one-time)
pip install awsebcli

# 2. Configure AWS
aws configure

# 3. Setup dev environment (one-time, ~15 minutes)
./setup-dev-environment.sh

# 4. Deploy updates (after code changes)
./deploy-to-dev.sh
```

---

## 📋 Table of Contents
- [Prerequisites](#prerequisites)
- [Step-by-Step Setup](#step-by-step-setup)
- [Deploying Updates](#deploying-updates)
- [Testing Your Deployment](#testing-your-deployment)
- [Dev vs Prod Differences](#dev-vs-prod-differences)
- [Troubleshooting](#troubleshooting)
- [Cost Management](#cost-management)

---

## Prerequisites

### 1. Install Required Tools

```bash
# AWS CLI
brew install awscli  # Mac
# or download from https://aws.amazon.com/cli/

# Elastic Beanstalk CLI
pip install awsebcli

# Verify installations
aws --version
eb --version
```

### 2. Configure AWS Credentials

```bash
aws configure
# Enter:
# - AWS Access Key ID
# - AWS Secret Access Key  
# - Default region (e.g., us-east-1)
# - Default output format (json)
```

### 3. Test AWS Connection

```bash
# Verify your AWS credentials work
aws sts get-caller-identity

# Should show your account ID and user ARN
```

---

## Step-by-Step Setup

### One-Time Setup (Run Once)

#### Option A: Automated Setup (Recommended)

```bash
# Make the script executable
chmod +x setup-dev-environment.sh

# Run the setup script
./setup-dev-environment.sh
```

The script will:
1. ✅ Create RDS PostgreSQL database (`kelox-dev-db`)
2. ✅ Build your application
3. ✅ Initialize Elastic Beanstalk
4. ✅ Create dev environment (`kelox-dev`)
5. ✅ Configure environment variables
6. ✅ Deploy your application

**⏱️ Total time: ~15-20 minutes**

#### Option B: Manual Setup

If you prefer manual control, follow these steps:

**1. Create RDS Database**

```bash
# Create PostgreSQL database for dev
aws rds create-db-instance \
    --db-instance-identifier kelox-dev-db \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --engine-version 15.4 \
    --master-username postgres \
    --master-user-password YOUR_DEV_PASSWORD \
    --allocated-storage 20 \
    --storage-type gp3 \
    --publicly-accessible \
    --backup-retention-period 3 \
    --db-name kelox_db \
    --tags Key=Environment,Value=dev

# Wait for database to be ready (~5-10 minutes)
aws rds wait db-instance-available --db-instance-identifier kelox-dev-db

# Get database endpoint
DB_ENDPOINT=$(aws rds describe-db-instances \
    --db-instance-identifier kelox-dev-db \
    --query 'DBInstances[0].Endpoint.Address' \
    --output text)

echo "Database endpoint: $DB_ENDPOINT"
```

**2. Build Application**

```bash
./gradlew clean bootJar -x test
```

**3. Initialize Elastic Beanstalk**

```bash
# Initialize EB (one-time)
eb init -p "Corretto 17 running on 64bit Amazon Linux 2023" \
    -r us-east-1 kelox-backend
```

**4. Create Dev Environment**

```bash
# Copy dev configuration
rm -rf .ebextensions
cp -r .ebextensions-dev .ebextensions

# Create environment
eb create kelox-dev \
    --instance-type t3.micro \
    --single \
    --envvars \
    DB_URL=jdbc:postgresql://$DB_ENDPOINT:5432/kelox_db,\
    DB_USERNAME=postgres,\
    DB_PASSWORD=YOUR_DEV_PASSWORD,\
    ADMIN_SECRET_CODE=dev-admin-secret,\
    JWT_SECRET=dev-jwt-secret-change-this,\
    SPRING_PROFILES_ACTIVE=dev

# Restore prod configuration
rm -rf .ebextensions
git checkout .ebextensions
```

---

## Deploying Updates

After making code changes, deploy to dev:

```bash
# Build and deploy to dev
./deploy-to-dev.sh
```

Or manually:

```bash
# Build
./gradlew clean bootJar -x test

# Prepare dev config
rm -rf .ebextensions
cp -r .ebextensions-dev .ebextensions

# Deploy
eb use kelox-dev
eb deploy kelox-dev

# Restore prod config
rm -rf .ebextensions
git checkout .ebextensions
```

---

## Testing Your Deployment

### 1. Check Application Health

```bash
# Check overall environment health
eb health kelox-dev

# Get application URL
eb status kelox-dev

# Open in browser
eb open kelox-dev
```

### 2. Test API Endpoints

```bash
# Get your dev URL
DEV_URL=$(eb status kelox-dev | grep CNAME | awk '{print $3}')

# Test health endpoint
curl http://$DEV_URL/api/actuator/health

# Expected response:
# {"status":"UP","components":{"db":{"status":"UP"},...}}

# Test a simple endpoint
curl http://$DEV_URL/api/health

# Test authentication endpoint
curl -X POST http://$DEV_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"privyDid":"test","signature":"test"}'
```

### 3. View Logs

```bash
# View recent logs
eb logs kelox-dev

# Stream logs in real-time
eb logs kelox-dev --stream

# Download full logs
eb logs kelox-dev --all --zip
```

### 4. SSH into Instance (if needed)

```bash
# SSH into the EC2 instance
eb ssh kelox-dev

# Once inside, you can:
# - Check Java version: java -version
# - View logs: sudo tail -f /var/log/web.stdout.log
# - Check processes: ps aux | grep java
# - Test local health: curl localhost:5000/api/actuator/health
```

---

## Dev vs Prod Differences

| Feature | Dev Environment | Production Environment |
|---------|----------------|------------------------|
| **Environment Name** | `kelox-dev` | `kelox-prod` |
| **Database** | `kelox-dev-db` (t3.micro) | `kelox-prod-db` (t3.small, Multi-AZ) |
| **Instance Type** | t3.micro (1 vCPU, 1GB RAM) | t3.small (2 vCPU, 2GB RAM) |
| **Instances** | Min 1, Max 2 | Auto-scaling (1-4 instances) |
| **Load Balancer** | Yes (ALB for HTTPS support) | Yes (Application Load Balancer) |
| **JVM Memory** | -Xmx256m -Xms128m | -Xmx512m -Xms256m |
| **Logging Level** | DEBUG | INFO |
| **SQL Logging** | Enabled (show-sql: true) | Disabled (show-sql: false) |
| **Log Retention** | 3 days | 7 days |
| **DB Backups** | 3 days | 7 days |
| **DB Encryption** | No | Yes |
| **Management Endpoints** | All exposed | Limited (health, info, metrics) |
| **Health Details** | Always shown | When authorized |
| **Cost** | ~$35-40/month | ~$55-70/month |

### Configuration Files

**Dev:** `.ebextensions-dev/`
- Optimized for cost savings
- Single instance
- Verbose logging
- Lower resource limits

**Prod:** `.ebextensions/`
- Optimized for reliability
- Auto-scaling
- Minimal logging
- Higher resource limits

---

## Troubleshooting

### Application Not Starting

```bash
# Check environment status
eb status kelox-dev

# View logs for errors
eb logs kelox-dev | grep ERROR

# Common issues:
# 1. Database connection failed
#    - Verify DB_URL, DB_USERNAME, DB_PASSWORD
#    - Check security group allows EB instance
# 2. Flyway migration failed
#    - Check database is accessible
#    - Verify migrations are valid
# 3. Port binding error
#    - Ensure SERVER_PORT=5000 in env vars
```

### Database Connection Issues

```bash
# Get database security group
aws rds describe-db-instances \
    --db-instance-identifier kelox-dev-db \
    --query 'DBInstances[0].VpcSecurityGroups'

# Get EB instance security group
eb ssh kelox-dev
curl ifconfig.me  # Get instance public IP

# Update RDS security group to allow EB instance
```

### Health Check Failing

```bash
# SSH into instance and test locally
eb ssh kelox-dev
curl localhost:5000/api/actuator/health

# If it works locally but not externally:
# 1. Check security group allows inbound on port 80
# 2. Verify health check path in .ebextensions-dev/01-environment.config
# 3. Ensure application is listening on correct port
```

### Deployment Takes Too Long

```bash
# Check deployment events
eb events kelox-dev --follow

# Common causes:
# 1. Flyway migrations running
# 2. Instance provisioning
# 3. Health check waiting for app to start
```

### Out of Memory

```bash
# View current JVM settings
eb config kelox-dev

# Upgrade instance size if needed
eb scale kelox-dev --type t3.small

# Or reduce JVM memory in .ebextensions-dev/01-environment.config
# JVMOptions: '-Xmx128m -Xms64m'
```

---

## Managing Environment Variables

### View Current Variables

```bash
eb printenv kelox-dev
```

### Update Variables

```bash
# Update single variable
eb setenv DB_PASSWORD=new_password

# Update multiple variables
eb setenv \
    ADMIN_SECRET_CODE=new_admin_secret \
    JWT_SECRET=new_jwt_secret
```

### Add New Variables

```bash
eb setenv NEW_VARIABLE=value
```

---

## Database Management

### Connect to Database

```bash
# Get database endpoint
DB_ENDPOINT=$(aws rds describe-db-instances \
    --db-instance-identifier kelox-dev-db \
    --query 'DBInstances[0].Endpoint.Address' \
    --output text)

# Connect using psql
psql -h $DB_ENDPOINT -U postgres -d kelox_db
```

### Create Snapshot

```bash
# Create manual snapshot
aws rds create-db-snapshot \
    --db-instance-identifier kelox-dev-db \
    --db-snapshot-identifier kelox-dev-snapshot-$(date +%Y%m%d)
```

### Restore from Snapshot

```bash
# List snapshots
aws rds describe-db-snapshots \
    --db-instance-identifier kelox-dev-db

# Restore
aws rds restore-db-instance-from-db-snapshot \
    --db-instance-identifier kelox-dev-db-restored \
    --db-snapshot-identifier kelox-dev-snapshot-20240101
```

---

## Cost Management

### Dev Environment Costs

| Service | Cost/Month |
|---------|------------|
| EC2 (t3.micro) | ~$7.50 |
| RDS (db.t3.micro) | ~$12 |
| Application Load Balancer | ~$16 |
| Storage (20GB) | ~$2.30 |
| Data Transfer | ~$1 |
| **Total** | **~$39/month** |

**Note:** Load balancer is required for HTTPS support.

### Cost Optimization Tips

**1. Stop Dev Environment When Not in Use**

```bash
# Stop environment (keeps configuration, stops instances)
# Note: This is not directly supported by EB CLI
# Alternative: Terminate and recreate

# Terminate environment
eb terminate kelox-dev

# Recreate when needed
./setup-dev-environment.sh
```

**2. Use Smaller Database**

The dev environment already uses `db.t3.micro` (smallest), but you can:
- Reduce allocated storage to 20GB (already configured)
- Reduce backup retention to 1 day
- Use gp2 instead of gp3 (saves ~$0.50/month)

**3. Schedule On/Off Hours**

For dev that's only used during business hours, use AWS Systems Manager to:
- Start instances at 9 AM
- Stop instances at 6 PM
- Save ~60% on EC2 costs

---

## Monitoring

### CloudWatch Logs

```bash
# Logs are automatically streamed to CloudWatch
# View in AWS Console:
# CloudWatch > Logs > /aws/elasticbeanstalk/kelox-dev/
```

### Basic Metrics

```bash
# View environment health
eb health kelox-dev --refresh

# Available metrics in CloudWatch:
# - CPUUtilization
# - NetworkIn/Out
# - StatusCheckFailed
# - DatabaseConnections
```

---

## Moving to Production

After testing in dev, deploy to production:

```bash
# 1. Setup production environment (one-time)
./setup-prod-environment.sh

# 2. Deploy to production
./deploy-to-prod.sh
```

See [AWS_DEPLOYMENT_GUIDE.md](./AWS_DEPLOYMENT_GUIDE.md) for production deployment details.

---

## Cleanup

When you're done with dev environment:

```bash
# Terminate EB environment
eb terminate kelox-dev

# Delete RDS database
aws rds delete-db-instance \
    --db-instance-identifier kelox-dev-db \
    --skip-final-snapshot

# Or create final snapshot first
aws rds delete-db-instance \
    --db-instance-identifier kelox-dev-db \
    --final-db-snapshot-identifier kelox-dev-final-snapshot
```

---

## Useful Commands Reference

```bash
# Environment Management
eb list                          # List all environments
eb use kelox-dev                 # Switch to dev environment
eb status kelox-dev              # Show environment status
eb health kelox-dev              # Show health status
eb events kelox-dev              # Show recent events
eb open kelox-dev                # Open app in browser

# Deployment
./deploy-to-dev.sh              # Deploy to dev
eb deploy kelox-dev             # Deploy manually

# Logs
eb logs kelox-dev               # View recent logs
eb logs kelox-dev --stream      # Stream logs in real-time
eb logs kelox-dev --all         # Download all logs

# Configuration
eb config kelox-dev             # Edit configuration
eb printenv kelox-dev           # Show environment variables
eb setenv KEY=value             # Set environment variable

# SSH
eb ssh kelox-dev                # SSH into instance

# Scaling
eb scale kelox-dev --size 2     # Scale to 2 instances (changes to LoadBalanced)
eb scale kelox-dev --type t3.small  # Change instance type

# Database
aws rds describe-db-instances --db-instance-identifier kelox-dev-db
aws rds create-db-snapshot --db-instance-identifier kelox-dev-db --db-snapshot-identifier name
```

---

## Next Steps

1. ✅ Deploy to dev environment
2. ✅ Test all API endpoints
3. ✅ Verify database migrations
4. ✅ Test authentication flow
5. ✅ Check logs for errors
6. ✅ Monitor performance
7. ✅ When satisfied, deploy to production

---

## Support

- **AWS EB Documentation**: https://docs.aws.amazon.com/elasticbeanstalk/
- **Troubleshooting**: See [AWS_DEPLOYMENT_GUIDE.md](./AWS_DEPLOYMENT_GUIDE.md#troubleshooting)
- **Cost Calculator**: https://calculator.aws/

---

**🎉 You're ready to deploy to dev!** Run `./setup-dev-environment.sh` to get started.

