package com.hexaware.Dao;

import com.hexaware.Entity.Clothing;
import com.hexaware.Entity.Electronics;
import com.hexaware.Entity.Product;
import com.hexaware.Entity.User;
import com.hexaware.Exception.UserNotFoundException;
import com.hexaware.Exception.OrderNotFoundException;
import com.hexaware.Exception.AuthorizationException;
import com.hexaware.Util.DBConnUtil;
import com.hexaware.Util.DBPropertyUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
// import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor implements IOrderManagementRepository {
    private Connection connection;

    public OrderProcessor() {
        try {
            String connectionString = DBPropertyUtil.getConnectionString("config.properties");
            this.connection = DBConnUtil.getDBConnection(connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void createOrder(User user, List<Product> products) throws UserNotFoundException {
        try {
            // Check if user exists
            if (!isUserExists(user.getUserId())) {
                throw new UserNotFoundException("User with ID " + user.getUserId() + " not found.");
            }

            // Create order
            String orderQuery = "INSERT INTO Orders (userId, orderDate) VALUES (?, CURRENT_TIMESTAMP)";
            PreparedStatement orderStmt = connection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, user.getUserId());
            orderStmt.executeUpdate();

            ResultSet rs = orderStmt.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }

            // Add order items
            String orderItemQuery = "INSERT INTO OrderItem (orderId, productId, quantity) VALUES (?, ?, 1)";
            PreparedStatement orderItemStmt = connection.prepareStatement(orderItemQuery);
            
            for (Product product : products) {
                orderItemStmt.setInt(1, orderId);
                orderItemStmt.setInt(2, product.getProductId());
                orderItemStmt.addBatch();
            }
            
            orderItemStmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancelOrder(int userId, int orderId) throws UserNotFoundException, OrderNotFoundException {
        try {
            // Check if user exists
            if (!isUserExists(userId)) {
                throw new UserNotFoundException("User with ID " + userId + " not found.");
            }

            // Check if order exists and belongs to user
            String checkOrderQuery = "SELECT 1 FROM Orders WHERE orderId = ? AND userId = ?";
            PreparedStatement checkOrderStmt = connection.prepareStatement(checkOrderQuery);
            checkOrderStmt.setInt(1, orderId);
            checkOrderStmt.setInt(2, userId);
            ResultSet rs = checkOrderStmt.executeQuery();
            
            if (!rs.next()) {
                throw new OrderNotFoundException("Order with ID " + orderId + " not found for user " + userId);
            }

            // Delete order items
            String deleteItemsQuery = "DELETE FROM OrderItem WHERE orderId = ?";
            PreparedStatement deleteItemsStmt = connection.prepareStatement(deleteItemsQuery);
            deleteItemsStmt.setInt(1, orderId);
            deleteItemsStmt.executeUpdate();

            // Delete order
            String deleteOrderQuery = "DELETE FROM Orders WHERE orderId = ?";
            PreparedStatement deleteOrderStmt = connection.prepareStatement(deleteOrderQuery);
            deleteOrderStmt.setInt(1, orderId);
            deleteOrderStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createProduct(User user, Product product) throws AuthorizationException {
        try {
            // Check if user is admin
            if (!"Admin".equalsIgnoreCase(user.getRole())) {
                throw new AuthorizationException("Only admin users can create products.");
            }

            String query = "INSERT INTO Product (productId, productName, description, price, quantityInStock, type) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, product.getProductId());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getDescription());
            stmt.setDouble(4, product.getPrice());
            stmt.setInt(5, product.getQuantityInStock());
            stmt.setString(6, product.getType());
            stmt.executeUpdate();

            // Insert into specific product type table
            if ("Electronics".equalsIgnoreCase(product.getType())) {
                Electronics electronics = (Electronics) product;
                String electronicsQuery = "INSERT INTO Electronics (productId, brand, warrantyPeriod) VALUES (?, ?, ?)";
                PreparedStatement electronicsStmt = connection.prepareStatement(electronicsQuery);
                electronicsStmt.setInt(1, electronics.getProductId());
                electronicsStmt.setString(2, electronics.getBrand());
                electronicsStmt.setInt(3, electronics.getWarrantyPeriod());
                electronicsStmt.executeUpdate();
            } else if ("Clothing".equalsIgnoreCase(product.getType())) {
                Clothing clothing = (Clothing) product;
                String clothingQuery = "INSERT INTO Clothing (productId, size, color) VALUES (?, ?, ?)";
                PreparedStatement clothingStmt = connection.prepareStatement(clothingQuery);
                clothingStmt.setInt(1, clothing.getProductId());
                clothingStmt.setString(2, clothing.getSize());
                clothingStmt.setString(3, clothing.getColor());
                clothingStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void createUser(User user) {
        try {
            String query = "INSERT INTO User (userId, username, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, user.getUserId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try {
            String query = "SELECT * FROM Product";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("productId"));
                product.setProductName(rs.getString("productName"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setQuantityInStock(rs.getInt("quantityInStock"));
                product.setType(rs.getString("type"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    @Override
    public List<Product> getOrderByUser(User user) throws UserNotFoundException {
        List<Product> products = new ArrayList<>();
        try {
            // Check if user exists
            if (!isUserExists(user.getUserId())) {
                throw new UserNotFoundException("User with ID " + user.getUserId() + " not found.");
            }

            String query = "SELECT p.* FROM Product p " +
                          "JOIN OrderItem oi ON p.productId = oi.productId " +
                          "JOIN Orders o ON oi.orderId = o.orderId " +
                          "WHERE o.userId = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("productId"));
                product.setProductName(rs.getString("productName"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setQuantityInStock(rs.getInt("quantityInStock"));
                product.setType(rs.getString("type"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    private boolean isUserExists(int userId) throws SQLException {
        String query = "SELECT 1 FROM User WHERE userId = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }
}
