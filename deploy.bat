@echo off
REM BizRok Platform Deployment Script for Windows
REM This script deploys the complete BizRok platform infrastructure

echo =================================
echo    BizRok Platform Deployment  
echo =================================
echo.

REM Check if Docker is running
echo Checking Docker...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not running. Please start Docker Desktop.
    pause
    exit /b 1
)

echo Docker is running ✓

REM Create directories
echo Creating directory structure...
if not exist "logs" mkdir logs
if not exist "logs\nginx" mkdir logs\nginx
if not exist "resources\tessdata" mkdir resources\tessdata
if not exist "resources\haarcascades" mkdir resources\haarcascades
if not exist "database" mkdir database
if not exist "nginx\conf.d" mkdir nginx\conf.d
if not exist "nginx\ssl" mkdir nginx\ssl
if not exist "monitoring\prometheus" mkdir monitoring\prometheus
if not exist "monitoring\grafana\dashboards" mkdir monitoring\grafana\dashboards
if not exist "monitoring\grafana\datasources" mkdir monitoring\grafana\datasources
if not exist "logging" mkdir logging

echo Directory structure created ✓

REM Setup environment variables
echo Setting up environment variables...
if not exist ".env" (
    echo Creating .env file...
    (
        echo # Database Configuration
        echo DB_PASSWORD=your_secure_database_password_change_me
        echo.
        echo # Redis Configuration
        echo REDIS_PASSWORD=your_redis_password_change_me
        echo.
        echo # JWT Configuration
        echo JWT_SECRET=your_super_secret_jwt_key_change_this_in_production
        echo.
        echo # Email Configuration
        echo EMAIL_USERNAME=your_email@gmail.com
        echo EMAIL_PASSWORD=your_app_specific_password
        echo.
        echo # Grafana Configuration
        echo GRAFANA_PASSWORD=admin_password_change_me
        echo.
        echo # Application Configuration
        echo SPRING_PROFILES_ACTIVE=prod
    ) > .env
    echo WARNING: Please update the .env file with your actual configuration values
) else (
    echo Using existing .env file
)

REM Setup database
echo Setting up database...
if not exist "database\init.sql" (
    (
        echo -- Create extensions
        echo CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
        echo.
        echo -- Create indexes for performance
        echo CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
        echo CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
        echo CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
        echo CREATE INDEX IF NOT EXISTS idx_order_answers_order_id ON order_answers(order_id);
        echo CREATE INDEX IF NOT EXISTS idx_order_answers_question_id ON order_answers(question_id);
        echo.
        echo -- Set up proper permissions
        echo GRANT ALL PRIVILEGES ON DATABASE bizrok_prod TO bizrok_user;
        echo GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO bizrok_user;
        echo GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO bizrok_user;
    ) > database\init.sql
)
echo Database setup completed ✓

REM Setup monitoring
echo Setting up monitoring configuration...
if not exist "monitoring\prometheus.yml" (
    (
        echo global:
        echo   scrape_interval: 15s
        echo   evaluation_interval: 15s
        echo.
        echo scrape_configs:
        echo   - job_name: 'bizrok-backend'
        echo     static_configs:
        echo       - targets: ['backend:8080']
        echo     metrics_path: '/actuator/prometheus'
        echo     scrape_interval: 5s
        echo.
        echo   - job_name: 'nginx'
        echo     static_configs:
        echo       - targets: ['nginx:9113']
    ) > monitoring\prometheus.yml
)

if not exist "monitoring\grafana\datasources\datasource.yml" (
    (
        echo apiVersion: 1
        echo.
        echo datasources:
        echo   - name: Prometheus
        echo     type: prometheus
        echo     access: proxy
        echo     url: http://prometheus:9090
        echo     isDefault: true
    ) > monitoring\grafana\datasources\datasource.yml
)

if not exist "monitoring\grafana\dashboards\dashboard.yml" (
    (
        echo apiVersion: 1
        echo.
        echo providers:
        echo   - name: 'default'
        echo     orgId: 1
        echo     folder: ''
        echo     type: file
        echo     disableDeletion: false
        echo     updateIntervalSeconds: 10
        echo     allowUiUpdates: true
        echo     options:
        echo       path: /etc/grafana/provisioning/dashboards
    ) > monitoring\grafana\dashboards\dashboard.yml
)
echo Monitoring setup completed ✓

REM Setup logging
echo Setting up logging configuration...
if not exist "logging\logstash.conf" (
    (
        echo input {
        echo   beats {
        echo     port => 5044
        echo   }
        echo }
        echo.
        echo filter {
        echo   if [fields][app] == "bizrok-backend" {
        echo     grok {
        echo       match => { "message" => "%%{TIMESTAMP_ISO8601:timestamp} \[%%{DATA:thread}\] %%{LOGLEVEL:level} %%{DATA:logger} - %%{GREEDYDATA:log_message}" }
        echo     }
        echo.
        echo     date {
        echo       match => [ "timestamp", "ISO8601" ]
        echo     }
        echo   }
        echo }
        echo.
        echo output {
        echo   elasticsearch {
        echo     hosts => ["elasticsearch:9200"]
        echo     index => "bizrok-logs-%%{+YYYY.MM.dd}"
        echo   }
        echo.
        echo   stdout {
        echo     codec => rubydebug
        echo   }
        echo }
    ) > logging\logstash.conf
)
echo Logging setup completed ✓

REM Setup nginx
echo Setting up Nginx configuration...
if not exist "nginx\nginx.conf" (
    (
        echo user nginx;
        echo worker_processes auto;
        echo error_log /var/log/nginx/error.log warn;
        echo pid /var/run/nginx.pid;
        echo.
        echo events {
        echo     worker_connections 1024;
        echo }
        echo.
        echo http {
        echo     include /etc/nginx/mime.types;
        echo     default_type application/octet-stream;
        echo.
        echo     log_format main '$remote_addr - $remote_user [$time_local] "$request" '
        echo                     '$status $body_bytes_sent "$http_referer" '
        echo                     '"$http_user_agent" "$http_x_forwarded_for"';
        echo.
        echo     access_log /var/log/nginx/access.log main;
        echo.
        echo     sendfile on;
        echo     tcp_nopush on;
        echo     tcp_nodelay on;
        echo     keepalive_timeout 65;
        echo     types_hash_max_size 2048;
        echo     client_max_body_size 10M;
        echo.
        echo     gzip on;
        echo     gzip_vary on;
        echo     gzip_min_length 1024;
        echo     gzip_proxied any;
        echo     gzip_comp_level 6;
        echo     gzip_types
        echo         text/plain
        echo         text/css
        echo         text/xml
        echo         text/javascript
        echo         application/json
        echo         application/javascript
        echo         application/xml+rss
        echo         application/atom+xml
        echo         image/svg+xml;
        echo.
        echo     include /etc/nginx/conf.d/*.conf;
        echo }
    ) > nginx\nginx.conf
)

if not exist "nginx\conf.d\bizrok.conf" (
    (
        echo upstream backend {
        echo     server backend:8080;
        echo }
        echo.
        echo server {
        echo     listen 80;
        echo     server_name localhost;
        echo.
        echo     # Health check endpoint
        echo     location /health {
        echo         access_log off;
        echo         return 200 "healthy\n";
        echo         add_header Content-Type text/plain;
        echo     }
        echo.
        echo     # API proxy
        echo     location /api/ {
        echo         proxy_pass http://backend;
        echo         proxy_set_header Host $host;
        echo         proxy_set_header X-Real-IP $remote_addr;
        echo         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        echo         proxy_set_header X-Forwarded-Proto $scheme;
        echo.
        echo         # Security headers
        echo         add_header X-Content-Type-Options nosniff;
        echo         add_header X-Frame-Options DENY;
        echo         add_header X-XSS-Protection "1; mode=block";
        echo         add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";
        echo.
        echo         # Rate limiting
        echo         limit_req zone_api burst=20 nodelay;
        echo     }
        echo.
        echo     # Static files (if any)
        echo     location /static/ {
        echo         alias /app/static/;
        echo         expires 1y;
        echo         add_header Cache-Control "public, immutable";
        echo     }
        echo }
        echo.
        echo # Rate limiting zone
        echo limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    ) > nginx\conf.d\bizrok.conf
)
echo Nginx setup completed ✓

REM Stop existing containers
echo Stopping existing containers...
docker-compose down

REM Clean up unused images
echo Cleaning up unused images...
docker image prune -f

REM Build and start services
echo Building and starting services...
docker-compose up -d

REM Wait for services to be ready
echo Waiting for services to be ready...
timeout /t 30 /nobreak

REM Check service health
echo Checking service health...

REM Check backend health
echo Checking backend health...
for /l %%i in (1,1,30) do (
    curl -f http://localhost:8080/api/health >nul 2>&1
    if !errorlevel! equ 0 (
        echo Backend is healthy ✓
        goto :check_db
    )
    timeout /t 2 /nobreak >nul
)
echo ERROR: Backend health check failed
pause
exit /b 1

:check_db
REM Check database connection
echo Checking database connection...
docker-compose exec -T postgres pg_isready -U bizrok_user -d bizrok_prod

REM Check Redis connection
echo Checking Redis connection...
docker-compose exec -T redis redis-cli ping

echo All services are healthy ✓

REM Display deployment summary
echo.
echo =================================
echo    Deployment Summary         
echo =================================
echo.
echo Services Status:
docker-compose ps
echo.
echo Access URLs:
echo   Backend API: http://localhost:8080/api
echo   Health Check: http://localhost/health
echo   Grafana: http://localhost:3000 (admin/admin_password_change_me)
echo   Kibana: http://localhost:5601
echo   Prometheus: http://localhost:9090
echo.
echo Important Notes:
echo   • Update the .env file with your actual configuration values
echo   • Configure SSL certificates in the nginx/ssl directory
echo   • Monitor logs in the logs/ directory
echo.
echo Deployment completed successfully! 🚀
echo.
pause