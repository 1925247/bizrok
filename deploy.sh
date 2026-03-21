#!/bin/bash

# BizRok Platform Deployment Script
# This script deploys the complete BizRok platform infrastructure

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="bizrok"
ENVIRONMENT=${ENVIRONMENT:-prod}
COMPOSE_FILE="docker-compose.yml"
BACKEND_DIR="./backend"
FRONTEND_DIR="./frontend"

# Logging
LOG_FILE="deployment.log"
exec > >(tee -a "$LOG_FILE")
exec 2>&1

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}    BizRok Platform Deployment  ${NC}"
echo -e "${BLUE}================================${NC}"
echo ""

# Function to print status
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check Docker
    if ! command_exists docker; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    # Check Docker Compose
    if ! command_exists docker-compose; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    # Check if Docker daemon is running
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker daemon is not running. Please start Docker."
        exit 1
    fi
    
    print_status "Prerequisites check completed ✓"
}

# Function to create directories
create_directories() {
    print_status "Creating directory structure..."
    
    # Create necessary directories
    mkdir -p logs
    mkdir -p logs/nginx
    mkdir -p resources/tessdata
    mkdir -p resources/haarcascades
    mkdir -p database
    mkdir -p nginx/conf.d
    mkdir -p nginx/ssl
    mkdir -p monitoring/prometheus
    mkdir -p monitoring/grafana/dashboards
    mkdir -p monitoring/grafana/datasources
    mkdir -p logging
    
    print_status "Directory structure created ✓"
}

# Function to setup environment variables
setup_environment() {
    print_status "Setting up environment variables..."
    
    if [ ! -f .env ]; then
        print_warning ".env file not found. Creating template..."
        cat > .env << EOF
# Database Configuration
DB_PASSWORD=your_secure_database_password_change_me

# Redis Configuration
REDIS_PASSWORD=your_redis_password_change_me

# JWT Configuration
JWT_SECRET=your_super_secret_jwt_key_change_this_in_production

# Email Configuration
EMAIL_USERNAME=your_email@gmail.com
EMAIL_PASSWORD=your_app_specific_password

# Grafana Configuration
GRAFANA_PASSWORD=admin_password_change_me

# Application Configuration
SPRING_PROFILES_ACTIVE=prod
EOF
        print_warning "Please update the .env file with your actual configuration values"
    else
        print_status "Using existing .env file"
    fi
}

# Function to build backend
build_backend() {
    print_status "Building backend application..."
    
    if [ ! -d "$BACKEND_DIR" ]; then
        print_error "Backend directory not found: $BACKEND_DIR"
        exit 1
    fi
    
    cd "$BACKEND_DIR"
    
    # Build the application
    if [ -f "pom.xml" ]; then
        print_status "Building with Maven..."
        mvn clean package -DskipTests
    else
        print_error "Maven pom.xml not found in backend directory"
        exit 1
    fi
    
    cd ..
    print_status "Backend build completed ✓"
}

# Function to setup OCR and Face Detection resources
setup_resources() {
    print_status "Setting up OCR and Face Detection resources..."
    
    # Download Tesseract language data (if not present)
    if [ ! -f "resources/tessdata/eng.traineddata" ]; then
        print_status "Downloading Tesseract language data..."
        mkdir -p temp_tesseract
        cd temp_tesseract
        
        # Download English language data
        curl -L -o "eng.traineddata" "https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata" || {
            print_warning "Failed to download Tesseract data. You may need to install it manually."
        }
        
        # Copy to resources
        cp eng.traineddata ../resources/tessdata/ 2>/dev/null || true
        cd ..
        rm -rf temp_tesseract
    fi
    
    # Download OpenCV cascade files (if not present)
    if [ ! -f "resources/haarcascades/haarcascade_frontalface_alt.xml" ]; then
        print_status "Downloading OpenCV cascade files..."
        mkdir -p temp_opencv
        cd temp_opencv
        
        # Download cascade files
        curl -L -o "haarcascade_frontalface_alt.xml" "https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml" || {
            print_warning "Failed to download face detection cascade. You may need to install it manually."
        }
        
        curl -L -o "haarcascade_eye.xml" "https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/haarcascade_eye.xml" || {
            print_warning "Failed to download eye detection cascade. You may need to install it manually."
        }
        
        # Copy to resources
        cp haarcascade_*.xml ../resources/haarcascades/ 2>/dev/null || true
        cd ..
        rm -rf temp_opencv
    fi
    
    print_status "OCR and Face Detection resources setup completed ✓"
}

# Function to setup database
setup_database() {
    print_status "Setting up database..."
    
    # Create database initialization script
    cat > database/init.sql << 'EOF'
-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create tables (if they don't exist)
-- Note: Spring Boot JPA will handle most table creation
-- This is for any additional setup needed

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_answers_order_id ON order_answers(order_id);
CREATE INDEX IF NOT EXISTS idx_order_answers_question_id ON order_answers(question_id);

-- Set up proper permissions
GRANT ALL PRIVILEGES ON DATABASE bizrok_prod TO bizrok_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO bizrok_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO bizrok_user;
EOF

    print_status "Database setup completed ✓"
}

# Function to setup monitoring
setup_monitoring() {
    print_status "Setting up monitoring configuration..."
    
    # Prometheus configuration
    cat > monitoring/prometheus.yml << 'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

scrape_configs:
  - job_name: 'bizrok-backend'
    static_configs:
      - targets: ['backend:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'nginx'
    static_configs:
      - targets: ['nginx:9113']

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          # - alertmanager:9093

# Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"
EOF

    # Grafana datasource configuration
    cat > monitoring/grafana/datasources/datasource.yml << 'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
EOF

    # Grafana dashboard configuration
    cat > monitoring/grafana/dashboards/dashboard.yml << 'EOF'
apiVersion: 1

providers:
  - name: 'default'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /etc/grafana/provisioning/dashboards
EOF

    print_status "Monitoring setup completed ✓"
}

# Function to setup logging
setup_logging() {
    print_status "Setting up logging configuration..."
    
    # Logstash configuration
    cat > logging/logstash.conf << 'EOF'
input {
  beats {
    port => 5044
  }
}

filter {
  if [fields][app] == "bizrok-backend" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:level} %{DATA:logger} - %{GREEDYDATA:log_message}" }
    }
    
    date {
      match => [ "timestamp", "ISO8601" ]
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "bizrok-logs-%{+YYYY.MM.dd}"
  }
  
  stdout {
    codec => rubydebug
  }
}
EOF

    print_status "Logging setup completed ✓"
}

# Function to setup nginx
setup_nginx() {
    print_status "Setting up Nginx configuration..."
    
    # Main nginx configuration
    cat > nginx/nginx.conf << 'EOF'
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    client_max_body_size 10M;

    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml+rss
        application/atom+xml
        image/svg+xml;

    include /etc/nginx/conf.d/*.conf;
}
EOF

    # Application configuration
    cat > nginx/conf.d/bizrok.conf << 'EOF'
upstream backend {
    server backend:8080;
}

server {
    listen 80;
    server_name localhost;

    # Health check endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }

    # API proxy
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Security headers
        add_header X-Content-Type-Options nosniff;
        add_header X-Frame-Options DENY;
        add_header X-XSS-Protection "1; mode=block";
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";
        
        # Rate limiting
        limit_req zone_api burst=20 nodelay;
    }

    # Static files (if any)
    location /static/ {
        alias /app/static/;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}

# Rate limiting zone
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
EOF

    print_status "Nginx setup completed ✓"
}

# Function to deploy application
deploy_application() {
    print_status "Deploying application..."
    
    # Stop existing containers
    print_status "Stopping existing containers..."
    docker-compose -f "$COMPOSE_FILE" down
    
    # Remove unused images
    print_status "Cleaning up unused images..."
    docker image prune -f
    
    # Build and start services
    print_status "Building and starting services..."
    docker-compose -f "$COMPOSE_FILE" up -d
    
    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    sleep 30
    
    # Check service health
    check_service_health
}

# Function to check service health
check_service_health() {
    print_status "Checking service health..."
    
    # Check backend health
    print_status "Checking backend health..."
    for i in {1..30}; do
        if curl -f http://localhost:8080/api/health >/dev/null 2>&1; then
            print_status "Backend is healthy ✓"
            break
        fi
        if [ $i -eq 30 ]; then
            print_error "Backend health check failed"
            exit 1
        fi
        sleep 2
    done
    
    # Check database connection
    print_status "Checking database connection..."
    docker-compose -f "$COMPOSE_FILE" exec -T postgres pg_isready -U bizrok_user -d bizrok_prod
    
    # Check Redis connection
    print_status "Checking Redis connection..."
    docker-compose -f "$COMPOSE_FILE" exec -T redis redis-cli ping
    
    print_status "All services are healthy ✓"
}

# Function to display deployment summary
display_summary() {
    echo ""
    echo -e "${GREEN}================================${NC}"
    echo -e "${GREEN}    Deployment Summary         ${NC}"
    echo -e "${GREEN}================================${NC}"
    echo ""
    echo -e "${BLUE}Services Status:${NC}"
    docker-compose -f "$COMPOSE_FILE" ps
    echo ""
    echo -e "${BLUE}Access URLs:${NC}"
    echo -e "  Backend API: ${GREEN}http://localhost:8080/api${NC}"
    echo -e "  Health Check: ${GREEN}http://localhost/health${NC}"
    echo -e "  Grafana: ${GREEN}http://localhost:3000${NC} (admin/${GREEN}admin_password_change_me${NC})"
    echo -e "  Kibana: ${GREEN}http://localhost:5601${NC}"
    echo -e "  Prometheus: ${GREEN}http://localhost:9090${NC}"
    echo ""
    echo -e "${BLUE}Important Notes:${NC}"
    echo -e "  • Update the .env file with your actual configuration values"
    echo -e "  • Configure SSL certificates in the nginx/ssl directory"
    echo -e "  • Monitor logs in the logs/ directory"
    echo -e "  • Check the deployment.log for detailed information"
    echo ""
    echo -e "${GREEN}Deployment completed successfully! 🚀${NC}"
}

# Function to cleanup
cleanup() {
    print_status "Cleaning up temporary files..."
    # Add any cleanup tasks here
}

# Main deployment function
main() {
    echo "Starting BizRok Platform deployment..."
    echo "Environment: $ENVIRONMENT"
    echo "Timestamp: $(date)"
    echo ""
    
    # Run deployment steps
    check_prerequisites
    create_directories
    setup_environment
    setup_database
    setup_monitoring
    setup_logging
    setup_nginx
    setup_resources
    build_backend
    deploy_application
    display_summary
    cleanup
    
    echo ""
    print_status "Deployment completed successfully!"
}

# Handle script interruption
trap 'print_error "Deployment interrupted"; exit 1' INT TERM

# Run main function
main "$@"