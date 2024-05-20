
# EuroSportHub 🏆

EuroSportHub is A Euro 2024 application built with Java designed to keep you updated with the latest live scores, team statistics, and match details. This application leverages cutting-edge technologies to provide real-time data and a seamless user experience. 

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Backend Logic](#backend-logic)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Features ✨

- **Live Scores**: Real-time updates of ongoing matches.
- **Team and Player Statistics**: Detailed statistics for teams and players.
- **Search Functionality**: Search for specific teams or matches.
- **Date Range Selector**: Easily navigate through different match dates.
- **Group Listings**: View groups and teams participating in various leagues.
- **User Authentication**: Secure login and registration system.
- **User Profile**: Manage your personal profile and preferences.

## Technologies Used 💻

### Backend
- **Java**
- **Spring Boot**
- **Spring Data JPA**
- **Spring Security**
- **MongoDB**
- **RestTemplate**

### Tools 🛠️
- **Maven**
- **Postman**
- **Git**
- **GitHub**

### Key Classes and Files 📂

1. **`DemoApplication.java`**: The main entry point for the Spring Boot application.

2. **`UserController.java`**: Handles user-related HTTP requests.

3. **`MatchController.java`**: Manages match-related endpoints.

4. **`AuthController.java`**: Manages authentication and authorization endpoints.

5. **`UserService.java`**: Contains business logic for user operations.

6. **`MatchService.java`**: Contains business logic for match operations.

7. **`SecurityConfig.java`**: Configures Spring Security for the application.

8. **`application.properties`**: Configuration file for application settings.
   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/eurosporthub
   spring.security.jwt.secret=mySecretKey
   LIVE_SCORE_API_URL = FOOTBALL_SCORE_API_URL
   LIVE_SCORE_API_KEY = YOUR_API_TOKEN_KEY
   LIVE_SCORE_API_SECRET = YOUR_API_SECRET_KEY
   ```

## Installation 🛠️

### Prerequisites

- Java 17
- Maven
- MongoDB
- Node.js
- npm

### Backend Setup

1. Clone the repository:
   ```sh
   git clone https://github.com/Moddic10/EuroSporthub---A-Football-Streaming-app.git
   cd EuroSportHub/backend
   ```

2. **Database Setup**:

   Ensure you have MongoDB installed and running. Create a database named `euro_sporthub`.

3. **Environment Variables**:

   Create a `.env` file in the root of your project and add the following keys:

   ```env
   LIVESCORE_API_KEY=your_api_key_here
   LIVESCORE_API_SECRET=your_api_secret_here
   DATABASE_URL=mongodb://localhost:27017/euro_sporthub
   ```

4. Install dependencies:
   ```sh
   mvn clean install
   ```

5. Configure MongoDB in `application.properties`:
   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/eurosporthub
   ```

5. Run the application:
   ```sh
   mvn spring-boot:run
   ```
   
## Backend Logic 🔧

## API Endpoints 📡

### Authentication

- **Login**: `POST /api/auth/login`
- **Register**: `POST /api/auth/register`

### Matches

- **Get Live Matches**: `GET /api/matches/live`
- **Get Match Details**: `GET /api/matches/{matchId}`

### Teams

- **Get Teams**: `GET /api/teams`
- **Get Team Details**: `GET /api/teams/{teamId}`

### Example API Request

To get live matches, you can use the following cURL command:

```sh
curl -X GET "http://localhost:8080/api/matches/live" -H "accept: application/json"
```

## Contributing 🤝

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License 📄

Distributed under the MIT License. See `LICENSE` for more information.

## Contact 📧

Your Name - [Muhammad Said](mailto:mohammedabdy10@gmail.com)

Project Link: [https://github.com/Moddic10/EuroSportHub](https://github.com/Moddic10/EuroSporthub---A-Football-Streaming-app)
