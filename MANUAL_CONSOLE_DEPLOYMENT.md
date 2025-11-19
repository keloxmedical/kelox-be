# 🖱️ Manual Deployment via AWS Console

Complete guide for building and deploying your Spring Boot application manually through the AWS Console.

---

## 📋 Table of Contents
- [Step 1: Build the Application](#step-1-build-the-application)
- [Step 2: Create RDS Database](#step-2-create-rds-database)
- [Step 3: Create Elastic Beanstalk Application](#step-3-create-elastic-beanstalk-application)
- [Step 4: Set Environment Variables](#step-4-set-environment-variables)
- [Step 5: Upload and Deploy](#step-5-upload-and-deploy)
- [Step 6: Test Your Deployment](#step-6-test-your-deployment)
- [Updating Your Application](#updating-your-application)

---

## Step 1: Build the Application

### 1.1 Build the JAR File

Open terminal in your project directory and run:

```bash
# Clean and build the application
./gradlew clean bootJar -x test

# The JAR file will be created at:
# build/libs/kelox-be-0.0.1-SNAPSHOT.jar
```

### 1.2 Verify the Build

```bash
# Check the JAR file exists
ls -lh build/libs/kelox-be-0.0.1-SNAPSHOT.jar

# You should see something like:
# -rw-r--r--  1 user  staff    45M Nov 13 10:00 kelox-be-0.0.1-SNAPSHOT.jar
```

### 1.3 Prepare Deployment Package

For Elastic Beanstalk, you can upload the JAR directly, or create a ZIP with configuration:

**Option A: Upload JAR directly (Simple)**
```bash
# Just use the JAR file as-is
cp build/libs/kelox-be-0.0.1-SNAPSHOT.jar ~/Desktop/kelox-backend.jar
```

**Option B: Create ZIP with EB configuration (Recommended)**
```bash
# Create a deployment package with configuration
cd build/libs
zip ~/Desktop/kelox-backend.zip kelox-be-0.0.1-SNAPSHOT.jar

# Add .ebextensions for configuration
cd ../..
zip -r ~/Desktop/kelox-backend.zip .ebextensions-dev/

# Or for production
zip -r ~/Desktop/kelox-backend-prod.zip build/libs/kelox-be-0.0.1-SNAPSHOT.jar .ebextensions/
```

**Option C: Use the prepared script**
```bash
# Create a deployment package script
./create-deployment-package.sh
# This will create kelox-backend-dev.zip and kelox-backend-prod.zip
```

---

## Step 2: Create RDS Database

### 2.1 Navigate to RDS Console

1. Go to [AWS Console](https://console.aws.amazon.com)
2. Search for "RDS" in the top search bar
3. Click **"RDS"** to open the RDS console

### 2.2 Create Database

1. Click **"Create database"** button
2. Choose database creation method: **Standard create**
3. Engine options:
   - Engine type: **PostgreSQL**
   - Version: **PostgreSQL 15.4** (or latest 15.x)

### 2.3 Templates

For **Dev Environment:**
- Select: **Free tier** (if eligible) or **Dev/Test**

For **Production:**
- Select: **Production**

### 2.4 Settings

**Dev Database:**
```
DB instance identifier: kelox-dev-db
Master username: postgres
Master password: [Your secure password - SAVE THIS!]
Confirm password: [Same password]
```

**Production Database:**
```
DB instance identifier: kelox-prod-db
Master username: postgres
Master password: [Strong password - SAVE THIS!]
Confirm password: [Same password]
```

### 2.5 Instance Configuration

**Dev:**
- DB instance class: **Burstable classes (includes t classes)**
- Select: **db.t3.micro**

**Production:**
- DB instance class: **Burstable classes**
- Select: **db.t3.small**
- Multi-AZ deployment: **Yes** (for high availability)

### 2.6 Storage

**Dev:**
- Storage type: **General Purpose SSD (gp3)**
- Allocated storage: **20 GiB**
- Storage autoscaling: **Disable** (or enable with max 50 GiB)

**Production:**
- Storage type: **General Purpose SSD (gp3)**
- Allocated storage: **50 GiB**
- Storage autoscaling: **Enable**
- Maximum storage threshold: **100 GiB**

### 2.7 Connectivity

- Virtual private cloud (VPC): **Default VPC**
- Public access: **Yes** (so Elastic Beanstalk can connect)
- VPC security group: **Create new** or select existing
  - Name: `kelox-dev-db-sg` or `kelox-prod-db-sg`

### 2.8 Additional Configuration

**Database options:**
- Initial database name: **kelox_db** (IMPORTANT!)
- DB parameter group: Default
- Option group: Default

**Backup:**
- Dev: **3 days** retention
- Production: **7 days** retention
- Enable automated backups: **Yes**

**Encryption:**
- Dev: **Not required** (save cost)
- Production: **Enable encryption** (recommended)

**Monitoring:**
- Enable Enhanced Monitoring: **Optional** (adds cost)

### 2.9 Create Database

1. Review all settings
2. Click **"Create database"**
3. Wait 5-10 minutes for database to be available

### 2.10 Get Database Endpoint

1. Go to **RDS > Databases**
2. Click on your database (kelox-dev-db or kelox-prod-db)
3. Find **"Endpoint & port"** section
4. Copy the **Endpoint** - it looks like:
   ```
   kelox-dev-db.xxxxxxxxxx.us-east-1.rds.amazonaws.com
   ```
5. **SAVE THIS!** You'll need it for environment variables

---

## Step 3: Create Elastic Beanstalk Application

### 3.1 Navigate to Elastic Beanstalk

1. Go to [AWS Console](https://console.aws.amazon.com)
2. Search for "Elastic Beanstalk"
3. Click **"Elastic Beanstalk"** to open the console

### 3.2 Create Application

1. Click **"Create application"** button

### 3.3 Application Information

```
Application name: kelox-backend
Application tags (optional):
  - Key: Project, Value: kelox
  - Key: Owner, Value: [Your name]
```

### 3.4 Environment Information

**For Dev:**
```
Environment name: kelox-dev
Domain: kelox-dev (or leave auto-generated)
Description: Development environment for Kelox backend
```

**For Production:**
```
Environment name: kelox-prod
Domain: kelox-prod (or leave auto-generated)
Description: Production environment for Kelox backend
```

### 3.5 Platform

1. Platform type: **Managed platform**
2. Platform: **Java**
3. Platform branch: **Corretto 17 running on 64bit Amazon Linux 2023**
4. Platform version: **Recommended** (latest)

### 3.6 Application Code

**Option 1: Upload now**
1. Select: **Upload your code**
2. Version label: `v1.0` or `kelox-backend-v1`
3. Source code origin: **Local file**
4. Click **"Choose file"**
5. Select your JAR file or ZIP: `kelox-backend.jar` or `kelox-backend.zip`

**Option 2: Upload later**
1. Select: **Sample application**
2. Click **"Next"** (you'll upload your code after environment is created)

### 3.7 Configure Service Access

1. Click **"Next"**

**Service role:**
- Use an existing service role: Select if you have one
- Or create and use new service role: **aws-elasticbeanstalk-service-role**

**EC2 key pair:**
- Select existing key pair (for SSH access) or "Proceed without key pair"

**EC2 instance profile:**
- Select: **aws-elasticbeanstalk-ec2-role**
- Or create new if doesn't exist

### 3.8 Set up Networking, Database, and Tags

1. Click **"Next"**

**VPC:**
- Select your **Default VPC**

**Instance subnets:**
- Check **at least 2 availability zones** (for load balancer)

**Database:** 
- **Skip this** (we already created RDS separately)

**Tags (optional):**
- Key: Environment, Value: dev (or production)
- Key: Project, Value: kelox

### 3.9 Configure Instance Traffic and Scaling

1. Click **"Next"**

**Root volume:**
- Type: **General Purpose (SSD)**
- Size: **10 GB**

**EC2 security groups:**
- Select or create: **Default**

**Instance types:**

**For Dev:**
- Select: **t3.micro**

**For Production:**
- Select: **t3.small**

**Load balancer:**
- Environment type: **Load balanced**
- Load balancer type: **Application Load Balancer**

**Capacity:**

**For Dev:**
- Auto scaling group
  - Min instances: **1**
  - Max instances: **2**
- Fleet composition: **On-Demand instances**
- Architecture: **x86_64**

**For Production:**
- Auto scaling group
  - Min instances: **1**
  - Max instances: **4**
- Fleet composition: **On-Demand instances**
- Scaling triggers:
  - Metric: **CPUUtilization**
  - Unit: **Percent**
  - Upper threshold: **75**
  - Lower threshold: **25**

### 3.10 Configure Updates, Monitoring, and Logging

1. Click **"Next"**

**Monitoring:**
- Health reporting: **Enhanced**
- Managed platform updates: **Enabled (recommended)**

**Rolling updates:**
- Deployment policy: **Rolling**
- Batch size: **50%**

**Platform updates:**
- Enable managed updates: **Yes**
- Maintenance window: Choose a time with low traffic

**CloudWatch logs:**
- Log streaming: **Enabled**
- Retention: 
  - Dev: **3 days**
  - Production: **7 days**

### 3.11 Review and Create

1. Review all settings
2. Click **"Submit"**
3. Wait 10-15 minutes for environment creation

---

## Step 4: Set Environment Variables

**This is where you set your database credentials and other environment variables!**

### 4.1 Navigate to Configuration

1. Go to **Elastic Beanstalk Console**
2. Click on your environment (**kelox-dev** or **kelox-prod**)
3. In the left sidebar, click **"Configuration"**
4. Find **"Software"** section
5. Click **"Edit"** button

### 4.2 Environment Properties

Scroll down to **"Environment properties"** section.

Add the following properties:

#### For Development Environment:

```
Property Name                 Property Value
─────────────────────────────────────────────────────────────────
SPRING_PROFILES_ACTIVE        dev
SERVER_PORT                   5000
DB_URL                        jdbc:postgresql://[YOUR-DB-ENDPOINT]:5432/kelox_db
DB_USERNAME                   postgres
DB_PASSWORD                   [Your database password from Step 2]
ADMIN_SECRET_CODE             kelox-admin-dev-[random-string]
JWT_SECRET                    kelox-jwt-dev-secret-[random-string-min-32-chars]
JWT_EXPIRATION               2592000000
```

**Example with actual values:**
```
SPRING_PROFILES_ACTIVE        dev
SERVER_PORT                   5000
DB_URL                        jdbc:postgresql://kelox-dev-db.abc123.us-east-1.rds.amazonaws.com:5432/kelox_db
DB_USERNAME                   postgres
DB_PASSWORD                   MySecurePassword123!
ADMIN_SECRET_CODE             kelox-admin-dev-a8f9c2e1d4b7
JWT_SECRET                    kelox-jwt-dev-secret-f9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4
JWT_EXPIRATION               2592000000
```

#### For Production Environment:

```
Property Name                 Property Value
─────────────────────────────────────────────────────────────────
SPRING_PROFILES_ACTIVE        prod
SERVER_PORT                   5000
DB_URL                        jdbc:postgresql://[YOUR-PROD-DB-ENDPOINT]:5432/kelox_db
DB_USERNAME                   postgres
DB_PASSWORD                   [Your strong production password]
ADMIN_SECRET_CODE             [Strong secret - min 32 characters]
JWT_SECRET                    [Very strong secret - min 64 characters]
JWT_EXPIRATION               2592000000
```

### 4.3 Generate Strong Secrets

Use these commands to generate secure secrets:

```bash
# Generate admin secret (32 characters)
openssl rand -hex 16

# Generate JWT secret (64 characters)
openssl rand -hex 32

# Or use this pattern:
# Admin: kelox-admin-prod-[32-char-random-string]
# JWT: kelox-jwt-prod-[64-char-random-string]
```

### 4.4 Apply Changes

1. After adding all environment properties
2. Click **"Apply"** at the bottom
3. Wait 2-5 minutes for environment to update

---

## Step 5: Upload and Deploy

### If You Selected "Sample Application" Earlier:

### 5.1 Navigate to Your Environment

1. Go to **Elastic Beanstalk Console**
2. Click on your environment (**kelox-dev** or **kelox-prod**)

### 5.2 Upload Your Application

1. Click **"Upload and deploy"** button (top right)
2. Click **"Choose file"**
3. Select your JAR file: `kelox-backend.jar`
4. Version label: `v1.0` or `kelox-backend-v1.0-$(date +%Y%m%d)`
5. Click **"Deploy"**

### 5.3 Wait for Deployment

1. Deployment takes 5-10 minutes
2. Monitor the events in the console
3. Status will change from **"Updating"** to **"Ok"** when done

---

## Step 6: Test Your Deployment

### 6.1 Get Your Application URL

1. In the Elastic Beanstalk console
2. You'll see your environment URL at the top:
   ```
   http://kelox-dev.us-east-1.elasticbeanstalk.com
   ```
3. Your API base URL is:
   ```
   http://kelox-dev.us-east-1.elasticbeanstalk.com/api
   ```

### 6.2 Test Health Endpoint

```bash
# Replace with your actual URL
curl http://kelox-dev.us-east-1.elasticbeanstalk.com/api/actuator/health

# Expected response:
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    ...
  }
}
```

### 6.3 Test Your API Endpoints

```bash
# Get your URL
API_URL="http://kelox-dev.us-east-1.elasticbeanstalk.com/api"

# Test health
curl $API_URL/health

# Test authentication endpoint
curl -X POST $API_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"privyDid":"test","signature":"test"}'
```

### 6.4 Check Application Logs

1. In Elastic Beanstalk console
2. Click **"Logs"** in the left sidebar
3. Click **"Request Logs"** > **"Last 100 Lines"** or **"Full Logs"**
4. Wait a moment, then click **"Download"**
5. Review logs for any errors

---

## Updating Your Application

### When You Make Code Changes:

### 1. Build New Version

```bash
# Make your code changes
# Then build
./gradlew clean bootJar -x test

# Copy to desktop with version number
cp build/libs/kelox-be-0.0.1-SNAPSHOT.jar ~/Desktop/kelox-backend-v1.1.jar
```

### 2. Upload New Version

1. Go to **Elastic Beanstalk Console**
2. Click on your environment
3. Click **"Upload and deploy"**
4. Choose your new JAR file
5. Version label: `v1.1` or `kelox-backend-v1.1-$(date +%Y%m%d)`
6. Click **"Deploy"**

### 3. Deployment Strategies

**For Dev:** Rolling deployment (default)
- Updates instances one at a time
- 5-10 minutes downtime per instance

**For Production:** Consider using:
- **Immutable**: Creates new instances, then swaps (zero downtime)
- **Blue/Green**: Creates entirely new environment, then swaps

Change deployment policy:
1. **Configuration** > **Rolling updates and deployments**
2. Select policy: **Immutable** or **Rolling**
3. Apply changes

---

## Updating Environment Variables

### After Initial Setup:

1. Go to **Elastic Beanstalk Console**
2. Select your environment
3. Click **"Configuration"** in sidebar
4. Find **"Software"** > Click **"Edit"**
5. Scroll to **"Environment properties"**
6. Modify or add variables
7. Click **"Apply"**
8. Wait 2-5 minutes for update

**Note:** Changing environment variables restarts your application!

---

## 📊 Environment Variables Reference

### Required Variables

| Variable | Dev Example | Prod Example | Description |
|----------|-------------|--------------|-------------|
| `SPRING_PROFILES_ACTIVE` | `dev` | `prod` | Spring profile |
| `SERVER_PORT` | `5000` | `5000` | Application port |
| `DB_URL` | `jdbc:postgresql://kelox-dev-db.xxx.rds.amazonaws.com:5432/kelox_db` | `jdbc:postgresql://kelox-prod-db.xxx.rds.amazonaws.com:5432/kelox_db` | Database URL |
| `DB_USERNAME` | `postgres` | `postgres` | Database user |
| `DB_PASSWORD` | `[dev-password]` | `[strong-password]` | Database password |
| `ADMIN_SECRET_CODE` | `kelox-admin-dev-xxx` | `[32+ char secret]` | Admin API secret |
| `JWT_SECRET` | `kelox-jwt-dev-xxx` | `[64+ char secret]` | JWT signing key |
| `JWT_EXPIRATION` | `2592000000` | `2592000000` | JWT expiry (30 days) |

### How to Format DB_URL

```
jdbc:postgresql://[RDS-ENDPOINT]:[PORT]/[DATABASE-NAME]

Example:
jdbc:postgresql://kelox-dev-db.abc123xyz.us-east-1.rds.amazonaws.com:5432/kelox_db
                  └─────────────── RDS Endpoint ──────────────┘          └─PORT─┘ └DB Name┘
```

**Get RDS Endpoint:**
1. Go to RDS Console
2. Click on your database
3. Copy from "Endpoint & port" section
4. Default port is **5432**
5. Database name is **kelox_db** (what you set during creation)

---

## 🔒 HTTPS Setup (Optional)

After initial deployment, you can add HTTPS:

### 1. Request SSL Certificate

1. Go to **AWS Certificate Manager** (ACM)
2. Click **"Request certificate"**
3. Enter domain: `dev-api.yourdomain.com`
4. Validation: **DNS validation**
5. Add CNAME record to your DNS
6. Wait for validation

### 2. Add Certificate to Load Balancer

1. Go to **Elastic Beanstalk Console**
2. Select your environment
3. **Configuration** > **Load balancer**
4. Click **"Edit"**
5. Under **Listeners**, click **"Add listener"**
   - Port: **443**
   - Protocol: **HTTPS**
   - SSL certificate: Select your ACM certificate
6. Click **"Apply"**

### 3. Update DNS

Point your domain to the load balancer:
- Create CNAME record
- Name: `dev-api` (or `api`)
- Value: Your EB environment URL

See **[SSL_HTTPS_SETUP_GUIDE.md](./SSL_HTTPS_SETUP_GUIDE.md)** for details.

---

## 🛠️ Troubleshooting

### Environment Variables Not Working

**Problem:** Application can't read environment variables

**Solution:**
1. Check variable names are exactly correct (case-sensitive)
2. No extra spaces in values
3. Wait for environment update to complete
4. Check logs: **Logs** > **Request Logs** > **Full Logs**

### Database Connection Failed

**Problem:** Can't connect to RDS

**Solutions:**

1. **Check Security Group:**
   - RDS security group must allow inbound from EB security group
   - Go to **RDS** > **Your DB** > **Connectivity & security** > **Security groups**
   - Edit inbound rules: Allow PostgreSQL (5432) from EB security group

2. **Check DB_URL format:**
   ```
   jdbc:postgresql://[endpoint]:5432/kelox_db
   ```

3. **Check database name:**
   - Must be `kelox_db` (what you created)
   - Check in RDS console: **Configuration** tab

4. **Verify credentials:**
   - Username: `postgres`
   - Password: Correct password from creation

### Application Won't Start

**Check logs:**
1. Go to **Logs** > **Request Logs** > **Full Logs**
2. Look for errors in `/var/log/web.stdout.log`

**Common issues:**
- Wrong Java version (should be Java 17)
- Missing environment variables
- Database connection failed
- Flyway migration errors

### Health Check Failing

**Problem:** Environment shows "Severe" or "Degraded"

**Solution:**
1. Check health check path is correct: `/api/actuator/health`
2. **Configuration** > **Load balancer** > Edit
3. Under **Processes** > **default**
4. Health check path: `/api/actuator/health`
5. Apply changes

---

## 💰 Cost Tracking

Monitor your AWS costs:

1. Go to **AWS Cost Explorer**
2. Filter by:
   - Service: RDS, EC2, Elastic Load Balancing
   - Tag: Environment=dev or production

**Expected monthly costs:**
- Dev: ~$35-40
- Production: ~$55-75 (base, before traffic)

---

## 📝 Checklist

### Before Deploying

- [ ] JAR file built successfully
- [ ] RDS database created and available
- [ ] Database endpoint saved
- [ ] Database password saved securely
- [ ] Elastic Beanstalk environment created
- [ ] Environment variables configured
- [ ] Security group allows RDS connection

### After Deploying

- [ ] Environment status is "Ok" (green)
- [ ] Health check passes
- [ ] Can access application URL
- [ ] API endpoints work
- [ ] Database connection works
- [ ] Logs show no errors

---

## 📚 Related Documentation

- [SSL_HTTPS_SETUP_GUIDE.md](./SSL_HTTPS_SETUP_GUIDE.md) - Add HTTPS
- [DEV_DEPLOYMENT_GUIDE.md](./DEV_DEPLOYMENT_GUIDE.md) - CLI deployment
- [AWS EB Console Guide](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/using-features.environments.html)

---

## 🎯 Quick Summary

**Build:**
```bash
./gradlew clean bootJar -x test
```

**Environment Variables (in AWS Console > EB > Configuration > Software):**
```
SPRING_PROFILES_ACTIVE  = dev (or prod)
SERVER_PORT            = 5000
DB_URL                 = jdbc:postgresql://[endpoint]:5432/kelox_db
DB_USERNAME            = postgres
DB_PASSWORD            = [your-password]
ADMIN_SECRET_CODE      = [secret]
JWT_SECRET             = [secret]
```

**Upload:**
- EB Console > Upload and deploy > Choose JAR file > Deploy

**Test:**
```bash
curl http://[your-env].elasticbeanstalk.com/api/actuator/health
```

---

**🎉 You're ready to deploy manually via AWS Console!**

Any issues? Check the troubleshooting section or the logs in AWS Console.

