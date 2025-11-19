# 🎯 START HERE - Kelox Backend Deployment

## Deploy to Dev (Test) Environment First

### Quick 3-Step Process

```bash
# 1️⃣ Install tools (one-time)
pip install awsebcli
aws configure  # Enter your AWS credentials

# 2️⃣ Setup dev environment (one-time, ~15 minutes)
./setup-dev-environment.sh

# 3️⃣ Deploy updates (after code changes)
./deploy-to-dev.sh
```

---

## 📋 What Happens When You Run Setup?

**`./setup-dev-environment.sh` will:**

1. ✅ Create PostgreSQL database on AWS RDS
2. ✅ Build your Spring Boot application  
3. ✅ Initialize AWS Elastic Beanstalk
4. ✅ Create development environment
5. ✅ Deploy your application
6. ✅ Configure health checks and logging

**Total time: ~15 minutes**  
**Total cost: ~$35-40/month** (includes load balancer for HTTPS support)

---

## 🚀 Deployment Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    DEVELOPMENT WORKFLOW                       │
└─────────────────────────────────────────────────────────────┘

1. Write Code Locally
   ↓
2. Test Locally (docker-compose up)
   ↓
3. Deploy to DEV Environment    ← YOU ARE HERE
   ./setup-dev-environment.sh   (first time)
   ./deploy-to-dev.sh          (updates)
   ↓
4. Test in DEV
   - Test API endpoints
   - Verify database migrations
   - Check logs
   ↓
5. Deploy to PRODUCTION
   ./setup-prod-environment.sh  (first time)
   ./deploy-to-prod.sh         (updates)
```

---

## 📁 Important Files

| File | Purpose | When to Use |
|------|---------|-------------|
| **setup-dev-environment.sh** | Setup dev via CLI (one-time) | Automated deployment |
| **deploy-to-dev.sh** | Deploy to dev via CLI | After code changes |
| **MANUAL_CONSOLE_DEPLOYMENT.md** | Deploy via AWS Console | Manual deployment ⭐ |
| **create-deployment-package.sh** | Build JAR for upload | Manual deployment |
| **ENV_VARIABLES_REFERENCE.md** | Environment variables guide | Setting up credentials |
| **setup-prod-environment.sh** | Setup production (one-time) | After testing in dev |
| **deploy-to-prod.sh** | Deploy to production | Release to users |

---

## 🔐 What You Need

### AWS Credentials

Get these from AWS IAM Console:
- **AWS Access Key ID**
- **AWS Secret Access Key**
- **Region** (e.g., us-east-1)

Set them up:
```bash
aws configure
```

### Permissions Required

Your AWS user needs:
- ElasticBeanstalk full access
- RDS full access
- EC2 (for instances)
- CloudWatch Logs

---

## 💡 Tips

### ✅ DO:
- Deploy to dev first
- Test thoroughly in dev
- Check logs after deployment
- Monitor costs in AWS Console

### ❌ DON'T:
- Deploy directly to production
- Skip testing in dev
- Use weak passwords for databases
- Ignore deployment errors

---

## 🆘 Need Help?

### Quick Troubleshooting

**"Command not found: aws"**
```bash
brew install awscli  # Mac
# or visit https://aws.amazon.com/cli/
```

**"Command not found: eb"**
```bash
pip install awsebcli
```

**"Access Denied" error**
```bash
# Check your AWS credentials
aws sts get-caller-identity

# Reconfigure if needed
aws configure
```

**"Build failed"**
```bash
# Make sure you can build locally first
./gradlew clean bootJar
```

### Documentation

1. **[DEV_DEPLOYMENT_GUIDE.md](./DEV_DEPLOYMENT_GUIDE.md)** - Complete dev deployment guide (start here!)
2. **[DEPLOYMENT_README.md](./DEPLOYMENT_README.md)** - Overview and common tasks
3. **[AWS_DEPLOYMENT_GUIDE.md](./AWS_DEPLOYMENT_GUIDE.md)** - Comprehensive AWS guide
4. **[QUICK_START_DEPLOYMENT.md](./QUICK_START_DEPLOYMENT.md)** - Quick reference

---

## ✅ Pre-Flight Checklist

Before running `./setup-dev-environment.sh`:

- [ ] AWS CLI installed (`aws --version`)
- [ ] EB CLI installed (`eb --version`)
- [ ] AWS credentials configured (`aws configure`)
- [ ] Can build locally (`./gradlew bootJar`)
- [ ] Read [DEV_DEPLOYMENT_GUIDE.md](./DEV_DEPLOYMENT_GUIDE.md)

---

## 🎯 Ready to Deploy?

Run this command:

```bash
./setup-dev-environment.sh
```

The script will guide you through the process!

---

## 📊 What You'll Get

After setup completes:

### Development Environment
- **Application URL**: `http://kelox-dev-xxx.elasticbeanstalk.com/api` (HTTPS after SSL setup)
- **Instance**: t3.micro with Application Load Balancer
- **Database**: PostgreSQL on RDS (db.t3.micro)
- **Monitoring**: CloudWatch logs
- **HTTPS**: Ready (requires SSL certificate setup)
- **Cost**: ~$35-40/month

### Useful Commands
```bash
eb health kelox-dev      # Check health
eb logs kelox-dev        # View logs
eb open kelox-dev        # Open in browser
eb ssh kelox-dev         # SSH into server
./deploy-to-dev.sh       # Deploy updates
```

---

**🚀 Let's deploy!** Run `./setup-dev-environment.sh` now.

Questions? Check [DEV_DEPLOYMENT_GUIDE.md](./DEV_DEPLOYMENT_GUIDE.md)

