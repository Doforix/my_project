package org.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://icopoghru9oezxh8.cbetxkdyhwsb.us-east-1.rds.amazonaws.com:3306/vj405xbpvcud57ju";
    private static final String USER = "c650tzngv9hbki1r";
    private static final String PASSWORD = "a0j7fxgnki38y8b6";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
