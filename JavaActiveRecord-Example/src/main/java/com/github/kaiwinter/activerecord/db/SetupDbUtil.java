package com.github.kaiwinter.activerecord.db;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetupDbUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetupDbUtil.class.getSimpleName());

    /**
     * Sets up the in-memory test database.
     */
    public static void setupDb() throws SQLException, IOException, ClassNotFoundException {
        LOGGER.debug("setup test db");

        Properties properties = new Properties();
        properties.load(SetupDbUtil.class.getResourceAsStream("db.properties"));
        Class.forName(properties.getProperty("driverClass"));
        try (Statement statement = Db.INSTANCE.getConnection().createStatement()) {
            statement.execute("DROP TABLE IF EXISTS person");
            statement.execute("DROP TABLE IF EXISTS mountain");
            statement.execute("DROP TABLE IF EXISTS person_with_db_sequence");
            statement.execute("CREATE TABLE person (id INTEGER, name VARCHAR, surname VARCHAR)");
            statement.execute("CREATE TABLE mountain (id INTEGER, name VARCHAR, height INTEGER)");
            statement.execute(
                    "CREATE TABLE person_with_db_sequence (id INTEGER PRIMARY KEY, name VARCHAR, surname VARCHAR)");
            LOGGER.debug("done");
        }
    }

}
