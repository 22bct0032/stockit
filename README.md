# StockIt - Full-Stack Stock Trading Application 📈

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg)
![Node.js](https://img.shields.io/badge/Backend-Node.js-green.svg)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

A modern, production-ready stock trading application with a beautiful Android frontend and robust Node.js backend, featuring real-time market data, portfolio management, and secure authentication.

[Features](#-features) • [Tech Stack](#-tech-stack) • [Setup](#-setup) • [API Documentation](#-api-documentation)

</div>

---

## 🌟 Features

### 📱 Android App
- **Modern UI/UX** - Dark theme with glassmorphism effects and smooth animations
- **Real-Time Trading** - Buy and sell stocks with live price updates
- **Portfolio Management** - Track holdings, P&L, and transaction history
- **Watchlist** - Save and monitor favorite stocks
- **Secure Authentication** - JWT-based login/signup with token management
- **Offline Support** - Cached data for seamless experience

### 🔧 Backend API
- **RESTful API** - Clean, well-documented endpoints
- **PostgreSQL Database** - Persistent data storage with relational integrity
- **JWT Authentication** - Secure user sessions
- **Mock Stock Data** - Realistic stock market simulation
- **Transaction Management** - Complete buy/sell order processing
- **Admin Dashboard** - View user data and statistics

---

## 🛠️ Tech Stack

### Frontend (Android)
- **Kotlin** - Modern, type-safe programming language
- **Jetpack Compose** - Declarative UI framework
- **Retrofit** - Type-safe HTTP client
- **Hilt** - Dependency injection
- **Coroutines** - Asynchronous programming
- **Material 3** - Modern design system

### Backend (API)
- **Node.js** - JavaScript runtime
- **Express.js** - Web framework
- **PostgreSQL** - Relational database
- **JWT** - Authentication tokens
- **Bcrypt** - Password hashing
- **CORS** - Cross-origin resource sharing

### Deployment
- **Render.com** - Backend hosting (PostgreSQL + Web Service)
- **GitHub** - Version control and CI/CD

---

## 🚀 Quick Start

### Prerequisites
- **Android Studio** (Arctic Fox or later)
- **Node.js** (v18+)
- **PostgreSQL** (for local development)
- **Git**

### 1. Clone the Repository

```bash
git clone https://github.com/22bct0032/stockit.git
cd stockit
```

### 2. Setup Backend

```bash
cd stockit-backend

# Install dependencies
npm install

# Create .env file
cp .env.example .env

# Edit .env with your configuration
# For local development, it will use SQLite
# For production, add DATABASE_URL for PostgreSQL

# Start server
npm run dev
```

Backend will run on `http://localhost:3000`

### 3. Setup Android App

```bash
# Open project in Android Studio
# File > Open > select stockit folder

# Update API URL in ApiConfig.kt
# For local: http://10.0.2.2:3000/ (Android emulator)
# For production: https://stockit-api-yqi5.onrender.com/

# Build and run
./gradlew installDebug
```

---

## 📡 API Documentation

### Base URL
**Production**: `https://stockit-api-yqi5.onrender.com`  
**Local**: `http://localhost:3000`

### Authentication Endpoints

#### Sign Up
```http
POST /api/auth/signup-simple
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "confirmPassword": "SecurePass123"
}
```

#### Sign In
```http
POST /api/auth/signin-simple
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

### Stock Endpoints

#### Get Trending Stocks
```http
GET /api/trending
```

#### Get Stock Quote
```http
GET /api/stock/:symbol
```

#### Search Stocks
```http
GET /api/search?q=AAPL
```

### User Endpoints (Requires Authentication)

Add header: `Authorization: Bearer <access_token>`

#### Get Wallet
```http
GET /api/user/wallet
```

#### Get Portfolio
```http
GET /api/user/portfolio
```

#### Buy Stock
```http
POST /api/user/stocks/buy
Content-Type: application/json

{
  "symbol": "AAPL",
  "quantity": 10,
  "pricePerShare": 150.25
}
```

#### Sell Stock
```http
POST /api/user/stocks/sell
Content-Type: application/json

{
  "symbol": "AAPL",
  "quantity": 5,
  "pricePerShare": 155.50
}
```

#### Get Transactions
```http
GET /api/user/transactions?limit=50&offset=0
```

#### Get Watchlist
```http
GET /api/user/watchlist
```

#### Add to Watchlist
```http
POST /api/user/watchlist
Content-Type: application/json

{
  "symbol": "GOOGL"
}
```

#### Remove from Watchlist
```http
DELETE /api/user/watchlist/:symbol
```

### Admin Endpoints

Add query parameter: `?password=admin123`

#### Get All Users
```http
GET /api/admin/users?password=admin123
```

#### Get User Details
```http
GET /api/admin/users/:userId?password=admin123
```

#### Get Database Stats
```http
GET /api/admin/stats?password=admin123
```

---

## 🗄️ Database Schema

### Users
- `id` - Primary key
- `fullName` - User's full name
- `email` - Unique email address
- `password` - Hashed password
- `createdAt` - Registration timestamp

### Wallets
- `id` - Primary key
- `userId` - Foreign key to users
- `balance` - Current cash balance
- `totalInvested` - Total amount invested
- `updatedAt` - Last update timestamp

### Portfolios
- `id` - Primary key
- `userId` - Foreign key to users
- `symbol` - Stock symbol
- `companyName` - Company name
- `quantity` - Number of shares
- `avgPrice` - Average purchase price
- `firstBuyDate` - First purchase date
- `lastUpdated` - Last update timestamp

### Transactions
- `id` - Primary key
- `userId` - Foreign key to users
- `symbol` - Stock symbol
- `companyName` - Company name
- `transactionType` - BUY or SELL
- `quantity` - Number of shares
- `price` - Price per share
- `totalAmount` - Total transaction amount
- `transactionDate` - Transaction timestamp

### Watchlists
- `id` - Primary key
- `userId` - Foreign key to users
- `symbol` - Stock symbol
- `companyName` - Company name
- `addedAt` - Timestamp when added

---

## 🔧 Configuration

### Environment Variables (Backend)

Create `.env` file in `stockit-backend/`:

```env
# Server
PORT=3000
NODE_ENV=development

# JWT
JWT_SECRET=your-super-secret-jwt-key-change-this
JWT_EXPIRES_IN=30d

# Database (Production)
DATABASE_URL=postgresql://user:password@host/database

# App Config
STARTING_BALANCE=100000

# Admin
ADMIN_PASSWORD=admin123
```

### Android App Configuration

Update `app/src/main/java/com/example/stockit/network/ApiConfig.kt`:

```kotlin
private const val BASE_URL = "https://stockit-api-yqi5.onrender.com/"
```

---

## 🚢 Deployment

### Backend (Render.com)

1. **Create PostgreSQL Database**
   - Go to Render Dashboard
   - Create new PostgreSQL database
   - Copy connection string

2. **Deploy Web Service**
   - Connect GitHub repository
   - Set build command: `npm install`
   - Set start command: `npm start`
   - Add environment variables (DATABASE_URL, JWT_SECRET, etc.)

3. **Access**
   - Backend URL: `https://stockit-api-yqi5.onrender.com`
   - Database: Render PostgreSQL

### Android App

1. **Build APK**
   ```bash
   ./gradlew assembleRelease
   ```

2. **Install**
   ```bash
   ./gradlew installRelease
   ```

3. **Distribute**
   - Upload to Google Play Store
   - Or share APK directly

---

## 📊 Project Structure

```
stockit/
├── app/                          # Android app
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/stockit/
│   │   │   │   ├── data/        # Data models
│   │   │   │   ├── network/     # API client
│   │   │   │   ├── ui/          # UI components
│   │   │   │   ├── utils/       # Utilities
│   │   │   │   └── StockItApplication.kt
│   │   │   └── res/             # Resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── stockit-backend/              # Node.js backend
│   ├── src/
│   │   ├── config/              # Database config
│   │   ├── controllers/         # Request handlers
│   │   ├── middleware/          # Auth middleware
│   │   ├── routes/              # API routes
│   │   ├── services/            # Business logic
│   │   └── app.js               # Main server
│   ├── .env                     # Environment variables
│   ├── package.json
│   └── README.md
├── screenshots/                  # App screenshots
├── .gitignore
├── LICENSE
└── README.md                    # This file
```

## 🔐 Security

- **Password Hashing**: Bcrypt with salt rounds
- **JWT Tokens**: Secure authentication with expiration
- **HTTPS**: All production traffic encrypted
- **Input Validation**: Server-side validation for all inputs
- **SQL Injection Prevention**: Parameterized queries
- **CORS**: Configured for security

---

## 🧪 Testing

### Backend Tests
```bash
cd stockit-backend
npm test
```

### Android Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## 📈 Future Enhancements

- [ ] Real stock data integration (Alpha Vantage, Yahoo Finance)
- [ ] Real-time WebSocket updates
- [ ] Stock price charts and technical indicators
- [ ] Push notifications for price alerts
- [ ] Social features (share trades, leaderboards)
- [ ] Advanced order types (limit, stop-loss)
- [ ] Multi-currency support
- [ ] Dark/Light theme toggle
- [ ] Biometric authentication

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Ashish** - [22bct0032](https://github.com/22bct0032)

---

## 🙏 Acknowledgments

- Jetpack Compose for modern Android UI
- Render.com for free hosting
- Material Design for UI guidelines
- Express.js community for excellent documentation

---

## 📞 Support

For support, kumarashish20032003@gmail.com or open an issue on GitHub.

---

<div align="center">

**⭐ Star this repo if you find it helpful!**

Made with ❤️ by Ashish

</div>
