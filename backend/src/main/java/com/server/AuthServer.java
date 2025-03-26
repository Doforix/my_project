package com.server;

import org.database.DatabaseConnection;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.Base64;

public class AuthServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/admin", new AdminHandler());
        server.setExecutor(null); // Использование стандартного потока
        server.start();
        System.out.println("Server started on port 8080");
    }

    // Обработчик для регистрации пользователей
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Please use POST request to register.";
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line = bufferedReader.readLine();
                    String[] params = line.split("&");
                    String username = params[0].split("=")[1];
                    String password = params[1].split("=")[1];
                    String email = params[2].split("=")[1];

                    String hashedPassword = hashPassword(password);

                    try (Connection connection = DatabaseConnection.getConnection()) {
                        String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
                        try (PreparedStatement statement = connection.prepareStatement(query)) {
                            statement.setString(1, username);
                            statement.setString(2, hashedPassword);
                            statement.setString(3, email);
                            int result = statement.executeUpdate();
                            if (result > 0) {
                                response = "User registered successfully!";
                            } else {
                                response = "Error registering user.";
                            }
                        }
                    }
                } catch (SQLException ex) {
                    response = "Database error.";
                    ex.printStackTrace();
                }
            }
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // Обработчик для авторизации
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Invalid request.";
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                String[] params = line.split("&");
                String username = params[0].split("=")[1];
                String password = params[1].split("=")[1];

                try (Connection connection = DatabaseConnection.getConnection()) {
                    String query = "SELECT * FROM users WHERE username = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setString(1, username);
                        ResultSet resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            String storedPassword = resultSet.getString("password");
                            if (verifyPassword(password, storedPassword)) {
                                response = "Login successful!";
                            } else {
                                response = "Invalid credentials.";
                            }
                        } else {
                            response = "User not found.";
                        }
                    }
                } catch (SQLException ex) {
                    response = "Database error.";
                    ex.printStackTrace();
                }
            }
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // Админ-обработчик
    static class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Admin section";
            if ("POST".equals(exchange.getRequestMethod())) {
                // Получить список пользователей или что-то еще, что нужно для админки
                response = "Fetching admin data";
            }

            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // Хэширование пароля
    private static String hashPassword(String password) {
        // Для примера просто возвращаем пароль
        return password;
    }

    // Проверка пароля
    private static boolean verifyPassword(String inputPassword, String storedPassword) {
        return inputPassword.equals(storedPassword); // Сравнение пароля
    }
}
