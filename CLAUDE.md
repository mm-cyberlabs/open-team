# Open Team Communication App

## Project Overview

A JavaFX desktop application for team communication and software deployment tracking. The application provides a futuristic interface with real-time updates for team announcements, activities, and deployment information.

## Technology Stack

- **Java**: 21 (LTS)
- **UI Framework**: JavaFX 21
- **Database**: PostgreSQL 15+
- **Build Tool**: Maven 3.9+
- **Configuration**: YAML-based local configuration
- **Architecture**: MVC Pattern with Repository Layer

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/openteam/
│   │       ├── OpenTeamApplication.java          # Main application entry point
│   │       ├── config/                           # Configuration management
│   │       │   ├── DatabaseConfig.java
│   │       │   └── YamlConfigLoader.java
│   │       ├── controller/                       # JavaFX Controllers
│   │       │   ├── MainController.java
│   │       │   ├── AnnouncementController.java
│   │       │   ├── ActivityController.java
│   │       │   └── DeploymentController.java
│   │       ├── model/                           # Entity classes
│   │       │   ├── Announcement.java
│   │       │   ├── Activity.java
│   │       │   ├── Deployment.java
│   │       │   └── User.java
│   │       ├── repository/                      # Data access layer
│   │       │   ├── DatabaseConnection.java
│   │       │   ├── AnnouncementRepository.java
│   │       │   ├── ActivityRepository.java
│   │       │   └── DeploymentRepository.java
│   │       ├── service/                         # Business logic
│   │       │   ├── AnnouncementService.java
│   │       │   ├── ActivityService.java
│   │       │   └── DeploymentService.java
│   │       └── util/                            # Utility classes
│   │           ├── DateTimeUtil.java
│   │           └── UIUtils.java
│   └── resources/
│       ├── fxml/                               # JavaFX FXML files
│       │   ├── main-view.fxml
│       │   ├── announcement-view.fxml
│       │   ├── activity-view.fxml
│       │   └── deployment-view.fxml
│       ├── css/                                # Stylesheets
│       │   └── futuristic-theme.css
│       └── icons/                              # Application icons
└── test/
    └── java/
        └── com/openteam/                       # Unit tests
```

## Database Configuration

The application reads database configuration from a YAML file located at:
- **Windows**: `%USERPROFILE%/.openteam/config.yml`
- **macOS/Linux**: `~/.openteam/config.yml`

### Configuration File Format (config.yml)

```yaml
database:
  url: jdbc:postgresql://localhost:5432/openteam
  username: openteam_user
  password: your_secure_password
  driver: org.postgresql.Driver

application:
  refreshInterval: 10 # seconds
```

## Database Schema

### PostgreSQL Setup Scripts

Create the following SQL files in `src/main/resources/sql/`:

#### 1. `01-create-database.sql`
```sql
-- Create database and user
CREATE DATABASE openteam;
CREATE USER openteam_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE openteam TO openteam_user;
```

#### 2. `02-create-schema.sql`
```sql
-- Use openteam database
\c openteam;

-- Create schema
CREATE SCHEMA IF NOT EXISTS team_comm;
GRANT ALL ON SCHEMA team_comm TO openteam_user;
```

#### 3. `03-create-tables.sql`
```sql
-- Set search path
SET search_path TO team_comm;

-- Users table for tracking who updates records
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Team announcements
CREATE TABLE announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT true
);

-- Team activities
CREATE TABLE activities (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    activity_type VARCHAR(50) DEFAULT 'GENERAL' CHECK (activity_type IN ('MEETING', 'TRAINING', 'EVENT', 'GENERAL')),
    scheduled_date TIMESTAMP WITH TIME ZONE,
    location VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT true
);

-- Software production deployments
CREATE TABLE deployments (
    id BIGSERIAL PRIMARY KEY,
    release_name VARCHAR(100) NOT NULL,
    version VARCHAR(50) NOT NULL,
    deployment_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    driver_user_id BIGINT REFERENCES users(id),
    release_notes TEXT,
    environment VARCHAR(20) DEFAULT 'PRODUCTION' CHECK (environment IN ('DEV', 'STAGING', 'PRODUCTION')),
    status VARCHAR(20) DEFAULT 'PLANNED' CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'ROLLED_BACK')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX idx_announcements_created_at ON announcements(created_at DESC);
CREATE INDEX idx_announcements_priority ON announcements(priority);
CREATE INDEX idx_activities_scheduled_date ON activities(scheduled_date);
CREATE INDEX idx_deployments_datetime ON deployments(deployment_datetime DESC);
CREATE INDEX idx_deployments_status ON deployments(status);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to all tables
CREATE TRIGGER update_announcements_updated_at BEFORE UPDATE ON announcements
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_activities_updated_at BEFORE UPDATE ON activities
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_deployments_updated_at BEFORE UPDATE ON deployments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

#### 4. `04-sample-data.sql`
```sql
SET search_path TO team_comm;

-- Insert sample users
INSERT INTO users (username, full_name, email) VALUES
('admin', 'System Administrator', 'admin@company.com'),
('jdoe', 'John Doe', 'john.doe@company.com'),
('msmith', 'Mary Smith', 'mary.smith@company.com'),
('rjohnson', 'Robert Johnson', 'robert.johnson@company.com');

-- Insert sample announcements
INSERT INTO announcements (title, content, priority, created_by, updated_by) VALUES
('System Maintenance', 'Scheduled maintenance this weekend', 'HIGH', 1, 1),
('New Security Policy', 'Please review the updated security guidelines', 'NORMAL', 1, 1);

-- Insert sample activities
INSERT INTO activities (title, description, activity_type, scheduled_date, location, created_by, updated_by) VALUES
('Sprint Planning', 'Planning for next development sprint', 'MEETING', CURRENT_TIMESTAMP + INTERVAL '1 day', 'Conference Room A', 2, 2),
('Security Training', 'Mandatory security awareness training', 'TRAINING', CURRENT_TIMESTAMP + INTERVAL '3 days', 'Training Room', 1, 1);

-- Insert sample deployments
INSERT INTO deployments (release_name, version, deployment_datetime, driver_user_id, release_notes, environment, status, created_by, updated_by) VALUES
('Customer Portal', 'v2.1.0', CURRENT_TIMESTAMP + INTERVAL '2 days', 3, 'Bug fixes and performance improvements', 'PRODUCTION', 'PLANNED', 2, 2),
('API Gateway', 'v1.5.2', CURRENT_TIMESTAMP - INTERVAL '1 day', 4, 'Security patches and new endpoints', 'PRODUCTION', 'COMPLETED', 3, 3);
```

## Entity Models

### Core Requirements for Entity Classes

1. **All entities must include audit fields**:
    - `createdAt`: Timestamp of creation
    - `updatedAt`: Timestamp of last update
    - `createdBy`: User who created the record
    - `updatedBy`: User who last updated the record

2. **Keep classes under 250 lines**
3. **Use Java records for immutable data transfer objects**
4. **Include validation annotations**

### Example Entity Structure

```java
@Entity
@Table(name = "announcements", schema = "team_comm")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    private Boolean isActive;
    
    // Constructors, getters, setters, equals, hashCode
}
```

## JavaFX UI Requirements

### Futuristic Theme Specifications

1. **Color Scheme**:
    - Primary: Dark blue/navy (#1a1a2e)
    - Secondary: Electric blue (#16213e)
    - Accent: Cyan (#0f3460)
    - Text: Light gray (#e94560)
    - Success: Neon green (#00ff85)
    - Warning: Orange (#ff6b6b)

2. **Navigation Menu**:
    - Left sidebar with animated hover effects
    - Icon-based navigation with labels
    - Active state highlighting

3. **Update Button Behavior**:
    - Each view must have a prominent "Update" button
    - Button should show loading state during refresh
    - Update timestamp display after successful refresh

### FXML Structure Requirements

```xml
<!-- Example structure for main-view.fxml -->
<BorderPane xmlns="http://javafx.com/javafx/11.0.1" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.openteam.controller.MainController">
   <left>
      <VBox styleClass="navigation-menu">
         <!-- Navigation items -->
      </VBox>
   </left>
   <center>
      <StackPane fx:id="contentArea">
         <!-- Dynamic content area -->
      </StackPane>
   </center>
</BorderPane>
```

## Build Configuration

### Maven Dependencies (pom.xml excerpt)

```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <javafx.version>21</javafx.version>
    <postgresql.version>42.7.1</postgresql.version>
    <jackson.version>2.16.1</jackson.version>
</properties>

<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>${javafx.version}</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>${javafx.version}</version>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>${postgresql.version}</version>
    </dependency>
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
        <version>5.1.0</version>
    </dependency>
    
    <!-- YAML Configuration -->
    <dependency>
        <groupId>com.fasterxml.jackson.dataformat</groupId>
        <artifactId>jackson-dataformat-yaml</artifactId>
        <version>${jackson.version}</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
        <version>${jackson.version}</version>
    </dependency>
</dependencies>
```

## Development Guidelines

### Code Quality Standards

1. **Class Size**: Maximum 250 lines per class
2. **Method Size**: Maximum 30 lines per method
3. **Dependency Injection**: Use constructor injection
4. **Error Handling**: Comprehensive try-catch blocks with logging
5. **Testing**: Minimum 80% code coverage

### Repository Pattern Implementation

```java
public interface AnnouncementRepository {
    List<Announcement> findAllActive();
    Optional<Announcement> findById(Long id);
    Announcement save(Announcement announcement);
    void deleteById(Long id);
    List<Announcement> findByPriority(Priority priority);
}
```

### Service Layer Pattern

```java
@Service
public class AnnouncementService {
    private final AnnouncementRepository repository;
    
    public List<Announcement> getAllActiveAnnouncements() {
        return repository.findAllActive();
    }
    
    public Announcement updateAnnouncement(Long id, String title, String content, Priority priority, User updatedBy) {
        // Business logic here
    }
}
```

## Configuration Management

### YamlConfigLoader Implementation

```java
public class YamlConfigLoader {
    private static final String CONFIG_DIR = ".openteam";
    private static final String CONFIG_FILE = "config.yml";
    
    public static DatabaseConfig loadDatabaseConfig() {
        Path configPath = getConfigPath();
        // Implementation for loading YAML configuration
    }
    
    private static Path getConfigPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR, CONFIG_FILE);
    }
}
```

## Deployment Instructions

1. **Database Setup**:
   ```bash
   # Run SQL scripts in order
   psql -U postgres -f src/main/resources/sql/01-create-database.sql
   psql -U postgres -d openteam -f src/main/resources/sql/02-create-schema.sql
   psql -U postgres -d openteam -f src/main/resources/sql/03-create-tables.sql
   psql -U postgres -d openteam -f src/main/resources/sql/04-sample-data.sql
   ```

2. **Configuration Setup**:
    - Create `~/.openteam/config.yml` with database credentials
    - Ensure PostgreSQL is running and accessible

3. **Build and Run**:
   ```bash
   mvn clean compile
   mvn javafx:run
   ```

## Features to Implement

### Phase 1 (Core Features)
- [ ] Database connection and configuration loading
- [ ] Main application window with navigation
- [ ] Team Announcements CRUD operations
- [ ] Team Activities CRUD operations
- [ ] Software Deployment tracking
- [ ] Update button functionality with audit logging

### Phase 2 (Enhanced Features)
- [ ] Real-time refresh capabilities
- [ ] Search and filtering functionality
- [ ] Export functionality (PDF/Excel)
- [ ] User preferences and settings
- [ ] Data validation and error handling

### Phase 3 (Advanced Features)
- [ ] Notification system
- [ ] Dashboard with statistics
- [ ] Backup and restore functionality
- [ ] Multi-user concurrent access handling

## Security Considerations

1. **Database Security**:
    - Use connection pooling with HikariCP
    - Parameterized queries to prevent SQL injection
    - Database credentials in encrypted configuration

2. **Application Security**:
    - Input validation on all user inputs
    - Audit trail for all data modifications
    - Session management for user tracking

## Performance Requirements

1. **Database Performance**:
    - Use indexes on frequently queried columns
    - Implement connection pooling
    - Lazy loading for related entities

2. **UI Performance**:
    - Asynchronous data loading
    - Pagination for large datasets
    - Efficient table rendering with virtual flows

## Testing Strategy

1. **Unit Tests**: Repository and Service layers
2. **Integration Tests**: Database connectivity and CRUD operations
3. **UI Tests**: JavaFX controller testing with TestFX
4. **Performance Tests**: Database query performance and UI responsiveness

This documentation provides a comprehensive foundation for building the Open Team Communication App with Claude Code assistance.