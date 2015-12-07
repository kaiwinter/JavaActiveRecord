package com.github.kaiwinter.activerecord.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import com.github.kaiwinter.activerecord.BaseAR;

/**
 * Singleton for accessing the database.
 */
public enum Db {
    INSTANCE;

    private static final String PROPERTIES_FILE = "db.properties";
    private static final String PROPERTY_DRIVERCLASS = "driverClass";
    private static final String PROPERTY_CONNECTIONSTRING = "connectionString";

    private Connection connection;

    private InternalSequenceGenerator sequenceGenerator;

    private Db() {
        try {
            LoggerFactory.getLogger(Db.class.getSimpleName()).debug("Initializing DB Connection");

            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream(PROPERTIES_FILE));
            Class.forName(properties.getProperty(PROPERTY_DRIVERCLASS));
            connection = DriverManager.getConnection(properties.getProperty(PROPERTY_CONNECTIONSTRING));
            sequenceGenerator = new InternalSequenceGenerator();
        } catch (ClassNotFoundException | SQLException | IOException e) {
            LoggerFactory.getLogger(Db.class.getSimpleName()).error(e.getMessage(), e);
        }
    }

    /**
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Returns the next available sequence number from the internal sequence generator.
     * 
     * @param clazz
     *            the Active Record class which identifies the database table
     * @return the next available sequence number
     * @throws SQLException
     *             when the current sequence number cannot be loaded from the database
     */
    public long getNextSequenceNumber(Class<? extends BaseAR> clazz) throws SQLException {
        return sequenceGenerator.getNextSequenceNumber(clazz);
    }
}
