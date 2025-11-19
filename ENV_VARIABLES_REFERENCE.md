# 🔐 Environment Variables Reference Card

Quick reference for setting environment variables in AWS Elastic Beanstalk Console.

---

## 📍 Where to Set Variables

**AWS Console Path:**
```
Elastic Beanstalk > Your Environment > Configuration > Software > Edit > Environment properties
```

**Or via CLI:**
```bash
eb setenv KEY=value
```

---

## 📋 Required Variables

### Development Environment

Copy and paste these into AWS Console, replacing the values in `[brackets]`:

```
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=5000
DB_URL=jdbc:postgresql://[YOUR-RDS-ENDPOINT]:5432/kelox_db
DB_USERNAME=postgres
DB_PASSWORD=[your-dev-password]
ADMIN_SECRET_CODE=kelox-admin-dev-[random-16-chars]
JWT_SECRET=kelox-jwt-dev-[random-32-chars]
JWT_EXPIRATION=2592000000
```

**Example with values filled in:**
```
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=5000
DB_URL=jdbc:postgresql://kelox-dev-db.abc123.us-east-1.rds.amazonaws.com:5432/kelox_db
DB_USERNAME=postgres
DB_PASSWORD=MyDevPassword123
ADMIN_SECRET_CODE=kelox-admin-dev-a8f9c2e1d4b7f8e9
JWT_SECRET=kelox-jwt-dev-f9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4
JWT_EXPIRATION=2592000000
```

---

### Production Environment

Copy and paste these into AWS Console, using **strong secrets**:

```
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=5000
DB_URL=jdbc:postgresql://[YOUR-PROD-RDS-ENDPOINT]:5432/kelox_db
DB_USERNAME=postgres
DB_PASSWORD=[strong-production-password]
ADMIN_SECRET_CODE=[32-character-random-string]
JWT_SECRET=[64-character-random-string]
JWT_EXPIRATION=2592000000
```

---

## 🔑 Getting Your Values

### 1. RDS Endpoint

**Get from AWS Console:**
1. Go to **RDS Console**
2. Click on your database (**kelox-dev-db** or **kelox-prod-db**)
3. Find **"Endpoint & port"** section
4. Copy the endpoint (looks like: `kelox-dev-db.xxxxx.us-east-1.rds.amazonaws.com`)

**Complete DB_URL format:**
```
jdbc:postgresql://[ENDPOINT]:5432/kelox_db
                   ↑           ↑      ↑
                   |           |      └─ Database name (always kelox_db)
                   |           └─ Port (always 5432 for PostgreSQL)
                   └─ Your RDS endpoint
```

**Example:**
```
jdbc:postgresql://kelox-dev-db.abc123xyz456.us-east-1.rds.amazonaws.com:5432/kelox_db
```

---

### 2. Generate Secrets

**Using Terminal:**

```bash
# For development (shorter secrets OK)
ADMIN_DEV="kelox-admin-dev-$(openssl rand -hex 8)"
JWT_DEV="kelox-jwt-dev-$(openssl rand -hex 16)"

echo "ADMIN_SECRET_CODE=$ADMIN_DEV"
echo "JWT_SECRET=$JWT_DEV"

# For production (longer, more secure)
ADMIN_PROD="kelox-admin-prod-$(openssl rand -hex 16)"
JWT_PROD="kelox-jwt-prod-$(openssl rand -hex 32)"

echo "ADMIN_SECRET_CODE=$ADMIN_PROD"
echo "JWT_SECRET=$JWT_PROD"
```

**Using Online Generator:**
- Visit: https://randomkeygen.com/
- Use "Fort Knox Passwords" section
- Copy two different passwords

---

## 📝 Variable Descriptions

| Variable | Description | Example Value | Required |
|----------|-------------|---------------|----------|
| `SPRING_PROFILES_ACTIVE` | Spring Boot profile (dev or prod) | `dev` or `prod` | ✅ Yes |
| `SERVER_PORT` | Application server port | `5000` | ✅ Yes |
| `DB_URL` | JDBC connection URL to PostgreSQL | `jdbc:postgresql://...` | ✅ Yes |
| `DB_USERNAME` | Database username | `postgres` | ✅ Yes |
| `DB_PASSWORD` | Database password | `[your-password]` | ✅ Yes |
| `ADMIN_SECRET_CODE` | Secret for admin API endpoints | `[random-string]` | ✅ Yes |
| `JWT_SECRET` | Secret key for JWT signing | `[random-string]` | ✅ Yes |
| `JWT_EXPIRATION` | JWT token lifetime (milliseconds) | `2592000000` (30 days) | ⚠️ Optional |

---

## 🛡️ Security Best Practices

### Password Strength

**Dev Environment:**
- Minimum 12 characters
- Include numbers, letters, special chars
- Example: `MyDev2024Pass!`

**Production Environment:**
- Minimum 16 characters
- Mix of upper/lower case, numbers, special chars
- Use a password manager
- Example: `Pr0d!uct10n$tr0ng#2024`

### Secret Generation

**Admin Secret:**
- Dev: Minimum 16 characters
- Prod: Minimum 32 characters
- Use random generation

**JWT Secret:**
- Dev: Minimum 32 characters
- Prod: Minimum 64 characters
- Never reuse between environments
- Never commit to git

---

## 📊 Setting Variables in AWS Console

### Step-by-Step:

1. **Navigate to Environment**
   - AWS Console > Elastic Beanstalk
   - Click your environment name

2. **Open Configuration**
   - Left sidebar > **Configuration**
   - Find **Software** section
   - Click **Edit**

3. **Add Environment Properties**
   - Scroll to **Environment properties** section
   - For each variable:
     - **Name**: Variable name (e.g., `DB_URL`)
     - **Value**: Variable value
   - Click **Add environment property** for each one

4. **Apply Changes**
   - Click **Apply** at bottom
   - Wait 2-5 minutes for update

### 🎯 Quick Copy-Paste Format

For easy pasting, prepare your variables in this format:

**Name** = **Value**

```
SPRING_PROFILES_ACTIVE = dev
SERVER_PORT = 5000
DB_URL = jdbc:postgresql://kelox-dev-db.abc123.us-east-1.rds.amazonaws.com:5432/kelox_db
DB_USERNAME = postgres
DB_PASSWORD = MySecurePassword123
ADMIN_SECRET_CODE = kelox-admin-dev-a8f9c2e1d4b7f8e9
JWT_SECRET = kelox-jwt-dev-f9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4
JWT_EXPIRATION = 2592000000
```

---

## 🔄 Updating Variables

**Important:** Changing environment variables restarts your application!

### Via Console:
1. Configuration > Software > Edit
2. Modify values
3. Click Apply
4. Wait for environment to update (~2-5 minutes)

### Via CLI:
```bash
# Update single variable
eb setenv DB_PASSWORD=new_password

# Update multiple variables
eb setenv ADMIN_SECRET_CODE=new_secret JWT_SECRET=new_jwt_secret
```

---

## ✅ Verification Checklist

After setting variables, verify:

- [ ] All 8 required variables are set
- [ ] No typos in variable names (case-sensitive!)
- [ ] DB_URL format is correct
- [ ] DB_URL endpoint matches your RDS instance
- [ ] Database name is `kelox_db`
- [ ] Port is `5432`
- [ ] Secrets are strong and random
- [ ] Different secrets for dev and prod
- [ ] Environment profile matches environment (dev/prod)
- [ ] Applied changes successfully
- [ ] Application restarted without errors

---

## 🐛 Troubleshooting

### Application Won't Start

**Check logs:**
1. EB Console > Logs > Request Logs > Full Logs
2. Look for errors mentioning environment variables

**Common issues:**
- Variable name typo (case-sensitive!)
- Extra spaces in values
- DB_URL format incorrect
- Database endpoint wrong
- Database doesn't exist

### Database Connection Failed

**Verify:**
```
DB_URL     = jdbc:postgresql://[correct-endpoint]:5432/kelox_db
DB_USERNAME = postgres
DB_PASSWORD = [correct-password]
```

**Check:**
- RDS instance is available (not stopped)
- Database name is `kelox_db` (check RDS console)
- Security group allows connection from EB

### JWT Errors

**Check:**
- `JWT_SECRET` is at least 32 characters
- No special characters causing issues (stick to alphanumeric)
- Same secret across all instances (don't mix dev/prod)

---

## 📱 Quick Reference Card (Print This!)

```
╔══════════════════════════════════════════════════════════╗
║           KELOX BACKEND - ENV VARIABLES                  ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  AWS Console Location:                                   ║
║  EB > Configuration > Software > Environment properties  ║
║                                                          ║
║  Required Variables (8):                                 ║
║  ┌────────────────────────────────────────────────────┐ ║
║  │ SPRING_PROFILES_ACTIVE  = dev or prod              │ ║
║  │ SERVER_PORT            = 5000                      │ ║
║  │ DB_URL                 = jdbc:postgresql://...     │ ║
║  │ DB_USERNAME            = postgres                  │ ║
║  │ DB_PASSWORD            = [secure-password]         │ ║
║  │ ADMIN_SECRET_CODE      = [16-32 char secret]       │ ║
║  │ JWT_SECRET             = [32-64 char secret]       │ ║
║  │ JWT_EXPIRATION         = 2592000000                │ ║
║  └────────────────────────────────────────────────────┘ ║
║                                                          ║
║  DB_URL Format:                                          ║
║  jdbc:postgresql://[endpoint]:5432/kelox_db              ║
║                                                          ║
║  Get RDS Endpoint:                                       ║
║  RDS Console > Your DB > Endpoint & port                 ║
║                                                          ║
║  Generate Secrets:                                       ║
║  openssl rand -hex 16  (32 chars)                        ║
║  openssl rand -hex 32  (64 chars)                        ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

---

## 📚 Related Documentation

- [MANUAL_CONSOLE_DEPLOYMENT.md](./MANUAL_CONSOLE_DEPLOYMENT.md) - Complete manual deployment guide
- [SSL_HTTPS_SETUP_GUIDE.md](./SSL_HTTPS_SETUP_GUIDE.md) - HTTPS configuration
- [DEV_DEPLOYMENT_GUIDE.md](./DEV_DEPLOYMENT_GUIDE.md) - Development environment guide

---

**💡 Pro Tip:** Save your environment variables in a secure password manager (1Password, LastPass, etc.) for easy reference!

