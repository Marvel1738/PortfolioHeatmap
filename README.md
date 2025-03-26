# ⭐ Portfolio Heatmap

A full-stack stock portfolio visualization application with a custom heatmap feature, built by a first-year computer science student to showcase professional-grade development skills.

## 📌 Project Overview
Portfolio Heatmap is a web application designed to help users manage and visualize their stock portfolios through an interactive heatmap, inspired by Finviz’s S&P 500 heatmap. Unlike traditional heatmaps, this app focuses on user-defined portfolios or custom stock lists, with square sizes representing the percentage of the portfolio and colors indicating price performance (e.g., green for gains, red for losses).

This project is a work-in-progress full-stack application built to professional standards, demonstrating advanced skills in backend development (Spring Boot), database management (MySQL), API integration (FMP, Alpha Vantage), user authentication (Spring Security with JWT), and soon, front-end development (React) and cloud deployment (AWS). As a 19-year-old first-year computer science student, I’m using this project to challenge myself, learn advanced concepts, and create a portfolio piece that stands out for internship applications. The backend is now mostly complete, and I’m currently focusing on learning React to build a high-quality front end that matches the backend’s capabilities.

---

## 🚀 Current Features
### 📊 Stock Management:
- CRUD operations for stocks (add, view, delete stocks).
- Populate stock data using Financial Modeling Prep (FMP) API.
- **Endpoints:**
  - `GET /stocks`
  - `GET /stocks/{id}`
  - `POST /stocks`
  - `POST /stocks/populate`
  - `DELETE /stocks/{id}`

### 📈 Stock Price Retrieval:
- Fetch real-time stock prices using Financial Modeling Prep (FMP) API (250 requests/day, free tier) with Alpha Vantage as a fallback (5/min, 25/day).
- **Endpoints:**
  - `GET /stocks/price/{symbol}`
  - `GET /stocks/batch-prices?symbols=AAPL,MSFT,TSLA`

### ⏳ Historical Price Updates:
- Update historical prices in the database for visualization and performance calculations.
- Populate historical price data for all stocks.
- **Endpoints:**
  - `PUT /stocks/{id}/update-price`
  - `POST /stocks/price-history/populate-all`

### 📁 Portfolio Management:
- Full CRUD operations for portfolios: create, view, and delete portfolios for authenticated users.
- **Endpoints:**
  - `POST /portfolios/create?name=PortfolioName`
  - `GET /portfolios/user`
  - `GET /portfolios/{portfolioId}`
  - `DELETE /portfolios/{portfolioId}`

### 📊 Portfolio Holdings Management:
- Add, update, and delete stock holdings within a portfolio.
- Mark holdings as sold by adding a selling price and date.
- **Endpoints:**
  - `POST /portfolios/{portfolioId}/holdings/add?ticker=AAPL&shares=10&purchasePrice=150.25&purchaseDate=2024-06-15`
  - `PUT /portfolios/holdings/{holdingId}?shares=10&sellingPrice=225.50&sellingDate=2025-03-25`
  - `DELETE /portfolios/holdings/{holdingId}`

### 📈 Performance Metrics:
- Calculate key portfolio metrics: total portfolio value, total open/closed gains/losses, and percentage returns.
- Per-holding gains/losses and percentage returns for both open and closed positions.
- Uses historical price data to compute unrealized gains/losses for open positions.

### 🗄️ Database:
- MySQL database with tables for stocks, portfolios, holdings, users, and historical prices.
- Normalized schema with foreign key relationships (e.g., `portfolio_holdings` links to `portfolios` and `stocks`).

### 🏗 Backend Architecture:
- Spring Boot with a clean separation of concerns (controllers, services, repositories).
- Flexible stock data provider setup using a factory pattern (`StockDataServiceFactory`) to switch between FMP and Alpha Vantage.
- Resolved serialization issues (circular references, large responses) using Jackson annotations (`@JsonIdentityInfo`, `@JsonIgnore`).

### 🔐 User Authentication:
- Spring Security with JWT for user registration, login, and role-based access control.
- Secure endpoints to ensure users can only access their own portfolios.
- **Endpoints:**
  - `POST /auth/register`
  - `POST /auth/login`

---

## 🎯 Planned Features

### 🔥 Portfolio Heatmap Visualization:
- Build an interactive heatmap (similar to Finviz’s S&P 500 heatmap) using React.
- Display stocks from the user’s portfolio or a custom list.
- Square sizes based on the percentage of the portfolio (e.g., larger squares for higher allocation).
- Square colors based on price performance (e.g., green for gains, red for losses).

### 🌐 React Front End:
- Develop a dynamic, responsive front end with React.
- Features: user authentication (login/register), portfolio management (add/view/delete portfolios and holdings), performance metrics display, and heatmap visualization.
- Polished UI with a modern design (e.g., Material-UI, Tailwind CSS).

### 📅 Daily Price Updates:
- Schedule a daily job to update `price_history` with the latest closing prices for all stocks.

### ☁️ AWS Deployment:
- Deploy the backend (Spring Boot) to AWS Elastic Beanstalk and the database to RDS.
- Deploy the React front end to AWS Amplify or S3+CloudFront.
- Set up a custom domain using Route 53 (e.g., `yourportfolioheatmap.com`).
- Enable HTTPS with AWS Certificate Manager.

### 🏢 Professional Features:
- Add caching (Redis) for same-day stock prices to reduce API calls.
- Implement error handling with consistent responses (e.g., 404 for not-found).
- Optimize database performance with indexing and partitioning.
- Set up monitoring with AWS CloudWatch.

---

## 🛠️ Tech Stack
### 📌 Backend
- **Spring Boot**: REST API framework.
- **MySQL**: Relational database for storing stocks, portfolios, holdings, users, and historical prices.
- **Spring Data JPA**: For database interaction.
- **Financial Modeling Prep (FMP) API**: Primary stock data provider (250 requests/day, free tier).
- **Alpha Vantage API**: Fallback stock data provider (5/min, 25/day).
- **Spring Security**: For user authentication with JWT.

### 🔜 Planned
- **React**: Front-end framework for building the UI and heatmap.
- **D3.js or React Heatmap Library**: For the portfolio heatmap visualization.
- **AWS**:
  - Elastic Beanstalk: Backend hosting.
  - RDS: MySQL database hosting.
  - Amplify/S3+CloudFront: Front-end hosting.
  - Route 53: Custom domain.
  - ElastiCache (Redis): Caching.
- **Material-UI or Tailwind CSS**: For a polished, professional front end.

---

## ⚙️ Setup Instructions
### 🔹 Prerequisites
- Java 17+
- MySQL 8.0+
- Maven
- FMP API Key (sign up at [financialmodelingprep.com](https://financialmodelingprep.com))
- Alpha Vantage API Key (sign up at [alphavantage.co](https://www.alphavantage.co))

### 🔹 Installation
#### 1️⃣ Clone the Repository:
```bash
git clone https://github.com/Marvel1738/portfolio-heatmap.git
cd portfolio-heatmap
```

#### 2️⃣ Set Up MySQL:
```sql
CREATE DATABASE portfolio_heatmap;
```
The schema (stocks, portfolio, users, price_history) will be created automatically by Spring Data JPA (`spring.jpa.hibernate.ddl-auto=update`).

#### 3️⃣ Configure Application Properties:
Edit `src/main/resources/application.properties`:

#### 4️⃣ Run the Application:
```bash
mvn spring-boot:run
```
The app will start on `http://localhost:8080`.

#### 5️⃣ Test Endpoints:
Use Postman or curl to test:
Authenticate:
```bash
POST http://localhost:8080/auth/login
Body: {"username": "yourusername", "password": "yourpassword"}
```
Copy the JWT token from the response.

Test Enpoints (include Authorization: Bearer <token> in headers):
```bash
POST http://localhost:8080/portfolios/create?name=MyPortfolio
GET http://localhost:8080/portfolios/user
GET http://localhost:8080/portfolios/{portfolioId}
POST http://localhost:8080/portfolios/{portfolioId}/holdings/add?ticker=AAPL&shares=10&purchasePrice=150.25&purchaseDate=2024-06-15
GET http://localhost:8080/stocks/price/AAPL
GET http://localhost:8080/stocks/batch-prices?symbols=AAPL,MSFT,TSLA
```

---

## 🎯 Future Goals
- **Professional Portfolio:** Showcase this project to employers.
- **React Front End: Build a high-quality React front end within the next few months to match the backend’s capabilities.
- **Potential Startup:** Monetization through subscriptions or ads.
- **Learning and Growth:** Advance skills in authentication, React, and AWS deployment.

---

## 🤝 Contributing
Contributions are welcome! If you’d like to contribute:
1. Fork the repository.
2. Create a new branch: `git checkout -b feature/your-feature`.
3. Commit your changes: `git commit -m 'Add your feature'`.
4. Push to the branch: `git push origin feature/your-feature`.
5. Open a pull request.

---

## 📜 License
This project is licensed under the MIT License. See the LICENSE file for details.

 - Author: [Marvel Bana] (19-year-old first-year computer science student)
 - Email: [marvelbana6@@gmail.com] 
 - GitHub: [Marvel1738] 
 - LinkedIn: [] 
 - License
 - This project is licensed under the MIT License. See the file for details.
