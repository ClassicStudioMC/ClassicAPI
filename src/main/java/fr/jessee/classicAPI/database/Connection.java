package fr.jessee.classicAPI.database;

import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class Connection implements AutoCloseable {
    private final Credentials CREDENTIALS;
    private java.sql.Connection connection;

    public Connection(@NotNull final Credentials CREDENTIALS) {
        this.CREDENTIALS = CREDENTIALS;
        this.connect();
    }

    private void connect() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                    this.CREDENTIALS.toURI(),
                    this.CREDENTIALS.getUSER(),
                    this.CREDENTIALS.getPASS()
            );
        } catch (SQLException | ClassNotFoundException error) {
            throw new RuntimeException(error);
        }


    }

    public void disconnect() {
        try {
            if (this.connection != null) {
                if (!this.connection.isClosed()) {
                    this.connection.close();
                }
            }
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    public java.sql.Connection getConnection() throws SQLException {
        if (this.connection != null) {
            if (!this.connection.isClosed()) {
                return this.connection;
            }
        }
        connect();
        return this.connection;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
