# 🚀 AWS Deployment Guide - Spring Boot Task Management App

**Complete step-by-step guide to deploy your Spring Boot application on AWS Free Tier**

**Estimated Time:** 4-6 hours  
**Cost:** $0 for first 12 months (Free Tier)  
**Prerequisites:** AWS account, basic terminal knowledge

---

## 📋 Table of Contents

1. [Prerequisites & Account Setup](#1-prerequisites--account-setup)
2. [Phase 1: Security & Billing Setup](#phase-1-security--billing-setup-30-minutes)
3. [Phase 2: RDS PostgreSQL Database](#phase-2-rds-postgresql-database-1-hour)
4. [Phase 3: EC2 Instance Setup](#phase-3-ec2-instance-setup-15-hours)
5. [Phase 4: Deploy Spring Boot Application](#phase-4-deploy-spring-boot-application-30-minutes)
6. [Phase 5: Nginx Reverse Proxy](#phase-5-nginx-reverse-proxy-30-minutes)
7. [Phase 6: Systemd Service Configuration](#phase-6-systemd-service-configuration-20-minutes)
8. [Phase 7: SSL Certificate with Let's Encrypt](#phase-7-ssl-certificate-30-minutes)
9. [Phase 8: Frontend Configuration](#phase-8-frontend-configuration-10-minutes)
10. [Phase 9: Monitoring & Cost Management](#phase-9-monitoring--cost-management-20-minutes)
11. [Troubleshooting](#troubleshooting)
12. [Cost Optimization](#cost-optimization)

---

## 1. Prerequisites & Account Setup

### ✅ Before You Start

- [ ] AWS account created at https://aws.amazon.com
- [ ] Credit/debit card added (won't be charged on free tier)
- [ ] Email verified
- [ ] Phone number verified
- [ ] Root user login credentials saved securely

### 📊 Free Tier Eligibility Check

**EC2:** 750 hours/month of t2.micro or t3.micro (12 months)  
**RDS:** 750 hours/month of db.t2.micro or db.t3.micro (12 months)  
**Storage:** 30 GB EBS, 20 GB RDS  
**Data Transfer:** 100 GB/month outbound

---

## Phase 1: Security & Billing Setup (30 minutes)

### Step 1.1: Enable MFA on Root Account

**Why:** Protect your AWS account from unauthorized access

```bash
1. Sign in to AWS Console as root user
2. Click on account name (top right) → Security credentials
3. Under "Multi-factor authentication (MFA)" → Activate MFA
4. Choose "Virtual MFA device"
5. Use Google Authenticator or Authy to scan QR code
6. Enter two consecutive MFA codes
7. Click "Assign MFA"
```

### Step 1.2: Create IAM Admin User

**Why:** Never use root account for daily tasks

```bash
1. Go to IAM Console: https://console.aws.amazon.com/iam/
2. Users → Add users
3. User name: admin-user
4. Select: "Provide user access to the AWS Management Console"
5. Choose: "I want to create an IAM user"
6. Console password: Custom password (save it!)
7. Uncheck: "Users must create a new password at next sign-in"
8. Next → Permissions

9. Attach policies directly:
   - AdministratorAccess (for now, restrict later)
   
10. Next → Create user
11. Save the console sign-in URL
12. Sign out and sign in with IAM user (use this from now on!)
```

### Step 1.3: Create Billing Alerts

**Why:** Avoid surprise charges

```bash
1. Go to Billing Console: https://console.aws.amazon.com/billing/
2. Billing Preferences → Edit
3. Enable: "Receive Billing Alerts"
4. Save preferences

5. Go to CloudWatch: https://console.aws.amazon.com/cloudwatch/
6. Change region to: US East (N. Virginia) - us-east-1
   (Billing metrics only available in us-east-1!)
7. Alarms → Billing → Create alarm

8. Create 3 alarms:
   
   Alarm 1: $1 threshold
   - Metric: EstimatedCharges
   - Statistic: Maximum
   - Period: 6 hours
   - Threshold: >= 1 USD
   - Email: your-email@example.com
   
   Alarm 2: $5 threshold
   Alarm 3: $10 threshold
```

### Step 1.4: Create Key Pair for SSH

**Why:** Securely connect to EC2 instances

```bash
1. Go to EC2 Console: https://console.aws.amazon.com/ec2/
2. Change region to your preferred region (e.g., ap-south-1 Mumbai)
3. Network & Security → Key Pairs → Create key pair
4. Name: taskmanagement-key
5. Key pair type: RSA
6. Private key file format: .pem (for Mac/Linux) or .ppk (for Windows/PuTTY)
7. Create key pair
8. Save the .pem file securely (you can't download it again!)

9. Set proper permissions (Mac/Linux):
   chmod 400 ~/Downloads/taskmanagement-key.pem
   
   Move to safe location:
   mkdir -p ~/.ssh/aws-keys
   mv ~/Downloads/taskmanagement-key.pem ~/.ssh/aws-keys/
```

---

## Phase 2: RDS PostgreSQL Database (1 hour)

### Step 2.1: Create RDS Security Group

```bash
1. EC2 Console → Security Groups → Create security group
2. Security group name: rds-postgres-sg
3. Description: Security group for RDS PostgreSQL
4. VPC: (default VPC)

5. Inbound rules:
   - Click "Add rule"
   - Type: PostgreSQL
   - Port: 5432
   - Source: Custom (we'll update this after creating EC2)
   - Description: Allow from EC2
   
6. Outbound rules: (leave default - all traffic)
7. Create security group
8. Note the Security Group ID (sg-xxxxxxxxx)
```

### Step 2.2: Create RDS PostgreSQL Instance

```bash
1. RDS Console: https://console.aws.amazon.com/rds/
2. Create database

3. Choose a database creation method: Standard create
4. Engine options:
   - Engine type: PostgreSQL
   - Version: PostgreSQL 15.x (latest stable)
   
5. Templates: Free tier (automatically selects db.t3.micro)

6. Settings:
   - DB instance identifier: taskmanagement-db
   - Master username: postgres
   - Master password: [Create strong password - save it!]
   - Confirm password: [same password]
   
7. Instance configuration:
   - DB instance class: db.t3.micro (free tier)
   - Storage type: General Purpose SSD (gp3)
   - Allocated storage: 20 GB (free tier max)
   - Uncheck: Enable storage autoscaling
   
8. Connectivity:
   - VPC: Default VPC
   - Subnet group: default
   - Public access: No
   - VPC security group: Choose existing → rds-postgres-sg
   - Availability Zone: No preference
   
9. Database authentication: Password authentication

10. Additional configuration:
    - Initial database name: taskmanagement
    - Backup retention period: 7 days (free)
    - Enable encryption: Yes (free)
    - Enable Enhanced monitoring: No (costs extra)
    - Enable auto minor version upgrade: Yes
    
11. Create database

12. Wait 10-15 minutes for database to be created
13. Once available, note the Endpoint (e.g., taskmanagement-db.xxxxxxxxx.ap-south-1.rds.amazonaws.com)
```

### Step 2.3: Export Data from Render

**On your local machine:**

```bash
# Install PostgreSQL client if not already installed
# Mac:
brew install postgresql@15

# Ubuntu/Debian:
sudo apt-get install postgresql-client

# Get Render database credentials from Render dashboard
# Go to your database → Connection → Internal Database URL

# Export data
pg_dump -h dpg-xxxxx-a \
  -U your_render_username \
  -d your_render_database \
  --no-owner \
  --no-acl \
  -f render_backup.sql

# Enter password when prompted

# Verify backup
ls -lh render_backup.sql
```

### Step 2.4: Import Data to AWS RDS

**We'll do this later after setting up EC2 for security**

---

## Phase 3: EC2 Instance Setup (1.5 hours)

### Step 3.1: Create EC2 Security Group

```bash
1. EC2 Console → Security Groups → Create security group
2. Security group name: ec2-web-sg
3. Description: Security group for web server
4. VPC: (default VPC)

5. Inbound rules:
   - Rule 1:
     Type: SSH
     Port: 22
     Source: My IP (your current IP - automatically detected)
     Description: SSH from my IP
   
   - Rule 2:
     Type: HTTP
     Port: 80
     Source: Anywhere-IPv4 (0.0.0.0/0)
     Description: HTTP from anywhere
   
   - Rule 3:
     Type: HTTPS
     Port: 443
     Source: Anywhere-IPv4 (0.0.0.0/0)
     Description: HTTPS from anywhere
   
   - Rule 4:
     Type: Custom TCP
     Port: 8080
     Source: Anywhere-IPv4 (0.0.0.0/0)
     Description: Spring Boot (temporary - remove after nginx setup)
   
6. Outbound rules: (leave default - all traffic)
7. Create security group
8. Note the Security Group ID (sg-yyyyyyyyy)
```

### Step 3.2: Update RDS Security Group

```bash
1. Security Groups → Find rds-postgres-sg
2. Edit inbound rules
3. Click on the PostgreSQL rule
4. Change Source to: ec2-web-sg (search and select)
5. Save rules

Now EC2 can connect to RDS!
```

### Step 3.3: Launch EC2 Instance

```bash
1. EC2 Console → Instances → Launch instances

2. Name: taskmanagement-backend

3. Application and OS Images:
   - Quick Start: Amazon Linux
   - Amazon Machine Image (AMI): Amazon Linux 2023 AMI
   - Architecture: 64-bit (x86)
   
4. Instance type: t3.micro (Free tier eligible)

5. Key pair:
   - Select: taskmanagement-key (created earlier)
   
6. Network settings:
   - VPC: default
   - Subnet: No preference
   - Auto-assign public IP: Enable
   - Firewall (security groups): Select existing → ec2-web-sg
   
7. Configure storage:
   - Root volume: 30 GB gp3 (Free tier - max 30 GB)
   - Delete on termination: Yes
   
8. Advanced details:
   - Leave all as default
   
9. Summary:
   - Number of instances: 1
   
10. Launch instance

11. Wait for instance state: Running
12. Select your instance and note:
    - Instance ID
    - Public IPv4 address
    - Public IPv4 DNS
```

### Step 3.4: Allocate Elastic IP (Optional but Recommended)

**Why:** Public IP changes if you stop/start instance

```bash
1. EC2 Console → Elastic IPs → Allocate Elastic IP address
2. Network Border Group: (leave default)
3. Allocate

4. Select the new Elastic IP
5. Actions → Associate Elastic IP address
6. Instance: Select taskmanagement-backend
7. Private IP: (auto-selected)
8. Associate

9. Note the new Elastic IP address
10. Use this IP for all connections from now on

⚠️ Important: Elastic IP is FREE while associated with a running instance
            But costs $3.60/month if not associated!
```

### Step 3.5: Connect to EC2 Instance

```bash
# Replace YOUR_ELASTIC_IP with your actual IP
ssh -i ~/.ssh/aws-keys/taskmanagement-key.pem ec2-user@YOUR_ELASTIC_IP

# If you get "Permission denied (publickey)" error:
chmod 400 ~/.ssh/aws-keys/taskmanagement-key.pem

# First time connection will ask to verify fingerprint, type: yes
```

### Step 3.6: Initial Server Setup

**Run these commands on EC2:**

```bash
# Update system
sudo yum update -y

# Install Java 17 (Amazon Corretto)
sudo yum install java-17-amazon-corretto-devel -y

# Verify Java installation
java -version
# Should show: openjdk version "17.x.x"

# Install Git
sudo yum install git -y

# Install PostgreSQL client (for database import)
sudo yum install postgresql15 -y

# Install nginx
sudo yum install nginx -y

# Install certbot for SSL
sudo yum install certbot python3-certbot-nginx -y

# Install htop for monitoring (optional)
sudo yum install htop -y

# Verify installations
git --version
psql --version
nginx -v
certbot --version
```

---

## Phase 4: Deploy Spring Boot Application (30 minutes)

### Step 4.1: Clone Repository

```bash
# On EC2 instance
cd ~
git clone https://github.com/Beesettyrakesh/task-management-system.git
cd task-management-system/backend
```

### Step 4.2: Create Production Configuration

```bash
# Create .env file
nano .env

# Add these configurations (replace with your actual values):
```

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://taskmanagement-db.xxxxxxxxx.ap-south-1.rds.amazonaws.com:5432/taskmanagement
DB_USERNAME=postgres
DB_PASSWORD=your_rds_password

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-min-256-bits-change-this-in-production

# Email Configuration
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-gmail-app-password

# AWS S3 Configuration
AWS_ACCESS_KEY_ID=your-aws-access-key
AWS_SECRET_ACCESS_KEY=your-aws-secret-key
AWS_S3_BUCKET_NAME=your-s3-bucket-name
AWS_REGION=ap-south-2

# Application Configuration
SPRING_PROFILES_ACTIVE=prod
FRONTEND_URL=https://task-management-system-gules.vercel.app

# Server Configuration (optional)
SERVER_PORT=8080
```

```bash
# Save and exit (Ctrl+X, then Y, then Enter)

# Set proper permissions
chmod 600 .env
```

### Step 4.3: Import Database from Render

```bash
# Copy backup file to EC2 (run this on your LOCAL machine):
scp -i ~/.ssh/aws-keys/taskmanagement-key.pem \
  render_backup.sql \
  ec2-user@YOUR_ELASTIC_IP:~/

# Back on EC2, import to RDS:
psql -h taskmanagement-db.xxxxxxxxx.ap-south-1.rds.amazonaws.com \
  -U postgres \
  -d taskmanagement \
  -f ~/render_backup.sql

# Enter RDS password when prompted

# Verify import
psql -h taskmanagement-db.xxxxxxxxx.ap-south-1.rds.amazonaws.com \
  -U postgres \
  -d taskmanagement \
  -c "\dt"

# Should show your tables: users, tasks, tags, etc.
```

### Step 4.4: Build Application

```bash
cd ~/task-management-system/backend

# Build the application (skip tests for faster build)
./mvnw clean package -DskipTests

# This will take 5-10 minutes
# Output JAR will be in: target/taskmanagement-0.0.1-SNAPSHOT.jar

# Verify build
ls -lh target/*.jar
```

### Step 4.5: Test Application

```bash
# Load environment variables
export $(cat .env | xargs)

# Run application in foreground (for testing)
java -jar target/taskmanagement-0.0.1-SNAPSHOT.jar

# Wait for "Started TaskmanagementApplication" message
# Keep terminal open and test from another terminal/browser

# Test from your local machine:
curl http://YOUR_ELASTIC_IP:8080/api/auth/login

# Should get JSON response (even if error, means it's running)

# Stop the application: Ctrl+C
```

---

## Phase 5: Nginx Reverse Proxy (30 minutes)

### Step 5.1: Configure Nginx

```bash
# Backup default config
sudo cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.backup

# Create new site configuration
sudo nano /etc/nginx/conf.d/taskmanagement.conf
```

```nginx
server {
    listen 80;
    server_name YOUR_ELASTIC_IP;  # Replace with your IP or domain

    # Increase buffer sizes for large requests
    client_max_body_size 50M;
    client_body_buffer_size 1M;

    # Logging
    access_log /var/log/nginx/taskmanagement-access.log;
    error_log /var/log/nginx/taskmanagement-error.log;

    # API endpoints
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_http_version 1.1;
        
        # Headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts (for slow startup)
        proxy_connect_timeout 90s;
        proxy_send_timeout 90s;
        proxy_read_timeout 90s;
        
        # WebSocket support (if needed later)
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Swagger UI
    location /swagger-ui {
        proxy_pass http://localhost:8080/swagger-ui;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # API docs
    location /v3/api-docs {
        proxy_pass http://localhost:8080/v3/api-docs;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Health check endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
```

```bash
# Save and exit (Ctrl+X, Y, Enter)

# Test nginx configuration
sudo nginx -t

# Should show: "syntax is ok" and "test is successful"

# Start nginx
sudo systemctl start nginx
sudo systemctl enable nginx  # Auto-start on boot

# Check status
sudo systemctl status nginx
```

### Step 5.2: Test Nginx Proxy

```bash
# From your local machine:
curl http://YOUR_ELASTIC_IP/api/auth/login
curl http://YOUR_ELASTIC_IP/health

# Should work without specifying :8080 port
```

---

## Phase 6: Systemd Service Configuration (20 minutes)

### Step 6.1: Create Systemd Service File

```bash
sudo nano /etc/systemd/system/taskmanagement.service
```

```ini
[Unit]
Description=Task Management Spring Boot Application
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/task-management-system/backend
EnvironmentFile=/home/ec2-user/task-management-system/backend/.env
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar target/taskmanagement-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
StandardOutput=journal
StandardError=journal
SyslogIdentifier=taskmanagement
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# Save and exit
```

### Step 6.2: Enable and Start Service

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service (auto-start on boot)
sudo systemctl enable taskmanagement

# Start service
sudo systemctl start taskmanagement

# Check status
sudo systemctl status taskmanagement

# Should show: "active (running)"

# View logs
sudo journalctl -u taskmanagement -f

# Press Ctrl+C to stop following logs
```

### Step 6.3: Service Management Commands

```bash
# Start service
sudo systemctl start taskmanagement

# Stop service
sudo systemctl stop taskmanagement

# Restart service
sudo systemctl restart taskmanagement

# View status
sudo systemctl status taskmanagement

# View logs (last 100 lines)
sudo journalctl -u taskmanagement -n 100

# Follow logs in real-time
sudo journalctl -u taskmanagement -f

# View logs from today
sudo journalctl -u taskmanagement --since today
```

---

## Phase 7: SSL Certificate (30 minutes)

### Step 7.1: Prerequisites

**You need a domain name for SSL!**

Options:
1. **Buy a domain:** GoDaddy, Namecheap, etc. (~$10/year)
2. **Free domain:** Freenom (not recommended for production)
3. **Skip SSL for now:** Use HTTP with IP address (not secure!)

### Step 7.2: Point Domain to EC2

```bash
1. Go to your domain registrar
2. Manage DNS settings
3. Add an A record:
   - Type: A
   - Name: @ (for root domain) or api (for subdomain)
   - Value: YOUR_ELASTIC_IP
   - TTL: 300 (5 minutes)
   
4. Wait 5-30 minutes for DNS propagation
5. Test: ping yourdomain.com (should show your Elastic IP)
```

### Step 7.3: Update Nginx Configuration

```bash
sudo nano /etc/nginx/conf.d/taskmanagement.conf

# Change this line:
server_name YOUR_ELASTIC_IP;

# To:
server_name yourdomain.com;  # or api.yourdomain.com

# Save and exit

# Test configuration
sudo nginx -t

# Reload nginx
sudo systemctl reload nginx
```

### Step 7.4: Get SSL Certificate

```bash
# Get certificate (replace with your domain)
sudo certbot --nginx -d yourdomain.com

# Follow prompts:
# - Enter email for urgent notices
# - Agree to terms of service: Y
# - Share email with EFF: N (optional)
# - Redirect HTTP to HTTPS: 2 (recommended)

# Certbot automatically configures nginx!

# Test auto-renewal
sudo certbot renew --dry-run

# Should show: "Congratulations, all simulated renewals succeeded"
```

### Step 7.5: Verify SSL

```bash
# Test from browser or curl:
https://yourdomain.com/health
https://yourdomain.com/api/auth/login

# Should show 🔒 secure connection!
```

---

## Phase 8: Frontend Configuration (10 minutes)

### Step 8.1: Update Vercel Environment Variable

```bash
1. Go to Vercel Dashboard: https://vercel.com
2. Select your project: task-management-system
3. Settings → Environment Variables
4. Find: VITE_API_URL
5. Edit value to: https://yourdomain.com/api
   (or http://YOUR_ELASTIC_IP/api if no domain)
6. Save

7. Deployments → Latest deployment → "..." → Redeploy
8. Wait 2-3 minutes for redeployment
```

### Step 8.2: Update Backend CORS

```bash
# On EC2:
nano ~/task-management-system/backend/.env

# Update FRONTEND_URL:
FRONTEND_URL=https://task-management-system-gules.vercel.app

# Save and exit

# Restart backend:
sudo systemctl restart taskmanagement

# Verify:
sudo systemctl status taskmanagement
```

### Step 8.3: Test Complete Application

```bash
1. Go to: https://task-management-system-gules.vercel.app
2. Try to sign up
3. Should work with no network errors!
4. Try all features:
   - Signup
   - Login
   - Create tasks
   - Upload files
   - Tags
   - Dashboard
```

---

## Phase 9: Monitoring & Cost Management (20 minutes)

### Step 9.1: Enable CloudWatch Metrics

```bash
1. EC2 Console → Select your instance
2. Monitoring tab
3. Enable detailed monitoring (costs $2.10/month - optional)
4. Or use default 5-minute monitoring (free)
```

### Step 9.2: Create CloudWatch Alarms

```bash
1. CloudWatch Console: https://console.aws.amazon.com/cloudwatch/
2. Alarms → Create alarm

Alarm 1: High CPU
- Metric: EC2 → Per-Instance Metrics → CPUUtilization
- Instance: taskmanagement-backend
- Statistic: Average
- Period: 5 minutes
- Threshold: >= 80%
- Actions: Send notification to email

Alarm 2: Low Disk Space
- Metric: EC2 → Per-Instance Metrics → DiskSpaceUtilization
- Threshold: >= 85%

Alarm 3: RDS CPU
- Metric: RDS → Per-Database Metrics → CPUUtilization  
- Threshold: >= 80%
```

### Step 9.3: Set Up Daily Cost Monitoring

```bash
# SSH to EC2 and create monitoring script
nano ~/check-aws-costs.sh
```

```bash
#!/bin/bash
# Simple cost check script

echo "AWS Cost Check - $(date)"
echo "================================"
echo ""
echo "To view detailed costs:"
echo "1. Go to: https://console.aws.amazon.com/billing/"
echo "2. Click: Bills"
echo "3. Check current month charges"
echo ""
echo "Free Tier Usage:"
echo "https://console.aws.amazon.com/billing/home#/freetier"
```

```bash
# Make executable
chmod +x ~/check-aws-costs.sh

# Run it
./check-aws-costs.sh
```

### Step 9.4: Monitor Application Logs

```bash
# Install log rotation for application logs
sudo nano /etc/logrotate.d/taskmanagement
```

```
/var/log/nginx/taskmanagement-*.log {
    daily
    missingok
    rotate 14
    compress
    delaycompress
    notifempty
    create 0640 nginx nginx
    sharedscripts
    postrotate
        [ ! -f /var/run/nginx.pid ] || kill -USR1 `cat /var/run/nginx.pid`
    endscript
}
```

---

## Troubleshooting

### Issue 1: Can't Connect to EC2

```bash
# Check security group allows SSH from your IP
# Check you're using correct key file
# Check key file permissions: chmod 400 key.pem
# Check you're using correct username: ec2-user (not ubuntu or root)

# Try verbose SSH:
ssh -v -i ~/.ssh/aws-keys/taskmanagement-key.pem ec2-user@YOUR_IP
```

### Issue 2: Application Won't Start

```bash
# Check Java is installed:
java -version

# Check environment variables are loaded:
sudo systemctl status taskmanagement

# Check logs for errors:
sudo journalctl -u taskmanagement -n 100

# Common issues:
# - Database connection failed (check RDS endpoint in .env)
# - Port 8080 already in use (check: sudo lsof -i :8080)
# - Out of memory (t3.micro has only 1GB RAM)
```

### Issue 3: Database Connection Failed

```bash
# Check RDS security group allows connection from EC2
# Check RDS endpoint is correct in .env
# Check database password is correct
# Test connection:
psql -h your-rds-endpoint -U postgres -d taskmanagement

# If times out: Security group issue
# If password wrong: Check .env file
```

### Issue 4: Nginx 502 Bad Gateway

```bash
# Check Spring Boot is running:
sudo systemctl status taskmanagement

# Check Spring Boot is listening on 8080:
sudo netstat -tlnp | grep 8080

# Check nginx error logs:
sudo tail -f /var/log/nginx/taskmanagement-error.log

# Restart both services:
sudo systemctl restart taskmanagement
sudo systemctl restart nginx
```

### Issue 5: SSL Certificate Fails

```bash
# Check domain points to EC2 IP:
nslookup yourdomain.com

# Check port 80 is open in security group
# Check nginx is running:
sudo systemctl status nginx

# Try manual certificate:
sudo certbot certonly --nginx -d yourdomain.com

# Check certificate status:
sudo certbot certificates
```

### Issue 6: High Costs

```bash
# Check what's consuming costs:
# 1. Go to AWS Cost Explorer
# 2. Group by: Service
# 3. Check for:
#    - Data Transfer (>100GB/month)
#    - EBS Snapshots (delete old ones)
#    - Elastic IP (not attached)
#    - RDS storage (>20GB)

# Stop services when not needed:
sudo systemctl stop taskmanagement
# Then stop EC2 instance (⚠️ Elastic IP will charge $3.60/month!)
```

---

## Cost Optimization

### Reduce Data Transfer Costs

```bash
# 1. Keep frontend on Vercel (FREE CDN)
# 2. Serve large files from S3 directly (not through backend)
# 3. Enable gzip compression in nginx:

sudo nano /etc/nginx/nginx.conf

# Add in http block:
gzip on;
gzip_vary on;
gzip_proxied any;
gzip_comp_level 6;
gzip_types text/plain text/css text/xml text/javascript 
           application/json application/javascript application/xml+rss;

sudo systemctl reload nginx
```

### Use Reserved Instances (Year 2+)

```bash
# After free tier ends, save 40% with 1-year commitment:
# 1. EC2 Console → Reserved Instances → Purchase Reserved Instances
# 2. Instance type: t3.micro
# 3. Term: 1 year
# 4. Payment: All upfront (best savings)
# Cost: ~$110/year (vs $180/year on-demand)
```

### Automate Backups

```bash
# Create automated RDS snapshots:
# RDS Console → Your database → Maintenance & backups
# Automated backups: Enabled (7 days retention - free)

# Manual snapshot before major changes:
# RDS Console → Your database → Actions → Take snapshot