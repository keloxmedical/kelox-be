# ⚡ Quick Start: Deploy to AWS in 15 Minutes

The fastest way to get your Kelox backend running on AWS.

## Prerequisites (5 minutes)

```bash
# 1. Install AWS CLI
brew install awscli  # Mac
# or download from https://aws.amazon.com/cli/

# 2. Install EB CLI
pip install awsebcli

# 3. Configure AWS credentials
aws configure
# Enter your Access Key ID, Secret Access Key, and region (e.g., us-east-1)
```

## Step 1: Create Database (5 minutes)

```bash
# Create RDS PostgreSQL (takes ~5 min to provision)
aws rds create-db-instance \
    --db-instance-identifier kelox-db \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --engine-version 15.4 \
    --master-username postgres \
    --master-user-password ChangeThisSecurePassword123! \
    --allocated-storage 20 \
    --publicly-accessible \
    --db-name kelox_db

# Wait for it to be ready
aws rds wait db-instance-available --db-instance-identifier kelox-db

# Get the endpoint URL (save this!)
aws rds describe-db-instances \
    --db-instance-identifier kelox-db \
    --query 'DBInstances[0].Endpoint.Address' \
    --output text
```

## Step 2: Deploy Application (5 minutes)

```bash
# Build the application
./gradlew clean bootJar -x test

# Initialize Elastic Beanstalk (one-time setup)
eb init -p "Corretto 17 running on 64bit Amazon Linux 2023" -r us-east-1 kelox-backend

# Create and deploy environment (replace YOUR_RDS_ENDPOINT)
eb create kelox-prod \
    --instance-type t3.small \
    --envvars \
    DB_URL=jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/kelox_db,\
    DB_USERNAME=postgres,\
    DB_PASSWORD=ChangeThisSecurePassword123!,\
    ADMIN_SECRET_CODE=change-this-admin-secret-$(openssl rand -hex 16),\
    JWT_SECRET=change-this-jwt-secret-$(openssl rand -hex 32),\
    SPRING_PROFILES_ACTIVE=prod

# Open your application
eb open
```

## Step 3: Verify Deployment (1 minute)

```bash
# Check health
eb health

# Test the API
curl $(eb status --verbose | grep CNAME | awk '{print $2}')/api/actuator/health

# View logs if needed
eb logs
```

## 🎉 Done!

Your application is now live at: `http://kelox-prod.REGION.elasticbeanstalk.com/api`

### Quick Commands

```bash
# Deploy updates
./deploy-elastic-beanstalk.sh

# View status
eb status

# Scale instances
eb scale 2

# Update environment variables
eb setenv KEY=value

# SSH into server
eb ssh

# View logs
eb logs --stream
```

## Security Checklist

Before going to production:

- [ ] Change `DB_PASSWORD` to a strong password
- [ ] Generate new `ADMIN_SECRET_CODE` and `JWT_SECRET`
- [ ] Configure custom domain with SSL
- [ ] Restrict RDS security group to only EB instances
- [ ] Enable RDS encryption at rest
- [ ] Set up CloudWatch alarms
- [ ] Configure automated backups
- [ ] Review `.ebextensions` settings

## Cost

- **Development**: ~$20/month (t3.micro for both EC2 and RDS)
- **Production**: ~$50/month (t3.small EC2, t3.micro RDS, ALB)

## Need Help?

See the detailed guide: [AWS_DEPLOYMENT_GUIDE.md](./AWS_DEPLOYMENT_GUIDE.md)

## Cleanup (When Done Testing)

```bash
# Terminate environment
eb terminate kelox-prod

# Delete RDS database
aws rds delete-db-instance \
    --db-instance-identifier kelox-db \
    --skip-final-snapshot
```

