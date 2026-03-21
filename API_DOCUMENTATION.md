# Bizrok Platform API Documentation

Comprehensive API documentation for the Bizrok Device Buyback Platform.

## 📋 Table of Contents

- [Authentication](#authentication)
- [Public Endpoints](#public-endpoints)
- [User Endpoints](#user-endpoints)
- [Admin Endpoints](#admin-endpoints)
- [Partner Endpoints](#partner-endpoints)
- [Field Executive Endpoints](#field-executive-endpoints)
- [Error Handling](#error-handling)
- [Rate Limiting](#rate-limiting)

## 🔐 Authentication

All authenticated endpoints require a JWT token in the Authorization header.

### Request Format
```
Authorization: Bearer <your-jwt-token>
```

### Token Structure
```json
{
  "email": "user@example.com",
  "role": "USER",
  "exp": 1640995200
}
```

## 🌐 Public Endpoints

### Authentication

#### Send OTP
```
POST /api/auth/send-otp
```

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent successfully"
}
```

#### Login with OTP
```
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "role": "USER",
    "kycVerified": false
  }
}
```

### Device Information

#### Get Categories
```
GET /api/public/categories
```

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "name": "Smartphones",
      "description": "Mobile phones and smartphones",
      "isActive": true,
      "sortOrder": 1
    }
  ]
}
```

#### Get Brands
```
GET /api/public/brands
```

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "name": "Apple",
      "slug": "apple",
      "isActive": true,
      "sortOrder": 1
    }
  ]
}
```

#### Get Models
```
GET /api/public/models?categoryId=1&brandId=2
```

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "name": "iPhone 13",
      "slug": "iphone-13",
      "brandName": "Apple",
      "categoryName": "Smartphones",
      "basePrice": 45000.00,
      "variantInfo": "128GB, Blue",
      "imageUrl": "https://example.com/image.jpg",
      "isActive": true,
      "sortOrder": 1
    }
  ]
}
```

#### Get Model Details
```
GET /api/public/models/{id}
```

**Response:**
```json
{
  "id": 1,
  "name": "iPhone 13",
  "slug": "iphone-13",
  "brandName": "Apple",
  "categoryName": "Smartphones",
  "basePrice": 45000.00,
  "variantInfo": "128GB, Blue",
  "imageUrl": "https://example.com/image.jpg",
  "isActive": true,
  "sortOrder": 1
}
```

### Questions

#### Get All Questions
```
GET /api/public/questions
```

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "text": "Is the display working properly?",
      "slug": "display-working",
      "groupName": "Display",
      "subGroupName": "Functionality",
      "questionType": "SINGLE_SELECT",
      "required": true,
      "isActive": true,
      "sortOrder": 1,
      "options": [
        {
          "id": 1,
          "text": "Yes, display is perfect",
          "slug": "display-perfect",
          "deductionValue": 0.0,
          "deductionType": "FLAT",
          "imageUrl": null,
          "isActive": true,
          "sortOrder": 1
        }
      ]
    }
  ]
}
```

#### Get Questions by Group
```
GET /api/public/questions/group/{groupId}
```

#### Get Questions by Sub-Group
```
GET /api/public/questions/subgroup/{subGroupId}
```

### Pricing

#### Calculate Price
```
POST /api/public/pricing/calculate
```

**Request Body:**
```json
{
  "modelId": 1,
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

**Response:**
```json
{
  "success": true,
  "finalPrice": 35000.00,
  "totalDeductions": 10000.00,
  "groupDeductionsTotal": 10000.00,
  "breakdown": {
    "basePrice": 45000.00,
    "displayDeductions": 5000.00,
    "batteryDeductions": 5000.00,
    "totalDeductions": 10000.00,
    "finalPrice": 35000.00
  }
}
```

## 👤 User Endpoints

### Profile

#### Get Profile
```
GET /api/user/profile
```

**Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "role": "USER",
  "kycVerified": false,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### Orders

#### Create Order
```
POST /api/user/orders
```

**Request Body:**
```json
{
  "modelId": 1,
  "pickupAddress": "123 Main St, City, State",
  "pickupPincode": "123456",
  "pickupDate": "2024-01-15T10:00:00",
  "pickupTime": "10:00 AM - 12:00 PM",
  "bankAccountNumber": "123456789012",
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

**Response:**
```json
{
  "id": 1,
  "orderNumber": "ORD-1234567890",
  "status": "CREATED",
  "model": {
    "id": 1,
    "name": "iPhone 13",
    "brandName": "Apple",
    "categoryName": "Smartphones",
    "basePrice": 45000.00,
    "finalPrice": 35000.00
  },
  "pickupAddress": "123 Main St, City, State",
  "pickupPincode": "123456",
  "pickupDate": "2024-01-15T10:00:00",
  "pickupTime": "10:00 AM - 12:00 PM",
  "bankAccountNumber": "123456789012",
  "bankIfsc": "SBIN0001234",
  "bankAccountName": "John Doe",
  "basePrice": 45000.00,
  "finalPrice": 35000.00,
  "totalDeductions": 10000.00,
  "kycVerified": false,
  "faceMatchVerified": false,
  "bankDetailsVerified": false,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

#### Get Orders
```
GET /api/user/orders
```

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "orderNumber": "ORD-1234567890",
      "status": "CREATED",
      "model": { ... },
      "finalPrice": 35000.00,
      "pickupDate": "2024-01-15T10:00:00",
      "createdAt": "2024-01-01T10:00:00"
    }
  ]
}
```

#### Get Order by ID
```
GET /api/user/orders/{id}
```

#### Update Order Status
```
PUT /api/user/orders/{id}/status
```

**Request Body:**
```json
{
  "status": "CANCELLED"
}
```

### KYC

#### Submit KYC
```
POST /api/user/kyc/submit
```

**Form Data:**
- `documentImage`: File (document photo)
- `selfieImage`: File (selfie photo)

**Response:**
```json
{
  "success": true,
  "message": "KYC submitted successfully",
  "documentId": 1
}
```

#### Get KYC Status
```
GET /api/user/kyc/status
```

**Response:**
```json
{
  "isKycVerified": false,
  "latestDocumentStatus": "PENDING_MANUAL",
  "verificationNotes": "Face match failed, manual verification required",
  "submittedAt": "2024-01-01T10:00:00",
  "verifiedAt": null
}
```

#### Get Price History
```
GET /api/user/orders/{id}/price-history
```

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "basePrice": 45000.00,
      "totalDeductions": 10000.00,
      "finalPrice": 35000.00,
      "groupDeductions": "Display: 5000, Battery: 5000",
      "calculatedAt": "2024-01-01T10:00:00"
    }
  ]
}
```

## 👑 Admin Endpoints

### Dashboard

#### Get Dashboard Stats
```
GET /api/admin/dashboard/stats
```

**Response:**
```json
{
  "totalUsers": 1000,
  "activeUsers": 800,
  "kycVerifiedUsers": 600,
  "totalOrders": 500,
  "pendingOrders": 50,
  "completedOrders": 400,
  "rejectedOrders": 50,
  "totalRevenue": 2500000.00,
  "pendingKycDocuments": 25,
  "totalModels": 100,
  "activeModels": 90
}
```

### Settings

#### Get All Settings
```
GET /api/admin/settings
```

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "key": "ENABLE_KYC",
      "value": "true",
      "isActive": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ]
}
```

#### Update Setting
```
PUT /api/admin/settings
```

**Request Body:**
```json
{
  "key": "ENABLE_KYC",
  "value": "false",
  "active": false
}
```

#### Toggle Feature
```
PUT /api/admin/settings/toggle/{key}
```

**Request Body:**
```json
{
  "enable": true
}
```

### Models

#### Get All Models
```
GET /api/admin/models
```

#### Create Model
```
POST /api/admin/models
```

**Request Body:**
```json
{
  "name": "iPhone 14",
  "slug": "iphone-14",
  "brandName": "Apple",
  "categoryName": "Smartphones",
  "basePrice": 55000.00,
  "variantInfo": "128GB, Midnight",
  "imageUrl": "https://example.com/image.jpg",
  "isActive": true,
  "sortOrder": 2
}
```

#### Update Model
```
PUT /api/admin/models/{id}
```

#### Delete Model
```
DELETE /api/admin/models/{id}
```

### Questions

#### Get All Questions
```
GET /api/admin/questions
```

#### Create Question
```
POST /api/admin/questions
```

**Request Body:**
```json
{
  "text": "Is the battery health above 80%?",
  "slug": "battery-health",
  "questionType": "SINGLE_SELECT",
  "isRequired": true,
  "isActive": true,
  "sortOrder": 2,
  "groupId": 3,
  "subGroupId": 6
}
```

#### Update Question
```
PUT /api/admin/questions/{id}
```

#### Delete Question
```
DELETE /api/admin/questions/{id}
```

### Options

#### Create Option
```
POST /api/admin/options
```

**Request Body:**
```json
{
  "text": "Battery health 80-90%",
  "slug": "battery-80-90",
  "deductionValue": 2000.0,
  "deductionType": "FLAT",
  "imageUrl": null,
  "isActive": true,
  "sortOrder": 1,
  "questionId": 2
}
```

#### Update Option
```
PUT /api/admin/options/{id}
```

#### Delete Option
```
DELETE /api/admin/options/{id}
```

### Orders

#### Get All Orders
```
GET /api/admin/orders
```

#### Get Orders by Status
```
GET /api/admin/orders/status/{status}
```

#### Update Order Status
```
PUT /api/admin/orders/{id}/status
```

**Request Body:**
```json
{
  "status": "COMPLETED",
  "notes": "Order completed successfully"
}
```

#### Assign Order
```
PUT /api/admin/orders/{id}/assign
```

**Request Body:**
```json
{
  "userId": 5
}
```

### Users

#### Get All Users
```
GET /api/admin/users
```

#### Get Users by Role
```
GET /api/admin/users/role/{role}
```

#### Update User Role
```
PUT /api/admin/users/{id}/role
```

**Request Body:**
```json
{
  "role": "PARTNER"
}
```

#### Toggle User Active
```
PUT /api/admin/users/{id}/active
```

**Request Body:**
```json
{
  "active": false
}
```

### KYC

#### Get Pending KYC Documents
```
GET /api/admin/kyc/pending
```

#### Verify KYC Document
```
PUT /api/admin/kyc/{id}/verify
```

**Request Body:**
```json
{
  "verified": true,
  "notes": "Document verified successfully"
}
```

## 🤝 Partner Endpoints

### Orders

#### Get Orders
```
GET /api/partner/orders
```

#### Get Assigned Orders
```
GET /api/partner/orders/assigned
```

#### Accept Order
```
PUT /api/partner/orders/{id}/accept
```

#### Reject Order
```
PUT /api/partner/orders/{id}/reject
```

**Request Body:**
```json
{
  "reason": "Cannot reach the location"
}
```

#### Update Order Details
```
PUT /api/partner/orders/{id}/details
```

**Request Body:**
```json
{
  "finalPrice": 32000.00,
  "notes": "Minor scratch on back panel"
}
```

#### Complete Order
```
PUT /api/partner/orders/{id}/complete
```

## 🕵️ Field Executive Endpoints

### Orders

#### Get Orders
```
GET /api/field/orders
```

#### Get Assigned Orders
```
GET /api/field/orders/assigned
```

#### Update Order Status
```
PUT /api/field/orders/{id}/status
```

**Request Body:**
```json
{
  "status": "IN_PROGRESS"
}
```

#### Update Order Details
```
PUT /api/field/orders/{id}/details
```

#### Complete Order
```
PUT /api/field/orders/{id}/complete
```

**Form Data:**
- `deviceImage`: File (device photo)
- `notes`: String (additional notes)

## ⚠️ Error Handling

### Error Response Format
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input data",
  "path": "/api/user/orders"
}
```

### Common Error Codes

| Code | Error | Description |
|------|-------|-------------|
| 400 | Bad Request | Invalid request format |
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists |
| 422 | Unprocessable Entity | Validation failed |
| 500 | Internal Server Error | Server error |

### Validation Errors
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 422,
  "error": "Validation Failed",
  "message": "Validation failed for object='orderRequest'",
  "errors": [
    "pickupAddress: Pickup address is required",
    "pickupPincode: Invalid pincode format"
  ],
  "path": "/api/user/orders"
}
```

## 🚦 Rate Limiting

### Limits

| Endpoint | Limit | Window |
|----------|-------|---------|
| OTP Requests | 3 per minute | 1 minute |
| Login Attempts | 5 per hour | 1 hour |
| General API | 1000 per hour | 1 hour |

### Rate Limit Headers
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1640995200
```

## 🔒 Security

### Best Practices

1. **Always use HTTPS** in production
2. **Store tokens securely** (httpOnly cookies or secure storage)
3. **Validate all inputs** on both client and server
4. **Use strong passwords** for admin accounts
5. **Monitor API usage** for suspicious activity
6. **Keep dependencies updated**

### CORS Configuration
```yaml
cors:
  allowed-origins: "https://yourdomain.com"
  allowed-methods: "GET,POST,PUT,DELETE"
  allowed-headers: "Authorization,Content-Type"
```

## 📊 Monitoring

### Health Check
```
GET /actuator/health
```

### Metrics
```
GET /actuator/metrics
```

### Logs
Application logs are available at:
- **Development**: Console output
- **Production**: `/var/log/bizrok/application.log`

## 🔄 Webhook Events

### Order Status Changes
```
POST /webhooks/order-status
```

**Payload:**
```json
{
  "eventType": "ORDER_STATUS_CHANGED",
  "orderId": 1,
  "oldStatus": "CREATED",
  "newStatus": "ASSIGNED",
  "timestamp": "2024-01-01T10:00:00"
}
```

### KYC Verification
```
POST /webhooks/kyc-verification
```

**Payload:**
```json
{
  "eventType": "KYC_VERIFIED",
  "userId": 1,
  "documentId": 1,
  "verified": true,
  "timestamp": "2024-01-01T10:00:00"
}
```

## 📞 Support

For API support:
- **Email**: api-support@bizrok.in
- **Documentation**: https://bizrok.in/docs/api
- **Status Page**: https://status.bizrok.in