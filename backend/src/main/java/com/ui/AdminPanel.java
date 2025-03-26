package com.ui;

import org.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminPanel extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;

    public AdminPanel() {
        setTitle("Admin Panel");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Создаем таблицу для отображения данных
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);

        // Скроллинг для таблицы
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок для действий
        JPanel panel = new JPanel();

        // Кнопка для загрузки данных
        JButton loadButton = new JButton("Загрузить данные");
        loadButton.addActionListener(e -> loadData());
        panel.add(loadButton);

        // Кнопка для добавления записи
        JButton addButton = new JButton("Добавить запись");
        addButton.addActionListener(e -> addRecord());
        panel.add(addButton);

        // Кнопка для удаления записи
        JButton deleteButton = new JButton("Удалить запись");
        deleteButton.addActionListener(e -> deleteRecord());
        panel.add(deleteButton);

        // Кнопка для редактирования записи
        JButton editButton = new JButton("Редактировать запись");
        editButton.addActionListener(e -> editRecord());
        panel.add(editButton);

        add(panel, BorderLayout.SOUTH);
    }

    // Метод для загрузки данных из базы данных
    private void loadData() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");

            // Очистка предыдущих данных
            tableModel.setRowCount(0);

            // Получаем метаданные для таблицы
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Добавляем столбцы в модель таблицы
            for (int i = 1; i <= columnCount; i++) {
                tableModel.addColumn(metaData.getColumnName(i));
            }

            // Добавляем строки данных в таблицу, исключая пустые значения
            while (resultSet.next()) {
                if (resultSet.getObject(1) != null) {  // Проверяем, чтобы в первой колонке не было null значений
                    List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.add(resultSet.getObject(i));
                    }
                    tableModel.addRow(row.toArray());
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка подключения к базе данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Метод для добавления записи в базу данных
    private void addRecord() {
        String name = JOptionPane.showInputDialog("Введите имя:");
        String email = JOptionPane.showInputDialog("Введите email:");

        if (name != null && email != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO users (name, email) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, name);
                    statement.setString(2, email);
                    statement.executeUpdate();
                    loadData(); // Перезагружаем данные в таблице
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка добавления записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    // Метод для удаления записи из базы данных
    private void deleteRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int userId = (int) table.getValueAt(selectedRow, 0); // Предполагаем, что ID в первой колонке

            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM users WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, userId);
                    statement.executeUpdate();
                    loadData(); // Перезагружаем данные в таблице
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка удаления записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите запись для удаления.", "Ошибка", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Метод для редактирования записи в базе данных
    private void editRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int userId = (int) table.getValueAt(selectedRow, 0); // Предполагаем, что ID в первой колонке

            String newName = JOptionPane.showInputDialog("Введите новое имя:", table.getValueAt(selectedRow, 1));
            String newEmail = JOptionPane.showInputDialog("Введите новый email:", table.getValueAt(selectedRow, 2));

            if (newName != null && newEmail != null) {
                try (Connection connection = DatabaseConnection.getConnection()) {
                    String query = "UPDATE users SET name = ?, email = ? WHERE id = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setString(1, newName);
                        statement.setString(2, newEmail);
                        statement.setInt(3, userId);
                        statement.executeUpdate();
                        loadData(); // Перезагружаем данные в таблице
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка редактирования записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите запись для редактирования.", "Ошибка", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminPanel panel = new AdminPanel();
            panel.setVisible(true);
        });
    }
}
