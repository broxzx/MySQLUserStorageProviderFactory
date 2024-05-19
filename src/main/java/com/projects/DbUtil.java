//package com.projects;
//
//import org.keycloak.component.ComponentModel;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.sql.Connection;
//import java.sql.DatabaseMetaData;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class DbUtil {
//
//    private static final Logger logger = LoggerFactory.getLogger(DbUtil.class);
//
//    public static final String CONFIG_KEY_JDBC_DRIVER = "jdbcDriver";
//    public static final String CONFIG_KEY_JDBC_URL = "jdbcUrl";
//    public static final String CONFIG_KEY_DB_USERNAME = "username";
//    public static final String CONFIG_KEY_DB_PASSWORD = "password";
//
//    public static Connection getConnection(ComponentModel config) throws SQLException {
//        String driverClass = config.get(CONFIG_KEY_JDBC_DRIVER);
//
//        logger.info("Attempting to load JDBC driver: {}", driverClass);
//
//        try {
//            Class.forName(driverClass);
//        } catch (ClassNotFoundException nfe) {
//            logger.error("Invalid JDBC driver: {}. Please check if your driver is properly installed", driverClass, nfe);
//            throw new RuntimeException("Invalid JDBC driver: " + driverClass + ". Please check if your driver is properly installed", nfe);
//        }
//
//        String jdbcUrl = config.get(CONFIG_KEY_JDBC_URL);
//        String username = config.get(CONFIG_KEY_DB_USERNAME);
//        String password = config.get(CONFIG_KEY_DB_PASSWORD);
//
//        logger.info("Connecting to database at URL: {}", jdbcUrl);
//        logger.debug("Using username: {}", username);
//
//        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
//
//        logger.info("Successfully connected to the database");
//
//        return connection;
//    }
//}
