# 🚀 Kelox Backend Deployment

Complete deployment solution for AWS with separate Dev and Production environments.

## 📦 What's Included

This repository contains everything you need to deploy to AWS:

### ✅ Configuration Files
- `application-dev.yml` - Development Spring Boot config
- `application-prod.yml` - Production Spring Boot config  
- `.ebextensions/` - Production Elastic Beanstalk config
- `.ebextensions-dev/` - Development Elastic Beanstalk config
- `Dockerfile` - Docker image for alternative deployment

### ✅ Deployment Scripts
- `setup-dev-environment.sh` - **Setup dev environment (run once)**
- `deploy-to-dev.sh` - Deploy updates to dev
- `setup-prod-environment.sh` - Setup production environment (run once)
- `deploy-to-prod.sh` - Deploy updates to production
- `deploy-docker.sh` - Build Docker image

### ✅ Documentation
- `DEV_DEPLOYMENT_GUIDE.md` - **Start here!** Complete dev deployment guide
- `AWS_DEPLOYMENT_GUIDE.md` - Comprehensive AWS guide
- `QUICK_START_DEPLOYMENT.md` - Quick reference

---

## 🎯 Quick Start

### For First-Time Deployment

**Step 1: Install Prerequisites**

```bash
# Install AWS CLI
brew install awscli  # Mac
# or visit https://aws.amazon.com/cli/

# Install EB CLI
pip install awsebcli

# Configure AWS
aws configure
```

**Step 2: Deploy to Dev Environment**

```bash
# Setup dev environment (one-time, ~15 minutes)
./setup-dev-environment.sh

# Your dev environment is now live! 🎉
```

**Step 3: Test in Dev**

```bash
# Check status
eb health kelox-dev

# View URL
eb status kelox-dev

# Test API
curl $(eb status kelox-dev | grep CNAME | awk '{print "http://"$3"/api/actuator/health"}')
```

**Step 4: Deploy to Production** (after testing in dev)

```bash
# Setup production environment (one-time, ~20 minutes)
./setup-prod-environment.sh

# Your production environment is now live! 🚀
```

---

## 🔄 Deploying Updates

After making code changes:

```bash
# Deploy to dev first
./deploy-to-dev.sh

# Test in dev, then deploy to production
./deploy-to-prod.sh
```

---

## 📊 Environment Overview

### Development Environment (`kelox-dev`)

**Purpose:** Testing and development  
**Cost:** ~$35-40/month

| Component | Configuration |
|-----------|--------------|
| **Application** | t3.micro instance (min 1, max 2) |
| **Database** | RDS PostgreSQL (db.t3.micro, 20GB) |
| **Load Balancer** | Application Load Balancer (for HTTPS) |
| **Auto-Scaling** | No |
| **Logging** | DEBUG level, 3-day retention |
| **Backups** | 3-day retention |

**When to use:**
- Feature development
- Testing new migrations
- API testing
- Integration testing

### Production Environment (`kelox-prod`)

**Purpose:** Live production traffic  
**Cost:** ~$55-75/month

| Component | Configuration |
|-----------|--------------|
| **Application** | Auto-scaling (1-4 t3.small instances) |
| **Database** | RDS PostgreSQL (db.t3.small, Multi-AZ, 50GB, Encrypted) |
| **Load Balancer** | Application Load Balancer (HTTPS) |
| **Auto-Scaling** | Yes (CPU-based) |
| **Logging** | INFO level, 7-day retention |
| **Backups** | 7-day retention |

**When to use:**
- Live user traffic
- After thorough testing in dev
- Stable releases only

---

## 📁 File Structure

```
kelox-be/
├── .ebextensions/              # Production EB config
│   ├── 01-environment.config
│   └── 02-nginx.config
│
├── .ebextensions-dev/          # Dev EB config
│   ├── 01-environment.config
│   └── 02-nginx.config
│
├── src/main/resources/
│   ├── application.yml         # Local development
│   ├── application-dev.yml     # AWS Dev environment
│   └── application-prod.yml    # AWS Production environment
│
├── setup-dev-environment.sh    # Setup dev (run once)
├── deploy-to-dev.sh           # Deploy updates to dev
├── setup-prod-environment.sh  # Setup prod (run once)
├── deploy-to-prod.sh          # Deploy updates to prod
├── deploy-docker.sh           # Build Docker image
│
├── Dockerfile                 # Docker configuration
├── .dockerignore             # Docker ignore rules
├── .ebignore                 # EB ignore rules
│
└── Documentation/
    ├── DEV_DEPLOYMENT_GUIDE.md       # Dev deployment guide
    ├── AWS_DEPLOYMENT_GUIDE.md       # Full AWS guide
    ├── QUICK_START_DEPLOYMENT.md     # Quick reference
    └── DEPLOYMENT_README.md          # This file
```

---

## 🔐 Environment Variables

### Development

Set automatically by `setup-dev-environment.sh`:

```bash
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:postgresql://[dev-db-endpoint]:5432/kelox_db
DB_USERNAME=postgres
DB_PASSWORD=[auto-generated or your input]
ADMIN_SECRET_CODE=[auto-generated]
JWT_SECRET=[auto-generated]
```

### Production

Set automatically by `setup-prod-environment.sh`:

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://[prod-db-endpoint]:5432/kelox_db
DB_USERNAME=postgres
DB_PASSWORD=[strong password required]
ADMIN_SECRET_CODE=[auto-generated, longer]
JWT_SECRET=[auto-generated, longer]
```

### Update Variables

```bash
# Dev
eb use kelox-dev
eb setenv KEY=value

# Production
eb use kelox-prod
eb setenv KEY=value
```

---

## 🛠️ Common Tasks

### View Logs

```bash
# Dev
eb logs kelox-dev
eb logs kelox-dev --stream  # Real-time

# Production
eb logs kelox-prod
eb logs kelox-prod --stream
```

### Check Application Status

```bash
# Dev
eb status kelox-dev
eb health kelox-dev

# Production
eb status kelox-prod
eb health kelox-prod
```

### SSH into Server

```bash
# Dev
eb ssh kelox-dev

# Production
eb ssh kelox-prod
```

### Database Operations

```bash
# Get database endpoint
aws rds describe-db-instances \
    --db-instance-identifier kelox-dev-db \
    --query 'DBInstances[0].Endpoint.Address'

# Create snapshot
aws rds create-db-snapshot \
    --db-instance-identifier kelox-dev-db \
    --db-snapshot-identifier backup-$(date +%Y%m%d)

# Connect to database
psql -h [endpoint] -U postgres -d kelox_db
```

### Scale Production

```bash
# Scale instances
eb scale 3  # Scale to 3 instances

# Change instance type
eb scale --type t3.medium
```

---

## 🔍 Troubleshooting

### Application Won't Start

```bash
# Check logs
eb logs kelox-dev | grep ERROR

# Common fixes:
# 1. Verify database connection
eb printenv kelox-dev | grep DB

# 2. Check Flyway migrations
eb ssh kelox-dev
sudo tail -f /var/log/web.stdout.log

# 3. Verify Java version
java -version  # Should be Java 17
```

### Database Connection Failed

```bash
# 1. Check database status
aws rds describe-db-instances --db-instance-identifier kelox-dev-db

# 2. Test connection from instance
eb ssh kelox-dev
telnet [db-endpoint] 5432

# 3. Update security group if needed
```

### Deployment Fails

```bash
# View deployment events
eb events kelox-dev --follow

# Check for:
# - Build failures
# - Health check failures
# - Timeout issues
```

### Out of Memory

```bash
# For dev: Upgrade to t3.small
eb scale kelox-dev --type t3.small

# Or adjust JVM settings in .ebextensions-dev/01-environment.config
```

---

## 💰 Cost Breakdown

### Development (~$39/month)
- EC2 t3.micro: $7.50
- RDS db.t3.micro: $12
- Application Load Balancer: $16
- Storage 20GB: $2.30
- Data transfer: $1

### Production (~$55-75/month base)
- EC2 t3.small (1-4): $15-60
- RDS db.t3.small Multi-AZ: $25
- Application Load Balancer: $16
- Storage 50GB: $5.75
- Data transfer: $2

### Cost Optimization
1. **Dev**: Stop when not in use (save 60%)
2. **Prod**: Use Reserved Instances (save 30%)
3. **Both**: Monitor CloudWatch metrics to right-size

---

## 📈 Monitoring

### CloudWatch

Automatically configured for:
- Application logs
- Database metrics
- Instance metrics
- Load balancer metrics

Access via AWS Console: CloudWatch > Logs & Metrics

### Health Checks

```bash
# Manual health check
curl https://your-domain/api/actuator/health

# Automated checks run every 30 seconds
# Configured in .ebextensions/01-environment.config
```

### Alerts

Set up CloudWatch alarms for:
- High CPU usage (>80%)
- High memory usage (>80%)
- Database connection failures
- 5xx error rates

---

## 🔄 CI/CD Integration (Optional)

### GitHub Actions Example

```yaml
# .github/workflows/deploy-dev.yml
name: Deploy to Dev
on:
  push:
    branches: [develop]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup Java
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build
        run: ./gradlew bootJar
      - name: Deploy to EB
        run: |
          pip install awsebcli
          eb use kelox-dev
          eb deploy
```

---

## 🗑️ Cleanup

### Delete Dev Environment

```bash
# Terminate environment
eb terminate kelox-dev

# Delete database
aws rds delete-db-instance \
    --db-instance-identifier kelox-dev-db \
    --final-db-snapshot-identifier kelox-dev-final
```

### Delete Production Environment

```bash
# ⚠️ WARNING: This deletes your production environment!

# Terminate environment
eb terminate kelox-prod

# Delete database (creates final snapshot)
aws rds delete-db-instance \
    --db-instance-identifier kelox-prod-db \
    --final-db-snapshot-identifier kelox-prod-final
```

---

## 📚 Documentation Links

- **[DEV_DEPLOYMENT_GUIDE.md](./DEV_DEPLOYMENT_GUIDE.md)** - Complete guide for dev deployment
- **[AWS_DEPLOYMENT_GUIDE.md](./AWS_DEPLOYMENT_GUIDE.md)** - Comprehensive AWS guide
- **[QUICK_START_DEPLOYMENT.md](./QUICK_START_DEPLOYMENT.md)** - Quick reference

---

## 🆘 Support

### Common Resources
- [AWS Elastic Beanstalk Docs](https://docs.aws.amazon.com/elasticbeanstalk/)
- [AWS RDS Docs](https://docs.aws.amazon.com/rds/)
- [Spring Boot on AWS](https://spring.io/guides/gs/spring-boot-aws/)

### Troubleshooting
1. Check the [DEV_DEPLOYMENT_GUIDE.md](./DEV_DEPLOYMENT_GUIDE.md#troubleshooting)
2. View EB logs: `eb logs kelox-dev`
3. Check CloudWatch logs in AWS Console
4. SSH into instance: `eb ssh kelox-dev`

---

## ✅ Deployment Checklist

### Before First Deployment
- [ ] AWS CLI installed
- [ ] EB CLI installed
- [ ] AWS credentials configured
- [ ] Read DEV_DEPLOYMENT_GUIDE.md

### Dev Deployment
- [ ] Run `./setup-dev-environment.sh`
- [ ] Test health endpoint
- [ ] Test API endpoints
- [ ] Check logs for errors
- [ ] Verify database migrations

### Production Deployment
- [ ] Tested thoroughly in dev
- [ ] All tests passing
- [ ] Run `./setup-prod-environment.sh`
- [ ] Configure custom domain
- [ ] Set up SSL certificate
- [ ] Configure CloudWatch alarms
- [ ] Test critical endpoints
- [ ] Monitor for 24 hours

---

**🚀 Ready to deploy?** Start with [DEV_DEPLOYMENT_GUIDE.md](./DEV_DEPLOYMENT_GUIDE.md)!

