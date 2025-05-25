# Telegram Authentication WebApp

This project implements a basic authentication system via Telegram WebApp. It verifies `initData`, saves user information to a PostgreSQL database, and displays the user's data on the homepage. The app is containerized using Docker and can be run via Docker Compose.

---

## 🧩 Features

- Authentication through Telegram WebApp
- Validation of Telegram `initData`
- User data storage in PostgreSQL
- Display of user info on frontend
- Docker + Docker Compose support

---

## 🛠️ Tech Stack

- **Language:** Java 21, JavaScript, HTML
- **Frameworks:** Spring Boot, Spring Security
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **Containerization:** Docker, Docker Compose

---

## 📂 Project Structure

```
telegram_authentication-main/
├── .dockerignore
├── .gitattributes
├── .gitignore
├── Dockerfile
├── README.Docker.md
├── compose.yaml
├── mvnw
├── mvnw.cmd
├── pom.xml
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── http-requests/
│   └── get-userinfo.http
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── tg/
│       │           └── authtelegram/
│       │               ├── AuthTelegramApplication.java
│       │               ├── bot/
│       │               │   └── MyTelegramBot.java
│       │               ├── config/
│       │               │   └── BotConfig.java
│       │               ├── controller/
│       │               │   ├── TelegramAuthController.java
│       │               │   └── ViewController.java
│       │               ├── model/
│       │               │   └── TelegramUser.java
│       │               ├── repository/
│       │               │   └── TelegramUserRepository.java
│       │               └── service/
│       │                   └── TelegramAuthService.java
│       └── resources/
│           ├── application.properties
│           └── templates/
│               ├── index.html
│               └── telegram.html
└── src/
    └── test/
        └── java/
            └── com/
                └── tg/
                    └── authtelegram/
                        └── AuthTelegramApplicationTests.java
```

---

## 🔧 Environment Variables

Create a `.env` file in the project root with the following variables:

```env
SPRING_DATASOURCE_URL=your_database_url
SPRING_DATASOURCE_USERNAME=your_database_username
SPRING_DATASOURCE_PASSWORD=your_database_password
TELEGRAM_BOT_TOKEN=bot_token
TELEGRAM_BOT_USERNAME=bot_username
```

These variables are used in `compose.yaml` to configure the services.

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/IamAbdullayev/telegram_authentication.git
cd telegram_authentication
```

### 2. Create `.env` file

Define all environment variables as shown above.

### 3. Build and Run with Docker Compose
```bash
docker compose -f compose.yaml up --build
```
This command will build and start your application and database containers locally.

---

### 4. Open in Browser
After running the app, open it locally in your browser:  
[http://localhost:8080](http://localhost:8080)

---

### 🌐 Make It Public with ngrok (for Telegram Mini App)

To connect your Telegram Mini App to your backend, the application must be accessible from the internet. One easy way to achieve this in a local development environment is by using [ngrok](https://ngrok.com/).

#### Steps:
1. **Install ngrok** (if not installed):
   ```bash
   npm install -g ngrok
   ```
   Or download it from the [official site](https://ngrok.com/download).

2. **Expose your local server**:
   ```bash
   ngrok http 8080
   ```

3. **Copy the generated HTTPS URL**, e.g.:  
   ```
   https://1234abcd.ngrok.io
   ```

4. **Set this URL in your Telegram bot**:
   - Open [BotFather](https://t.me/BotFather)
   - Set the WebApp URL in the menu button configuration or inside your bot logic (`web_app.url`)
   - Now, when users open the Mini App, it will connect to your local app via ngrok.

---

## 👤 Author

**Ramazan Abdullayev**  
[GitHub](https://github.com/IamAbdullayev) · [Telegram](https://t.me/iamabdullayev) · [LinkedIn](https://www.linkedin.com/in/ramazanabdu11ayev)

---

Feel free to contribute or open issues if you find bugs or have suggestions.
