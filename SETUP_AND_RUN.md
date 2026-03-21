# Bizrok Platform - Quick Setup & Run Guide

This guide provides the fastest way to get the Bizrok platform running on your system.

## 🚀 Quick Start (Docker - Easiest)

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop) installed

### Steps
1. **Clone or download the project**
2. **Open terminal in the project root directory**
3. **Run the following command:**

```bash
docker-compose up -d
```

4. **Wait for containers to start (2-3 minutes)**
5. **Access the application:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - Health Check: http://localhost:8080/actuator/health

6. **Test the API:**
```bash
curl http://localhost:8080/api/public/categories
```

## 🧑‍💻 Manual Setup (If you prefer local development)

### Prerequisites
- Java 17+
- Maven
- Node.js v18+
- npm

### Backend Setup
```bash
cd bizrok-platform/backend

# Install dependencies
mvn clean install

# Start backend
mvn spring-boot:run
```
Backend will be available at: http://localhost:8080

### Frontend Setup
```bash
cd ../frontend

# Install dependencies
npm install

# Start frontend
npm run dev
```
Frontend will be available at: http://localhost:3000

## 🔐 Testing the Application

### Default Credentials
- **Email OTP**: Use `123456` if email is not configured
- **Admin User**: Create via registration flow

### Test Flow
1. Visit http://localhost:3000
2. Click "Start Selling Now"
3. Select a device category and brand
4. Choose a model
5. Answer condition questions
6. View price calculation
7. Create an order

## 🛠️ Configuration

### Environment Variables

#### Backend (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:sqlite:db/bizrok.db
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password

bizrok:
  otp:
    expiry: 5
    max-attempts: 3
    length: 6
```

#### Frontend (.env.local)
```
VITE_API_BASE_URL=http://localhost:8080/api
```

## 🔍 API Endpoints to Test

### Public Endpoints
```bash
# Get device categories
curl http://localhost:8080/api/public/categories

# Get device brands
curl http://localhost:8080/api/public/brands

# Get models by category
curl "http://localhost:8080/api/public/models?categoryId=1"

# Calculate price
curl -X POST http://localhost:8080/api/public/pricing/calculate \
  -H "Content-Type: application/json" \
  -d '{"modelId": 1, "answers": []}'
```

### Authentication
```bash
# Send OTP
curl -X POST http://localhost:8080/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'

# Login with OTP
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "otp": "123456"}'
```

## 🐛 Troubleshooting

### Common Issues

#### 1. Port Already in Use
```bash
# Check what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID)
taskkill /PID <PID> /F
```

#### 2. Database Issues
```bash
# Delete and recreate database
rm -rf db/
# Restart application
```

#### 3. CORS Errors
- Ensure frontend `.env.local` has correct API URL
- Check backend CORS configuration

#### 4. Maven Issues
```bash
# Clean and rebuild
mvn clean install -U
```

#### 5. Node.js Issues
```bash
# Clear npm cache
npm cache clean --force

# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

### Docker Issues
```bash
# Stop all containers
docker-compose down

# Remove volumes and rebuild
docker-compose down -v --remove-orphans
docker-compose up -d --build

# Check container logs
docker-compose logs
```

## 📊 Monitoring

### Health Checks
```bash
# Backend health
curl http://localhost:8080/actuator/health

# Frontend health
curl http://localhost:3000/
```

### Application Logs
```bash
# View backend logs
docker-compose logs backend

# View frontend logs
docker-compose logs frontend
```

## 🚀 Production Deployment

### Using Docker
```bash
# Build production images
docker-compose -f docker-compose.prod.yml up -d

# Monitor logs
docker-compose -f docker-compose.prod.yml logs -f
```

### Using Traditional Deployment
1. Build backend: `mvn clean package`
2. Build frontend: `npm run build`
3. Deploy to your server
4. Configure reverse proxy (Nginx/Apache)

## 📞 Support

If you encounter issues:

1. **Check logs**: `docker-compose logs` or application logs
2. **Verify ports**: Ensure 8080 and 3000 are available
3. **Check configuration**: Verify environment variables
4. **Test connectivity**: Use curl to test API endpoints

For additional help, refer to:
- [API Documentation](API_DOCUMENTATION.md)
- [Deployment Guide](DEPLOYMENT_GUIDE.md)
- [README](README.md)

## ✅ Success Checklist

- [ ] Docker Desktop installed (for Docker setup)
- [ ] Java 17+ and Maven installed (for manual setup)
- [ ] Node.js v18+ installed
- [ ] Backend running on http://localhost:8080
- [ ] Frontend running on http://localhost:3000
- [ ] API endpoints responding
- [ ] Device selection flow working
- [ ] Price calculation functional
- [ ] Order creation working

You're now ready to use the Bizrok Device Buyback Platform! 🎉