# 🚀 Kelox Backend - AWS Deployment Guide

This guide provides step-by-step instructions for deploying the Kelox backend to AWS.

## 📋 Table of Contents
- [Prerequisites](#prerequisites)
- [Option 1: Elastic Beanstalk (Recommended)](#option-1-elastic-beanstalk-recommended)
- [Option 2: ECS/Fargate with Docker](#option-2-ecsfargate-with-docker)
- [Database Setup (RDS PostgreSQL)](#database-setup-rds-postgresql)
- [Environment Variables](#environment-variables)
- [Post-Deployment](#post-deployment)
- [Monitoring & Maintenance](#monitoring--maintenance)
- [Cost Estimation](#cost-estimation)

---

## Prerequisites

### 1. Install Required Tools

```bash
# AWS CLI
# Mac:
brew install awscli

# Windows:
# Download from https://aws.amazon.com/cli/

# Linux:
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Elastic Beanstalk CLI (for Option 1)
pip install awsebcli

# Docker (for Option 2)
# Download from https://docker.com
```

### 2. Configure AWS Credentials

```bash
aws configure
# Enter your:
# - AWS Access Key ID
# - AWS Secret Access Key
# - Default region (e.g., us-east-1)
# - Default output format (json)
```

### 3. Verify Installation

```bash
aws --version
eb --version  # For Elastic Beanstalk
docker --version  # For ECS/Fargate
```

---

## Option 1: Elastic Beanstalk (Recommended)

**✅ Best for:** Quick deployment, managed infrastructure, auto-scaling  
**⏱️ Setup Time:** 15-20 minutes  
**💰 Cost:** ~$15-30/month (t3.small instance + RDS)

### Step 1: Create RDS PostgreSQL Database

```bash
# Create RDS PostgreSQL instance
aws rds create-db-instance \
    --db-instance-identifier kelox-db \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --engine-version 15.4 \
    --master-username postgres \
    --master-user-password YOUR_SECURE_PASSWORD \
    --allocated-storage 20 \
    --storage-type gp3 \
    --publicly-accessible \
    --backup-retention-period 7 \
    --vpc-security-group-ids sg-xxxxxx \
    --db-name kelox_db

# Wait for database to be available (takes ~5-10 minutes)
aws rds wait db-instance-available --db-instance-identifier kelox-db

# Get database endpoint
aws rds describe-db-instances \
    --db-instance-identifier kelox-db \
    --query 'DBInstances[0].Endpoint.Address' \
    --output text
```

Save the database endpoint URL, you'll need it for environment variables.

### Step 2: Build Application

```bash
# Clean and build the JAR file
./gradlew clean bootJar -x test

# Verify the JAR was created
ls -lh build/libs/kelox-be-0.0.1-SNAPSHOT.jar
```

### Step 3: Initialize Elastic Beanstalk

```bash
# Initialize EB in your project directory
eb init

# Follow the prompts:
# 1. Select your region (e.g., us-east-1)
# 2. Enter application name: kelox-backend
# 3. Select platform: Java
# 4. Select platform branch: Corretto 17
# 5. Setup SSH: Yes (recommended for troubleshooting)
```

### Step 4: Create Elastic Beanstalk Environment

```bash
# Create environment with load balancer
eb create kelox-prod \
    --instance-type t3.small \
    --envvars \
    DB_URL=jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/kelox_db,\
    DB_USERNAME=postgres,\
    DB_PASSWORD=YOUR_SECURE_PASSWORD,\
    ADMIN_SECRET_CODE=your-admin-secret-change-this,\
    JWT_SECRET=your-very-long-and-secure-jwt-secret-key-change-this-in-production,\
    SPRING_PROFILES_ACTIVE=prod

# This takes 5-10 minutes
```

### Step 5: Deploy Application

```bash
# Deploy using the script
chmod +x deploy-elastic-beanstalk.sh
./deploy-elastic-beanstalk.sh

# Or deploy manually
eb deploy
```

### Step 6: Configure Health Check

The health check is already configured in `.ebextensions/01-environment.config` to use:
- Path: `/api/actuator/health`
- Interval: 30 seconds

### Step 7: Open Application

```bash
# Open in browser
eb open

# Your API will be available at:
# http://kelox-prod.us-east-1.elasticbeanstalk.com/api
```

### Useful Elastic Beanstalk Commands

```bash
# Check status
eb status

# View logs
eb logs

# SSH into instance
eb ssh

# Update environment variables
eb setenv DB_PASSWORD=new_password

# Scale instances
eb scale 2

# Terminate environment (cleanup)
eb terminate kelox-prod
```

---

## Option 2: ECS/Fargate with Docker

**✅ Best for:** Containerized deployments, microservices  
**⏱️ Setup Time:** 30-45 minutes  
**💰 Cost:** ~$20-40/month (Fargate + RDS)

### Step 1: Create RDS Database

Follow the same RDS creation steps from Option 1.

### Step 2: Build and Push Docker Image

```bash
# Build image locally
chmod +x deploy-docker.sh
./deploy-docker.sh

# Create ECR repository
aws ecr create-repository --repository-name kelox-backend

# Get login credentials
aws ecr get-login-password --region us-east-1 | \
    docker login --username AWS --password-stdin \
    YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# Tag image
docker tag kelox-backend:latest \
    YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/kelox-backend:latest

# Push to ECR
docker push YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/kelox-backend:latest
```

### Step 3: Create ECS Cluster

```bash
# Create ECS cluster
aws ecs create-cluster --cluster-name kelox-cluster
```

### Step 4: Create Task Definition

Create a file `ecs-task-definition.json`:

```json
{
  "family": "kelox-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "kelox-backend",
      "image": "YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/kelox-backend:latest",
      "portMappings": [
        {
          "containerPort": 5000,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "DB_URL",
          "value": "jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/kelox_db"
        },
        {
          "name": "DB_USERNAME",
          "value": "postgres"
        },
        {
          "name": "DB_PASSWORD",
          "value": "YOUR_SECURE_PASSWORD"
        },
        {
          "name": "ADMIN_SECRET_CODE",
          "value": "your-admin-secret"
        },
        {
          "name": "JWT_SECRET",
          "value": "your-jwt-secret"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/kelox-backend",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

Register the task:

```bash
aws ecs register-task-definition --cli-input-json file://ecs-task-definition.json
```

### Step 5: Create ALB and Service

This is complex - consider using AWS Console or AWS CDK/CloudFormation for easier setup.

---

## Database Setup (RDS PostgreSQL)

### Security Group Configuration

Ensure your RDS security group allows inbound traffic:

```bash
# Create security group
aws ec2 create-security-group \
    --group-name kelox-db-sg \
    --description "Security group for Kelox RDS"

# Allow PostgreSQL from your application
aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxx \
    --protocol tcp \
    --port 5432 \
    --source-group sg-yyyyyy  # Your EB/ECS security group
```

### Database Migration

Flyway migrations will run automatically on first startup. Ensure:
1. Database is accessible from your application
2. Credentials are correct
3. Database name matches (`kelox_db`)

### Backup Configuration

```bash
# Modify backup retention (recommended: 7-30 days)
aws rds modify-db-instance \
    --db-instance-identifier kelox-db \
    --backup-retention-period 7 \
    --apply-immediately
```

---

## Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://xxx.rds.amazonaws.com:5432/kelox_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `SecurePassword123!` |
| `ADMIN_SECRET_CODE` | Admin API secret | `kelox-admin-prod-secret-2024` |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | `very-long-secure-jwt-secret-key` |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `prod` |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_EXPIRATION` | JWT expiration in milliseconds | `2592000000` (30 days) |
| `SERVER_PORT` | Application port | `5000` |

### Setting Environment Variables

**Elastic Beanstalk:**
```bash
eb setenv KEY1=value1 KEY2=value2
```

**ECS:**
Add to task definition's `environment` array (see Step 4 above)

---

## Post-Deployment

### 1. Verify Health Check

```bash
# Get your application URL
eb status  # For Elastic Beanstalk

# Test health endpoint
curl https://your-app-url.com/api/actuator/health

# Expected response:
# {"status":"UP"}
```

### 2. Test API Endpoints

```bash
# Test base endpoint
curl https://your-app-url.com/api/health

# Test authentication
curl -X POST https://your-app-url.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

### 3. Configure Custom Domain (Optional)

```bash
# For Elastic Beanstalk:
# 1. Go to Route 53 and create hosted zone
# 2. Create CNAME record pointing to EB URL
# 3. Add SSL certificate via AWS Certificate Manager

# Update EB environment with custom domain
# Console: EB > Configuration > Network > Load Balancer
```

---

## Monitoring & Maintenance

### CloudWatch Logs

```bash
# View logs (Elastic Beanstalk)
eb logs --all

# Stream logs in real-time
eb logs --stream
```

### CloudWatch Metrics

Key metrics to monitor:
- **CPU Utilization**: Should be < 70%
- **Memory Usage**: Should be < 80%
- **Response Time**: Monitor latency
- **Error Rate**: Track 4xx/5xx errors
- **Database Connections**: Monitor pool usage

Set up alarms:
```bash
# Create CPU alarm
aws cloudwatch put-metric-alarm \
    --alarm-name kelox-high-cpu \
    --alarm-description "Alert when CPU exceeds 80%" \
    --metric-name CPUUtilization \
    --namespace AWS/EB \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold \
    --evaluation-periods 2
```

### Scaling

**Auto-scaling is configured in `.ebextensions/01-environment.config`:**
- Min instances: 1
- Max instances: 4
- Scale up: CPU > 75%
- Scale down: CPU < 25%

Adjust as needed:
```bash
eb config
# Edit auto-scaling settings
```

### Database Maintenance

```bash
# Create manual snapshot
aws rds create-db-snapshot \
    --db-instance-identifier kelox-db \
    --db-snapshot-identifier kelox-db-snapshot-$(date +%Y%m%d)

# View available snapshots
aws rds describe-db-snapshots \
    --db-instance-identifier kelox-db
```

---

## Cost Estimation

### Elastic Beanstalk Deployment

| Service | Configuration | Monthly Cost |
|---------|---------------|--------------|
| EC2 (t3.small) | 1 instance | ~$15 |
| RDS (db.t3.micro) | PostgreSQL 15, 20GB | ~$15 |
| Load Balancer | ALB | ~$16 |
| Data Transfer | 1GB out | ~$0.09 |
| CloudWatch | Basic monitoring | Free |
| **Total** | | **~$46/month** |

### Cost Optimization Tips

1. **Use Reserved Instances**: Save 30-40% with 1-year commitment
2. **Schedule downtime**: Stop dev environments outside business hours
3. **Use t3.micro for dev**: Reduce instance size for non-production
4. **Enable RDS Auto-scaling**: Only scale storage when needed
5. **Use S3 for static files**: Offload file storage

---

## Troubleshooting

### Common Issues

**1. Health Check Failing**
```bash
# Check logs
eb logs --all

# Verify health endpoint
eb ssh
curl localhost:5000/api/actuator/health
```

**2. Database Connection Issues**
```bash
# Verify security group rules
# Ensure RDS allows traffic from EB security group

# Test connection from instance
eb ssh
telnet YOUR_RDS_ENDPOINT 5432
```

**3. Application Not Starting**
```bash
# Check Java version
java -version  # Should be Java 17

# Verify environment variables
eb printenv

# Check application logs
eb logs --all | grep ERROR
```

**4. Out of Memory**
```bash
# Increase JVM memory in .ebextensions/01-environment.config
# Change: JVMOptions: '-Xmx1024m -Xms512m'

# Or upgrade instance type
eb scale --type t3.medium
```

---

## Security Best Practices

1. **Use AWS Secrets Manager** for sensitive data:
```bash
# Store secrets
aws secretsmanager create-secret \
    --name kelox/db/password \
    --secret-string "your-password"

# Reference in application (requires additional setup)
```

2. **Enable SSL/TLS**:
   - Use AWS Certificate Manager for free SSL certificates
   - Configure HTTPS listener in load balancer

3. **Restrict database access**:
   - Only allow connections from application security group
   - Use private subnets for RDS

4. **Enable WAF** (Web Application Firewall):
   - Protect against common web exploits
   - Add rate limiting

5. **Regular updates**:
```bash
# Update platform version
eb upgrade

# Update dependencies
./gradlew dependencies --refresh-dependencies
```

---

## Rolling Back

### Elastic Beanstalk

```bash
# List application versions
aws elasticbeanstalk describe-application-versions \
    --application-name kelox-backend

# Deploy previous version
eb deploy --version previous-version-label
```

### Database Rollback

```bash
# Restore from snapshot
aws rds restore-db-instance-from-db-snapshot \
    --db-instance-identifier kelox-db-restored \
    --db-snapshot-identifier kelox-db-snapshot-20240101
```

---

## Next Steps

1. ✅ Deploy application to AWS
2. ✅ Configure custom domain
3. ✅ Set up SSL certificate
4. ✅ Configure CloudWatch alarms
5. ✅ Set up automated backups
6. ✅ Configure CI/CD pipeline (GitHub Actions, AWS CodePipeline)
7. ✅ Set up staging environment
8. ✅ Configure CDN (CloudFront) for static assets

---

## Support & Resources

- **AWS Documentation**: https://docs.aws.amazon.com/elasticbeanstalk/
- **Spring Boot on AWS**: https://spring.io/guides/gs/spring-boot-aws/
- **Cost Calculator**: https://calculator.aws/
- **AWS Support**: https://aws.amazon.com/support/

---

**🎉 Congratulations!** Your Kelox backend is now running on AWS!

For questions or issues, please refer to the troubleshooting section or consult AWS documentation.

