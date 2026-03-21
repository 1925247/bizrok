# Bizrok Device Buyback Platform

A comprehensive device buyback platform with advanced features including OCR-based KYC, face detection, and dynamic pricing.

## 🚀 Features

### Core Features
- **Device Selection Flow**: Intuitive device selection with categories and brands
- **Dynamic Pricing**: Config-driven pricing engine with question-based deductions
- **Multi-role Authentication**: User, Partner, Field Executive, and Admin roles
- **Order Management**: Complete order lifecycle from creation to completion
- **Config-driven System**: All business logic controlled via admin settings

### Advanced Features
- **OCR Integration**: Document scanning and text extraction (Aadhaar, PAN, Driving License)
- **Face Detection**: Biometric verification with confidence scoring
- **Email OTP**: Secure email-based authentication
- **KYC Verification**: Multi-modal verification with auto-approval
- **Real-time Updates**: Live order status tracking

## 🏗️ Architecture

### Backend (Spring Boot)
- **Java 17** with Spring Boot 3.2.0
- **SQLite Database** with JPA/Hibernate
- **JWT Authentication** with role-based access control
- **Config-driven Design** - Zero hardcoding architecture
- **RESTful APIs** with comprehensive endpoints

### Frontend (React)
- **React 18** with TypeScript
- **Material-UI** for consistent design
- **React Query** for state management
- **React Router** for navigation
- **Vite** for fast development

## 📁 Project Structure

```
bizrok-platform/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/com/bizrok/
│   │   ├── BizrokApplication.java          # Main application
│   │   ├── config/                         # Configuration classes
│   │   ├── controller/                     # REST API controllers
│   │   ├── service/                        # Business logic
│   │   ├── repository/                     # JPA repositories
│   │   ├── model/
│   │   │   ├── entity/                     # JPA entities
│   │   │   └── dto/                        # Data transfer objects
│   │   ├── util/                           # Utility classes
│   │   └── exception/                      # Custom exceptions
│   └── src/main/resources/
│       ├── application.yml                 # Spring configuration
│       ├── schema.sql                      # Database schema
│       └── data.sql                        # Seed data
├── frontend/                   # React Frontend
│   ├── src/
│   │   ├── App.tsx                         # Main app component
│   │   ├── main.tsx                        # App entry point
│   │   ├── components/                     # Reusable components
│   │   ├── pages/                          # Page components
│   │   ├── hooks/                          # Custom hooks
│   │   ├── services/                       # API services
│   │   └── utils/                          # Utility functions
│   ├── package.json                        # Dependencies
│   └── vite.config.ts                      # Vite configuration
└── README.md                   # This file
```

## 🛠️ Technology Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Web framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database access
- **SQLite** - Database
- **JWT** - Token-based authentication
- **Tesseract OCR** - Document scanning
- **OpenCV** - Face detection
- **Lombok** - Code generation

### Frontend
- **React 18** - UI framework
- **TypeScript** - Type-safe JavaScript
- **Material-UI** - Component library
- **React Query** - State management
- **React Router** - Navigation
- **Axios** - HTTP client
- **Vite** - Build tool

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Maven (for backend)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd bizrok-platform/backend
   ```

2. **Install dependencies**
   ```bash
   mvn clean install
   ```

3. **Configure application**
   Edit `src/main/resources/application.yml`:
   ```yaml
   bizrok:
     otp:
       expiry: 5        # OTP expiry in minutes
       max-attempts: 3  # Maximum OTP attempts
       length: 6        # OTP length
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd ../frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment**
   Create `.env.local`:
   ```
   VITE_API_BASE_URL=http://localhost:8080/api
   ```

4. **Run the development server**
   ```bash
   npm run dev
   ```

   The frontend will start on `http://localhost:3000`

## 📖 API Documentation

### Authentication Endpoints

#### POST /api/auth/send-otp
Send OTP to email
```json
{
  "email": "user@example.com"
}
```

#### POST /api/auth/login
Login with OTP
```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

### Device Endpoints

#### GET /api/public/categories
Get all device categories

#### GET /api/public/brands
Get all device brands

#### GET /api/public/models?categoryId=1&brandId=2
Get models by category and brand

### Order Endpoints

#### POST /api/user/orders
Create new order
```json
{
  "modelId": 1,
  "pickupAddress": "123 Main St",
  "pickupPincode": "123456",
  "pickupDate": "2024-01-01T10:00:00",
  "pickupTime": "10:00 AM",
  "bankAccountNumber": "1234567890",
  "bankIfsc": "SBIN0001234",
  "bankAccountName": "John Doe",
  "answers": [
    {
      "questionId": 1,
      "optionId": 2,
      "answerText": null,
      "imageUrl": null
    }
  ]
}
```

### Admin Endpoints

#### GET /api/admin/dashboard/stats
Get dashboard statistics

#### PUT /api/admin/settings
Update system settings
```json
{
  "key": "ENABLE_KYC",
  "value": "true",
  "active": true
}
```

## 🔧 Configuration

### System Settings
All business logic is controlled via the Settings table:

| Key | Description | Default |
|-----|-------------|---------|
| `ENABLE_KYC` | Enable KYC verification | `false` |
| `ENABLE_EMAIL_OTP` | Enable email OTP | `true` |
| `ENABLE_FACE_MATCH` | Enable face detection | `false` |
| `ENABLE_BANK_CHECK` | Enable bank verification | `false` |
| `MIN_PRICE_PERCENT` | Minimum price percentage | `20.0` |
| `MAX_DEDUCTION_PERCENT` | Maximum deduction percentage | `80.0` |
| `OTP_EXPIRY_MINUTES` | OTP expiry time | `5` |
| `MAX_OTP_ATTEMPTS` | Maximum OTP attempts | `3` |

### Environment Variables

#### Backend
```yaml
# application.yml
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

#### Frontend
```
# .env.local
VITE_API_BASE_URL=http://localhost:8080/api
```

## 🎯 Usage Examples

### Device Selection Flow
1. User visits homepage
2. Selects device category (Smartphone, Laptop, etc.)
3. Selects brand (Apple, Samsung, etc.)
4. Browses available models
5. Selects specific model variant
6. Proceeds to question flow

### Question Flow
1. System loads questions based on device type
2. User answers condition questions (Display, Battery, etc.)
3. Each answer applies deductions to base price
4. System calculates final price
5. User reviews order summary

### Order Completion
1. User provides pickup details
2. KYC verification (if enabled)
3. Order creation with price calculation
4. Partner assignment
5. Field executive verification
6. Payment processing

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Role-based Access**: Different permissions for each user type
- **Email OTP**: Two-factor authentication
- **KYC Verification**: Document and face verification
- **Input Validation**: Comprehensive validation on all inputs
- **CORS Protection**: Configured CORS for security

## 📊 Database Schema

The system uses SQLite with the following main entities:

- **Users**: User accounts with roles
- **Categories**: Device categories (Smartphone, Laptop, etc.)
- **Brands**: Device brands (Apple, Samsung, etc.)
- **Models**: Specific device models with base prices
- **Questions**: Dynamic questions for price calculation
- **Options**: Answer options with deduction values
- **Orders**: Order lifecycle management
- **Settings**: Config-driven system settings

## 🚀 Deployment

### Backend Deployment
1. Build the application:
   ```bash
   mvn clean package
   ```

2. Run the JAR:
   ```bash
   java -jar target/bizrok-0.0.1-SNAPSHOT.jar
   ```

### Frontend Deployment
1. Build the application:
   ```bash
   npm run build
   ```

2. Serve the build files with any static file server

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for your changes
5. Run the test suite
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spring Boot for the robust backend framework
- React for the flexible frontend library
- Material-UI for beautiful, consistent components
- Tesseract OCR for document scanning capabilities
- OpenCV for face detection functionality

## 📞 Support

For support and questions:
- Create an issue on GitHub
- Email: support@bizrok.in
- Website: https://bizrok.in