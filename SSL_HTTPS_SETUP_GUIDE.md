# 🔒 HTTPS/SSL Setup Guide for Kelox Backend

Both dev and prod environments now use Application Load Balancers to support HTTPS.

## 📋 Overview

### Instance Types (Confirmed)
- **Dev:** t3.micro (1 vCPU, 1GB RAM)
- **Prod:** t3.small (2 vCPU, 2GB RAM)

### Load Balancer Setup
- **Dev:** Application Load Balancer (ALB) with t3.micro instance
- **Prod:** Application Load Balancer (ALB) with t3.small instance

### Initial Access
- Both environments start with **HTTP only** (port 80)
- After SSL certificate setup: **HTTPS** (port 443)

---

## 🚀 Quick Setup (3 Options)

### Option 1: Use Default AWS Domain (HTTP) - Immediate
✅ Works out of the box  
✅ No SSL certificate needed  
❌ HTTP only (not secure)

```bash
# After deployment
eb status kelox-dev
# URL: http://kelox-dev-xxx.elasticbeanstalk.com/api
```

### Option 2: AWS Certificate Manager (ACM) - Free SSL
✅ Free SSL certificate  
✅ Auto-renewal  
✅ Easy setup  
⚠️ Requires custom domain

**Setup time:** 15-20 minutes

### Option 3: Custom SSL Certificate
✅ Use existing certificate  
✅ Works with any provider  
⚠️ Manual renewal  
⚠️ More complex

---

## 🎯 Recommended: AWS Certificate Manager (ACM)

### Prerequisites

1. **Custom domain** (e.g., `api.yourdomain.com`)
2. **Route 53** or access to DNS settings
3. **Deployed environment** (dev or prod)

### Step-by-Step Setup

#### 1. Request SSL Certificate in ACM

```bash
# Set your region
AWS_REGION="us-east-1"

# Request certificate for dev
aws acm request-certificate \
    --domain-name dev-api.yourdomain.com \
    --validation-method DNS \
    --region $AWS_REGION \
    --tags Key=Environment,Value=dev Key=Project,Value=kelox

# Request certificate for prod
aws acm request-certificate \
    --domain-name api.yourdomain.com \
    --validation-method DNS \
    --region $AWS_REGION \
    --tags Key=Environment,Value=production Key=Project,Value=kelox

# Get certificate ARN
aws acm list-certificates --region $AWS_REGION
```

Save the Certificate ARN, you'll need it later.

#### 2. Validate Domain Ownership

**If using Route 53:**

```bash
# Get validation CNAME records
CERT_ARN="arn:aws:acm:us-east-1:123456789:certificate/xxx-xxx-xxx"

aws acm describe-certificate \
    --certificate-arn $CERT_ARN \
    --region $AWS_REGION \
    --query 'Certificate.DomainValidationOptions[0].ResourceRecord'

# ACM can automatically add these records if you're using Route 53
# Or add them manually in Route 53 console
```

**If using external DNS provider:**

1. Go to AWS Console > Certificate Manager
2. View certificate details
3. Copy CNAME name and value
4. Add CNAME record in your DNS provider
5. Wait for validation (5-30 minutes)

#### 3. Configure HTTPS in Elastic Beanstalk

##### For Dev Environment

Edit `.ebextensions-dev/01-environment.config` and uncomment the HTTPS section:

```yaml
# HTTP to HTTPS Redirect
aws:elbv2:listener:default:
  ListenerEnabled: 'true'  # Keep HTTP enabled or set to 'false' to disable

aws:elbv2:listener:443:
  Protocol: HTTPS
  SSLCertificateArns: 'arn:aws:acm:us-east-1:YOUR_ACCOUNT:certificate/YOUR_CERT_ID'
  DefaultProcess: default
  Rules: ''

# Optional: Redirect HTTP to HTTPS
aws:elbv2:listenerrule:redirect:
  PathPatterns: /*
  Priority: 1
  Actions: redirect
  RedirectConfig:
    Protocol: HTTPS
    Port: '443'
    StatusCode: HTTP_301
```

##### For Prod Environment

Edit `.ebextensions/01-environment.config` and uncomment the HTTPS section:

```yaml
# HTTPS Configuration
aws:elbv2:listener:default:
  ListenerEnabled: 'false'  # Disable HTTP, force HTTPS only

aws:elbv2:listener:443:
  Protocol: HTTPS
  SSLCertificateArns: 'arn:aws:acm:us-east-1:YOUR_ACCOUNT:certificate/YOUR_CERT_ID'
  DefaultProcess: default
  Rules: ''
```

#### 4. Deploy Updated Configuration

```bash
# For dev
./deploy-to-dev.sh

# For prod
./deploy-to-prod.sh
```

#### 5. Configure DNS (Route 53 or External)

**Using Route 53:**

```bash
# Get load balancer DNS name
LB_DNS=$(eb status kelox-dev | grep CNAME | awk '{print $3}')

# Create Route 53 alias record
aws route53 change-resource-record-sets \
    --hosted-zone-id YOUR_HOSTED_ZONE_ID \
    --change-batch '{
      "Changes": [{
        "Action": "CREATE",
        "ResourceRecordSet": {
          "Name": "dev-api.yourdomain.com",
          "Type": "CNAME",
          "TTL": 300,
          "ResourceRecords": [{"Value": "'$LB_DNS'"}]
        }
      }]
    }'
```

**Using External DNS:**

1. Get load balancer URL: `eb status kelox-dev`
2. Create CNAME record:
   - Name: `dev-api` (or `api` for prod)
   - Type: CNAME
   - Value: `kelox-dev-xxx.elasticbeanstalk.com`
   - TTL: 300

#### 6. Test HTTPS

```bash
# Test HTTPS endpoint
curl https://dev-api.yourdomain.com/api/actuator/health

# Should return: {"status":"UP",...}

# Test HTTP redirect (if configured)
curl -I http://dev-api.yourdomain.com/api/actuator/health
# Should return: 301 Moved Permanently
```

---

## 🔧 Alternative: Quick HTTPS Setup with EB Console

If you prefer using the AWS Console:

### 1. Deploy Environment First

```bash
./setup-dev-environment.sh
# or
./setup-prod-environment.sh
```

### 2. Request Certificate in ACM Console

1. Go to **AWS Certificate Manager**
2. Click **Request certificate**
3. Enter domain name (e.g., `dev-api.yourdomain.com`)
4. Choose **DNS validation**
5. Add CNAME record to your DNS
6. Wait for validation
7. Copy certificate ARN

### 3. Add Certificate to Load Balancer

1. Go to **Elastic Beanstalk Console**
2. Select your environment (`kelox-dev` or `kelox-prod`)
3. Click **Configuration**
4. Under **Load balancer**, click **Edit**
5. Click **Add listener**
   - Port: 443
   - Protocol: HTTPS
   - SSL certificate: Select your ACM certificate
6. Click **Apply**

### 4. Optional: Redirect HTTP to HTTPS

In the same load balancer configuration:
1. Select the **Port 80** listener
2. Edit **Rules**
3. Add redirect rule:
   - Path: `/*`
   - Redirect to: HTTPS:443
   - Response code: 301

---

## 💰 Cost Impact

### With Load Balancer (Required for HTTPS)

| Environment | Before | With ALB | Monthly Cost |
|-------------|--------|----------|--------------|
| **Dev** | N/A | New setup | ~$23-26/month |
| **Prod** | N/A | New setup | ~$60-75/month |

**Cost Breakdown:**

**Dev Environment:**
- t3.micro instance: $7.50
- db.t3.micro RDS: $12
- Application Load Balancer: $16
- SSL Certificate (ACM): **Free**
- **Total:** ~$35.50/month

**Prod Environment:**
- t3.small instance: $15
- db.t3.small Multi-AZ: $25
- Application Load Balancer: $16
- SSL Certificate (ACM): **Free**
- **Total:** ~$56/month (base, before traffic costs)

---

## 🛠️ Troubleshooting

### Certificate Validation Stuck

```bash
# Check certificate status
aws acm describe-certificate \
    --certificate-arn YOUR_CERT_ARN \
    --region us-east-1

# Common issues:
# 1. CNAME record not added to DNS
# 2. Wrong CNAME value
# 3. DNS propagation delay (wait 5-30 minutes)
```

### HTTPS Not Working After Setup

```bash
# 1. Check certificate is attached to listener
aws elbv2 describe-listeners \
    --load-balancer-arn $(eb status kelox-dev --verbose | grep "LoadBalancerARN" | awk '{print $2}')

# 2. Check security group allows port 443
# 3. Check DNS points to correct load balancer
# 4. Clear browser cache / try incognito mode
```

### Mixed Content Warnings

If your frontend is on HTTP but backend on HTTPS:
- Deploy frontend with HTTPS too
- Or configure CORS to allow HTTP frontend to call HTTPS backend

### Certificate Expired

ACM certificates auto-renew if:
- DNS validation records are still in place
- Domain is still accessible
- No action needed!

---

## 🔐 Security Best Practices

### 1. Force HTTPS Only (Production)

In `.ebextensions/01-environment.config`:

```yaml
aws:elbv2:listener:default:
  ListenerEnabled: 'false'  # Disable HTTP completely
```

### 2. Use Strong SSL Policies

```yaml
aws:elbv2:listener:443:
  Protocol: HTTPS
  SSLCertificateArns: 'arn:aws:acm:...'
  SSLPolicy: 'ELBSecurityPolicy-TLS-1-2-2017-01'  # TLS 1.2+
```

### 3. Enable HSTS (HTTP Strict Transport Security)

Create `.ebextensions-dev/03-security-headers.config`:

```yaml
files:
  "/etc/nginx/conf.d/https-security.conf":
    mode: "000644"
    owner: root
    group: root
    content: |
      add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
      add_header X-Frame-Options "SAMEORIGIN" always;
      add_header X-Content-Type-Options "nosniff" always;
      add_header X-XSS-Protection "1; mode=block" always;

container_commands:
  01_reload_nginx:
    command: "sudo service nginx reload"
```

### 4. Regular Security Audits

```bash
# Check SSL configuration
curl -I https://api.yourdomain.com

# Test SSL with SSL Labs
# Visit: https://www.ssllabs.com/ssltest/
```

---

## 📊 Deployment Workflow with HTTPS

### Initial Setup (One-Time)

```bash
# 1. Deploy environment
./setup-dev-environment.sh

# 2. Request SSL certificate
aws acm request-certificate --domain-name dev-api.yourdomain.com ...

# 3. Validate domain (add DNS records)

# 4. Update .ebextensions-dev/01-environment.config with cert ARN

# 5. Redeploy
./deploy-to-dev.sh

# 6. Configure DNS to point to load balancer

# 7. Test HTTPS
curl https://dev-api.yourdomain.com/api/actuator/health
```

### Future Deployments

```bash
# Just deploy normally - SSL configuration persists
./deploy-to-dev.sh
./deploy-to-prod.sh
```

---

## 🌐 Domain Setup Examples

### Example 1: Subdomains for Dev/Prod

```
dev-api.yourdomain.com  → kelox-dev environment (HTTPS)
api.yourdomain.com      → kelox-prod environment (HTTPS)
```

### Example 2: Different Domains

```
dev.kelox.io  → kelox-dev environment (HTTPS)
api.kelox.io  → kelox-prod environment (HTTPS)
```

### Example 3: Path-Based (Single Domain)

Not recommended for dev/prod separation, but possible with CloudFront.

---

## 🎯 Quick Reference Commands

```bash
# Request certificate
aws acm request-certificate \
    --domain-name api.yourdomain.com \
    --validation-method DNS

# List certificates
aws acm list-certificates

# Get certificate details
aws acm describe-certificate --certificate-arn ARN

# Get load balancer DNS
eb status kelox-dev | grep CNAME

# Test HTTPS
curl -I https://api.yourdomain.com/api/actuator/health

# Check certificate expiry
echo | openssl s_client -servername api.yourdomain.com \
    -connect api.yourdomain.com:443 2>/dev/null | \
    openssl x509 -noout -dates
```

---

## ✅ Checklist

### Before Requesting Certificate
- [ ] Have a custom domain
- [ ] Have access to DNS settings
- [ ] Environment is deployed
- [ ] Know your AWS region

### Certificate Request
- [ ] Request certificate in ACM
- [ ] Add DNS validation records
- [ ] Wait for validation (5-30 min)
- [ ] Save certificate ARN

### Configuration
- [ ] Update .ebextensions config with cert ARN
- [ ] Uncomment HTTPS listener section
- [ ] Configure HTTP redirect (optional)
- [ ] Deploy updated configuration

### DNS Setup
- [ ] Point domain to load balancer
- [ ] Wait for DNS propagation (5-60 min)
- [ ] Test HTTPS access
- [ ] Verify certificate in browser

### Testing
- [ ] HTTPS endpoint works
- [ ] HTTP redirects to HTTPS (if configured)
- [ ] API endpoints function correctly
- [ ] No SSL warnings in browser
- [ ] Health check passes

---

## 📞 Need Help?

Common issues and solutions:
1. **"Certificate validation pending"** - Check DNS records, wait 30 minutes
2. **"502 Bad Gateway"** - Check application health: `eb health kelox-dev`
3. **"SSL certificate error"** - Verify certificate ARN is correct
4. **"DNS not resolving"** - Check CNAME record, wait for propagation

For more help, see:
- [AWS ACM Documentation](https://docs.aws.amazon.com/acm/)
- [Elastic Beanstalk HTTPS Guide](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/configuring-https.html)

---

**🎉 You're all set!** Both dev and prod environments can now use HTTPS with free SSL certificates from AWS Certificate Manager.

