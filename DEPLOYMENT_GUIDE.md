# BizRok Platform - Production Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the BizRok platform to production. The deployment uses Docker Compose for container orchestration and includes monitoring, logging, and security configurations.

## 🚀 Quick Start

### Prerequisites

- **Docker** 20.10+
- **Docker Compose** 2.0+
- **Maven** 3.6+ (for building)
- **8GB RAM** minimum (16GB recommended)
- **50GB Disk Space** minimum

### One-Command Deployment

```bash
# Clone the repository
git clone <repository-url>
cd bizrok-platform

# Run the deployment script
chmod +x deploy.sh
./deploy.sh
```

## 📋 Manual Deployment Steps

### 1. Environment Setup

#### Create Environment Variables

```bash
# Copy the template
cp .env.template .env

# Edit the .env file with your configuration
nano .env
```

**Required Environment Variables:**

```bash
# Database Configuration
DB_PASSWORD=your_secure_database_password

# Redis Configuration  
REDIS_PASSWORD=your_redis_password

# JWT Configuration
JWT_SECRET=your_super_secret_jwt_key

# Email Configuration
EMAIL_USERNAME=your_email@gmail.com
EMAIL_PASSWORD=your_app_specific_password

# Grafana Configuration
GRAFANA_PASSWORD=admin_password
```

### 2. Build Application

#### Backend Build

```bash
cd backend

# Build the application
mvn clean package -DskipTests

# Verify build
ls -la target/*.jar
```

#### Frontend Build (Optional)

```bash
cd frontend

# Install dependencies
npm install

# Build for production
npm run build

# Serve static files (if using Nginx)
cp -r dist/* ../nginx/static/
```

### 3. Setup Resources

#### OCR Configuration

```bash
# Download Tesseract language data
mkdir -p resources/tessdata
cd resources/tessdata

# Download English language data
curl -L -o eng.traineddata https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata

# Download additional languages if needed
curl -L -o hin.traineddata https://github.com/tesseract-ocr/tessdata/raw/main/hin.traineddata
```

#### Face Detection Configuration

```bash
# Download OpenCV cascade files
mkdir -p resources/haarcascades
cd resources/haarcascades

# Download face detection cascades
curl -L -o haarcascade_frontalface_alt.xml https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml
curl -L -o haarcascade_eye.xml https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/haarcascade_eye.xml
```

### 4. Database Setup

#### Initialize Database

```bash
# Start only the database
docker-compose up -d postgres

# Wait for database to be ready
sleep 30

# Verify database connection
docker-compose exec postgres pg_isready -U bizrok_user -d bizrok_prod
```

#### Run Database Migrations

The application will automatically create tables and run migrations on startup using Spring Boot JPA.

### 5. Start Services

```bash
# Start all services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f
```

### 6. Health Checks

```bash
# Check backend health
curl http://localhost:8080/api/health

# Check all services
docker-compose exec postgres pg_isready -U bizrok_user -d bizrok_prod
docker-compose exec redis redis-cli ping
```

## 🔧 Configuration Details

### Application Configuration

#### Environment-Specific Configs

- **Development**: `application.yml` (default)
- **Production**: `application-prod.yml`

#### Key Configuration Areas

1. **Database**: PostgreSQL with connection pooling
2. **Redis**: Caching and rate limiting
3. **Security**: JWT authentication with refresh tokens
4. **Monitoring**: Prometheus metrics and Grafana dashboards
5. **Logging**: ELK stack integration

### Security Configuration

#### SSL/TLS Setup

```bash
# Generate SSL certificates
mkdir -p nginx/ssl
cd nginx/ssl

# Self-signed certificate (for testing)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout bizrok.key -out bizrok.crt

# Update nginx configuration for SSL
# Edit nginx/conf.d/bizrok.conf to include SSL configuration
```

#### Security Headers

The Nginx configuration includes:
- Content Security Policy
- X-Frame-Options
- X-XSS-Protection
- Strict-Transport-Security
- X-Content-Type-Options

### Monitoring Setup

#### Grafana Dashboards

Access Grafana at `http://localhost:3000`:
- Username: `admin`
- Password: Set in `.env` file

#### Prometheus Metrics

Access Prometheus at `http://localhost:9090`:
- Application metrics at `/actuator/prometheus`
- Custom metrics for business logic

#### ELK Stack

- **Elasticsearch**: `http://localhost:9200`
- **Logstash**: Port 5044 (for log shipping)
- **Kibana**: `http://localhost:5601`

### Rate Limiting

#### Configuration

```yaml
rate-limiting:
  enabled: true
  window-size: 60000  # 1 minute
  requests-per-window: 60
  login-attempts-per-window: 5
  login-window-size: 900000  # 15 minutes
  block-duration: 3600000  # 1 hour
```

#### Monitoring Rate Limits

```bash
# Check Redis for rate limit keys
docker-compose exec redis redis-cli keys "rate_limit:*"

# View rate limit headers in API responses
curl -I http://localhost:8080/api/health
```

## 🚨 Production Considerations

### 1. Security Hardening

#### Change Default Passwords

```bash
# Update all default passwords in .env
DB_PASSWORD=your_new_secure_password
REDIS_PASSWORD=your_new_redis_password
JWT_SECRET=your_new_jwt_secret
GRAFANA_PASSWORD=your_new_grafana_password
```

#### Enable SSL/TLS

```bash
# Obtain SSL certificates from Let's Encrypt or your provider
# Update nginx configuration
# Redirect HTTP to HTTPS
```

#### Firewall Configuration

```bash
# Allow only necessary ports
ufw allow 22    # SSH
ufw allow 80    # HTTP
ufw allow 443   # HTTPS
ufw deny 5432   # PostgreSQL (internal only)
ufw deny 6379   # Redis (internal only)
ufw deny 9090   # Prometheus (internal only)
ufw deny 3000   # Grafana (internal only)
ufw deny 5601   # Kibana (internal only)
```

### 2. Performance Tuning

#### JVM Tuning

```bash
# Set JVM options in docker-compose.yml
JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:+UseContainerSupport"
```

#### Database Optimization

```sql
-- Create additional indexes for performance
CREATE INDEX CONCURRENTLY idx_orders_created_at ON orders(created_at);
CREATE INDEX CONCURRENTLY idx_order_answers_created_at ON order_answers(created_at);
CREATE INDEX CONCURRENTLY idx_users_created_at ON users(created_at);
```

#### Redis Optimization

```bash
# Configure Redis for production
# Edit redis configuration
maxmemory 2gb
maxmemory-policy allkeys-lru
```

### 3. Backup and Recovery

#### Database Backup

```bash
# Create backup
docker-compose exec postgres pg_dump -U bizrok_user bizrok_prod > backup_$(date +%Y%m%d).sql

# Restore backup
docker-compose exec -T postgres psql -U bizrok_user -d bizrok_prod < backup_file.sql
```

#### Application Data Backup

```bash
# Backup uploaded files
tar -czf uploads_backup_$(date +%Y%m%d).tar.gz uploads/

# Backup configuration
tar -czf config_backup_$(date +%Y%m%d).tar.gz nginx/ monitoring/ logging/
```

### 4. Scaling Considerations

#### Horizontal Scaling

```yaml
# Scale backend services
docker-compose up -d --scale backend=3

# Load balancing with Nginx
# Update nginx configuration for upstream servers
```

#### Database Scaling

- **Read Replicas**: Configure PostgreSQL streaming replication
- **Connection Pooling**: Use PgBouncer for connection pooling
- **Caching**: Increase Redis memory and configure clustering

## 🔍 Monitoring and Maintenance

### Health Checks

#### Automated Health Checks

```bash
# Create health check script
cat > health_check.sh << 'EOF'
#!/bin/bash
services=("backend" "postgres" "redis" "nginx")
for service in "${services[@]}"; do
    if ! docker-compose ps $service | grep -q "Up"; then
        echo "Service $service is down"
        exit 1
    fi
done
echo "All services are healthy"
EOF

chmod +x health_check.sh
```

#### Log Monitoring

```bash
# Monitor application logs
docker-compose logs -f backend

# Monitor specific log levels
docker-compose logs -f backend 2>&1 | grep ERROR

# Monitor access logs
tail -f logs/nginx/access.log
```

### Performance Monitoring

#### Key Metrics to Monitor

1. **Application Metrics**:
   - Response time (p95, p99)
   - Error rate
   - Request rate
   - JVM memory usage

2. **Infrastructure Metrics**:
   - CPU usage
   - Memory usage
   - Disk I/O
   - Network I/O

3. **Business Metrics**:
   - Order completion rate
   - KYC verification success rate
   - User registration rate

#### Alerting

Configure alerts in Grafana for:
- High error rates (> 5%)
- High response times (> 2 seconds)
- Service downtime
- Disk space usage (> 80%)
- Memory usage (> 80%)

### Maintenance Tasks

#### Regular Tasks

```bash
# Daily: Check logs and metrics
# Weekly: Update dependencies and security patches
# Monthly: Review and optimize database queries
# Quarterly: Update SSL certificates
```

#### Database Maintenance

```bash
# Vacuum and analyze tables
docker-compose exec postgres vacuumdb -U bizrok_user -d bizrok_prod --analyze

# Check for long-running queries
docker-compose exec postgres psql -U bizrok_user -d bizrok_prod -c "SELECT * FROM pg_stat_activity WHERE state = 'active';"
```

## 🚨 Troubleshooting

### Common Issues

#### 1. Service Won't Start

```bash
# Check logs for specific service
docker-compose logs <service-name>

# Check Docker resources
docker system df

# Check port conflicts
netstat -tulpn | grep :8080
```

#### 2. Database Connection Issues

```bash
# Check database status
docker-compose exec postgres pg_isready -U bizrok_user -d bizrok_prod

# Check connection from application
docker-compose exec backend curl -f http://postgres:5432
```

#### 3. Redis Connection Issues

```bash
# Check Redis status
docker-compose exec redis redis-cli ping

# Check Redis memory usage
docker-compose exec redis redis-cli info memory
```

#### 4. SSL/TLS Issues

```bash
# Check SSL certificate
openssl x509 -in nginx/ssl/bizrok.crt -text -noout

# Test SSL connection
curl -k https://localhost
```

### Debug Commands

```bash
# Check all container status
docker-compose ps

# View real-time logs
docker-compose logs -f

# Execute commands in container
docker-compose exec <service> <command>

# Restart specific service
docker-compose restart <service>

# Scale services
docker-compose up -d --scale <service>=<count>
```

## 📊 Performance Benchmarks

### Expected Performance

- **API Response Time**: < 200ms (p95)
- **Concurrent Users**: 1000+ 
- **Order Processing**: 100+ orders/minute
- **KYC Verification**: < 30 seconds per verification

### Load Testing

```bash
# Install k6 for load testing
curl -s https://github.com/grafana/k6/releases/download/v0.45.0/k6-v0.45.0-linux-amd64.tar.gz | tar xz

# Run load test
./k6-v0.45.0-linux-amd64/k6 run load-test.js
```

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
name: Deploy to Production

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Docker Buildx
        uses: docker/setup-buildx-action@v2
        
      - name: Build and Deploy
        run: |
          chmod +x deploy.sh
          ./deploy.sh
```

### Docker Registry Integration

```bash
# Build and push images
docker build -t your-registry/bizrok-backend:v1.0.0 backend/
docker push your-registry/bizrok-backend:v1.0.0

# Update docker-compose.yml with registry images
```

## 📞 Support

### Getting Help

1. **Check Logs**: `docker-compose logs -f`
2. **Health Checks**: `curl http://localhost:8080/api/health`
3. **Documentation**: This guide and code comments
4. **Monitoring**: Grafana and Kibana dashboards

### Emergency Procedures

#### Service Recovery

```bash
# Stop all services
docker-compose down

# Clean up
docker system prune -f

# Restart
docker-compose up -d
```

#### Data Recovery

```bash
# Restore from backup
docker-compose exec -T postgres psql -U bizrok_user -d bizrok_prod < backup_file.sql

# Verify data integrity
docker-compose exec postgres psql -U bizrok_user -d bizrok_prod -c "SELECT COUNT(*) FROM users;"
```

## 📝 Checklist

### Pre-Deployment

- [ ] Environment variables configured
- [ ] SSL certificates obtained
- [ ] Database backup created
- [ ] Monitoring configured
- [ ] Security headers verified
- [ ] Rate limiting tested

### Post-Deployment

- [ ] All services healthy
- [ ] Health checks passing
- [ ] Logs accessible
- [ ] Monitoring dashboards working
- [ ] SSL/TLS working
- [ ] Performance baseline established

### Ongoing

- [ ] Regular backups
- [ ] Security updates
- [ ] Performance monitoring
- [ ] Log analysis
- [ ] Capacity planning

---

**Note**: This guide assumes a Linux-based production environment. Adjust commands as needed for your specific environment.