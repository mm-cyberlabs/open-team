module open.team.app {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    
    // Database modules
    requires java.sql;
    requires java.naming;
    requires com.zaxxer.hikari;
    requires org.postgresql.jdbc;
    
    // Jackson YAML modules
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.datatype.jsr310;
    
    // Logging modules
    requires org.slf4j;
    requires ch.qos.logback.classic;
    
    // Java desktop for opening URLs
    requires java.desktop;
    
    // Validation modules
    requires jakarta.validation;
    requires org.hibernate.validator;
    
    // Open packages for JavaFX FXML
    opens com.openteam.controller to javafx.fxml;
    opens com.openteam.model to javafx.base, com.fasterxml.jackson.databind;
    opens com.openteam to javafx.fxml;
    
    // Export main package
    exports com.openteam;
}