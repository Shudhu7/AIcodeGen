# AI Code Generator Backend

A Spring Boot REST API that leverages Google's Gemini AI to generate code based on user prompts. The application supports multiple programming languages and maintains a history of all code generation requests.

## Features

- **AI-Powered Code Generation**: Uses Google Gemini AI to generate production-ready code
- **Multi-Language Support**: Supports 20+ programming languages including Java, Python, JavaScript, TypeScript, C++, and more
- **Request History**: Stores and tracks all code generation requests with metadata
- **Statistics & Analytics**: Provides detailed statistics on usage patterns and performance
- **Search & Filter**: Search through generation history by keywords or filter by programming language
- **Error Handling**: Comprehensive error handling with detailed logging
- **Performance Monitoring**: Tracks execution times and success rates
- **Health Checks**: Built-in health endpoints for monitoring
- **CORS Support**: Configured for cross-origin requests

## Technology Stack

- **Java 17**
- **Spring Boot 3.3.5**
- **Spring Data JPA**
- **MySQL 8**
- **Google Gemini AI API**
- **Lombok**
- **Maven**
- **JUnit 5 & Mockito** (Testing)

## Prerequisites

Before running the application, ensure you have:

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Google Gemini API key

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/Shudhu7/AIcodeGen.git
cd ai-code-generator-backend
```

### 2. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE aicodegen;
CREATE USER 'aicodegen_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON aicodegen.* TO 'aicodegen_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configuration

Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/aicodegen
spring.datasource.username=your_username
spring.datasource.password=your_password

# Google Gemini AI Configuration
gemini.api.key=your_gemini_api_key_here
```

### 4. Get Gemini API Key

1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create a new API key
3. Add the key to your `application.properties`

### 5. Build and Run

```bash
# Build the application
mvn clean compile

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Code Generation

#### Generate Code
```http
POST /api/codegen
Content-Type: application/json

{
  "prompt": "Create a function to calculate factorial of a number",
  "language": "Java"
}
```

**Response:**
```json
{
  "generatedCode": "public static long factorial(int n) {\n    if (n <= 1) return 1;\n    return n * factorial(n - 1);\n}",
  "prompt": "Create a function to calculate factorial of a number",
  "language": "Java",
  "timestamp": "2024-01-15T10:30:00",
  "executionTimeMs": 1500,
  "success": true,
  "errorMessage": null
}
```

### History & Search

#### Get All History
```http
GET /api/codegen/history
```

#### Get Recent History
```http
GET /api/codegen/history/recent?limit=10
```

#### Get History by Language
```http
GET /api/codegen/history/language/Java
```

#### Search History
```http
GET /api/codegen/history/search?keyword=factorial
```

### Statistics

#### Basic Statistics
```http
GET /api/codegen/stats
```

**Response:**
```json
{
  "totalGenerations": 150,
  "successfulGenerations": 145,
  "failedGenerations": 5,
  "averageExecutionTimeMs": 1250.5,
  "successRate": 96.67,
  "languageUsage": {
    "Java": 45,
    "Python": 38,
    "JavaScript": 32,
    "TypeScript": 20
  }
}
```

#### Detailed Statistics
```http
GET /api/codegen/stats/detailed
```

### Utility Endpoints

#### Supported Languages
```http
GET /api/codegen/languages
```

#### Health Check
```http
GET /api/codegen/health
```

#### Version Info
```http
GET /api/codegen/version
```

## Supported Programming Languages

- Java
- Python
- JavaScript
- TypeScript
- C++
- C#
- Go
- Rust
- Kotlin
- Swift
- PHP
- Ruby
- Scala
- R
- SQL
- HTML
- CSS
- React
- Angular
- Vue
- Node.js
- Spring Boot

## Error Handling

The API provides consistent error responses:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "prompt": "Prompt cannot be empty",
    "language": "Programming language must be specified"
  }
}
```

## Validation Rules

- **Prompt**: Required, max 1000 characters
- **Language**: Required, max 50 characters
- **History limit**: 1-100 entries

## Database Schema

### CodeHistory Entity

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key (auto-increment) |
| user_prompt | VARCHAR(1000) | User's code generation request |
| programming_language | VARCHAR(50) | Target programming language |
| generated_code | TEXT | AI-generated code |
| created_at | TIMESTAMP | Creation timestamp |
| execution_time_ms | BIGINT | API execution time |
| success | BOOLEAN | Success/failure status |
| error_message | VARCHAR(500) | Error details (if failed) |

## Performance & Monitoring

### Caching

The application uses Spring caching for:
- All history queries
- Language-specific queries
- Statistics calculations

### Logging

Comprehensive logging is configured at multiple levels:
- **DEBUG**: Detailed request/response information
- **INFO**: General application flow
- **WARN**: Validation failures
- **ERROR**: API failures and exceptions

### Metrics

Track key performance indicators:
- Response times
- Success/failure rates
- Language usage patterns
- API call volumes

## Testing

Run the test suite:

```bash
# Unit tests
mvn test

# Integration tests
mvn test -Dtest=**/*IntegrationTest

# Test coverage report
mvn jacoco:report
```

Test coverage reports are generated in `target/site/jacoco/index.html`

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/aicodegen/backend/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data transfer objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Exception handlers
│   │   ├── repository/      # Data repositories
│   │   └── service/         # Business logic
│   └── resources/
│       └── application.properties
└── test/
    └── java/                # Test classes
```

### Adding New Features

1. **New Language Support**: Add to the supported languages list in `CodeGenerationController`
2. **Custom Validation**: Extend validation annotations in DTOs
3. **Additional Statistics**: Add new queries to `CodeHistoryRepository`
4. **API Enhancements**: Extend controllers with new endpoints

## Security Considerations

- **API Key Security**: Store Gemini API key securely (environment variables recommended)
- **Input Validation**: All inputs are validated for size and content
- **SQL Injection**: Using JPA/Hibernate with parameterized queries
- **CORS**: Configured for specific origins in production

## Production Deployment

### Environment Variables

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/aicodegen
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
export GEMINI_API_KEY=your_gemini_api_key
```

### Build Production JAR

```bash
mvn clean package -DskipTests
```

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify MySQL is running
   - Check connection string and credentials
   - Ensure database exists

2. **Gemini API Errors**
   - Validate API key
   - Check API quota limits
   - Verify internet connectivity

3. **Empty Code Generation**
   - Check prompt clarity and specificity
   - Verify language is supported
   - Review Gemini API response logs

### Logs Location

- Application logs: `logs/spring.log`
- Error logs: Check console output
- Database logs: MySQL error log

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review application logs for error details

## Changelog

### Version 1.0.0
- Initial release
- Basic code generation functionality
- History tracking and statistics
- Multi-language support
- REST API with comprehensive endpoints
