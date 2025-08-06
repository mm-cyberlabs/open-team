# Open Team Communication App

A JavaFX desktop application for team communication and software deployment tracking with a futuristic interface and real-time updates.

## Features

- **Team Announcements**: Create, view, and manage team announcements with priority levels
- **Activity Scheduling**: Track team activities, meetings, training sessions, and events
- **Deployment Monitoring**: Monitor software deployments with status tracking and release notes
- **Futuristic UI**: Dark theme with neon accents and smooth animations
- **Real-time Updates**: Refresh data with audit logging and timestamp tracking
- **Database Integration**: PostgreSQL backend with connection pooling

## Technology Stack

- **Java 21** (LTS)
- **JavaFX 21** for desktop UI
- **PostgreSQL 15+** for data storage
- **Maven 3.9+** for build management
- **HikariCP** for connection pooling
- **Jackson** for YAML configuration
- **SLF4J + Logback** for logging

## Prerequisites

1. **Java 21 or higher** installed and configured
2. **PostgreSQL 15+** installed and running
3. **Maven 3.9+** for building the application

## Database Setup

1. Start PostgreSQL service
2. Run the SQL scripts in order:
   ```bash
   psql -U postgres -f src/main/resources/sql/01-create-database.sql
   psql -U postgres -d openteam -f src/main/resources/sql/02-create-schema.sql
   psql -U postgres -d openteam -f src/main/resources/sql/03-create-tables.sql
   psql -U postgres -d openteam -f src/main/resources/sql/04-sample-data.sql
   ```

## Configuration

The application reads configuration from `~/.openteam/config.yml`. A default configuration file will be created automatically on first run.

### Example Configuration

```yaml
database:
  url: jdbc:postgresql://localhost:5432/openteam
  username: openteam_user
  password: your_secure_password
  driver: org.postgresql.Driver

application:
  refreshInterval: 10
```

## Building and Running

### Option 1: Using Maven (Development)

```bash
# Compile the application
mvn clean compile

# Run with JavaFX plugin
mvn javafx:run

# Or build and run JAR
mvn clean package
java -jar target/open-team-app-1.0.0.jar
```

### Option 2: Using the Run Script

```bash
# Make script executable (first time only)
chmod +x run-app.sh

# Run the application
./run-app.sh
```

### Option 3: Direct JAR Execution

```bash
# After building with mvn clean package
java -jar target/open-team-app-1.0.0.jar
```

## Packaging for Distribution (macOS)

### Create Portable macOS App (Recommended)

Create a self-contained `.app` bundle with embedded Java runtime:

```bash
# Creates OpenTeam.app with embedded JRE - works on any Mac!
./create-portable-macos-app.sh
```

**Benefits:**
- ✅ No Java installation required on target machines
- ✅ Works on any macOS 10.15+ system  
- ✅ Professional "double-click to run" experience
- ✅ Only requires user configuration file

**End users just need:**
1. Download and copy `OpenTeam.app` to Applications
2. Create `~/.openteam/config.yml`
3. Run the app

See [PACKAGING.md](PACKAGING.md) for detailed packaging instructions.

## Project Structure

```
src/
├── main/
│   ├── java/com/openteam/
│   │   ├── OpenTeamApplication.java     # Main application entry point
│   │   ├── config/                      # Configuration management
│   │   ├── controller/                  # JavaFX Controllers
│   │   ├── model/                       # Entity classes
│   │   ├── repository/                  # Data access layer
│   │   ├── service/                     # Business logic
│   │   └── util/                        # Utility classes
│   └── resources/
│       ├── fxml/                        # JavaFX FXML files
│       ├── css/                         # Stylesheets
│       └── sql/                         # Database scripts
```

## Usage

1. **Launch the application** using one of the methods above
2. **Navigate** between sections using the left sidebar:
   - 📢 Announcements
   - 🗓️ Activities  
   - 🚀 Deployments
3. **View data** in tables with sorting and filtering
4. **Refresh data** using the Update button
5. **Filter content** using the dropdown filters
6. **View details** by selecting rows in the tables

## Database Schema

The application uses three main entities:

- **Users**: Team members with audit tracking
- **Announcements**: Team announcements with priority levels
- **Activities**: Scheduled team activities and events
- **Deployments**: Software deployment tracking with status

## Troubleshooting

### Database Connection Issues

1. Verify PostgreSQL is running: `pg_ctl status`
2. Check database configuration in `~/.openteam/config.yml`
3. Ensure database and user exist
4. Test connection: `psql -U openteam_user -d openteam`

### JavaFX Runtime Issues

1. Ensure Java 21+ is installed: `java -version`
2. On some systems, you may need JavaFX runtime separately
3. The shaded JAR includes all JavaFX dependencies

### Build Issues

1. Clean and rebuild: `mvn clean package`
2. Check Maven version: `mvn -version`
3. Ensure JAVA_HOME points to Java 21+

## Development

### Code Guidelines

- Classes should not exceed 200 lines
- Methods should not exceed 30 lines  
- Use constructor injection for dependencies
- Include comprehensive error handling
- Follow the existing code style and patterns

### Testing

```bash
# Run unit tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## License

This project is open source. See LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes following the code guidelines
4. Test thoroughly
5. Submit a pull request

## Support

For issues and questions:
- Check the troubleshooting section above
- Review the database setup instructions
- Ensure all prerequisites are met
- Check application logs for detailed error messages