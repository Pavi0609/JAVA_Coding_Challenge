package com.hexaware.Main;

import com.hexaware.Entity.Product;
import com.hexaware.Entity.Electronics;
import com.hexaware.Entity.Clothing;
import com.hexaware.Entity.User;
import com.hexaware.Dao.OrderProcessor;
import com.hexaware.Exception.UserNotFoundException;
import com.hexaware.Exception.OrderNotFoundException;
import com.hexaware.Exception.AuthorizationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class OrderManagement {
    private static OrderProcessor orderProcessor = new OrderProcessor();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;
        
        while (running) {
            System.out.println("\n===== Order Management System =====");
            System.out.println("1. Create User");
            System.out.println("2. Create Product (Admin only)");
            System.out.println("3. Create Order");
            System.out.println("4. Cancel Order");
            System.out.println("5. Get All Products");
            System.out.println("6. Get Orders by User");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); 
            try {
                switch (choice) {
                    case 1:
                        createUser();
                        break;
                    case 2:
                        createProduct();
                        break;
                    case 3:
                        createOrder();
                        break;
                    case 4:
                        cancelOrder();
                        break;
                    case 5:
                        getAllProducts();
                        break;
                    case 6:
                        getOrdersByUser();
                        break;
                    case 7:
                        running = false;
                        System.out.println("Exiting the system. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
               } catch (UserNotFoundException | OrderNotFoundException | AuthorizationException e) {
                   System.out.println("Error: " + e.getMessage());
               }
           }
           
           scanner.close();
       }

       private static void createUser() {
           System.out.println("\n--- Create New User ---");
           System.out.print("Enter User ID: ");
           int userId = scanner.nextInt();
           scanner.nextLine();
           
           System.out.print("Enter Username: ");
           String username = scanner.nextLine();
           
           System.out.print("Enter Password: ");
           String password = scanner.nextLine();
           
           System.out.print("Enter Role (Admin/User): ");
           String role = scanner.nextLine();
           
           User user = new User(userId, username, password, role);
           orderProcessor.createUser(user);
           System.out.println("User created successfully!");
       }
       private static void createProduct() throws AuthorizationException {
           System.out.println("\n--- Create New Product ---");
           
           System.out.print("Enter Admin User ID: ");
           int userId = scanner.nextInt();
           scanner.nextLine();
           
           User adminUser = new User();
           adminUser.setUserId(userId);
           adminUser.setRole("Admin");
           
           System.out.print("Enter Product ID: ");
           int productId = scanner.nextInt();
           scanner.nextLine();
           
           System.out.print("Enter Product Name: ");
           String productName = scanner.nextLine();
           
           System.out.print("Enter Description: ");
           String description = scanner.nextLine();
           
           System.out.print("Enter Price: ");
           double price = scanner.nextDouble();
           scanner.nextLine();
           
           System.out.print("Enter Quantity in Stock: ");
           int quantity = scanner.nextInt();
           scanner.nextLine();
           System.out.print("Enter Product Type (Electronics/Clothing): ");
           String type = scanner.nextLine();
           
           Product product;
           if ("Electronics".equalsIgnoreCase(type)) {
               System.out.print("Enter Brand: ");
               String brand = scanner.nextLine();
               
               System.out.print("Enter Warranty Period (months): ");
               int warranty = scanner.nextInt();
               scanner.nextLine();
               
               product = new Electronics(productId, productName, description, price, quantity, type, brand, warranty);
           } else if ("Clothing".equalsIgnoreCase(type)) {
               System.out.print("Enter Size: ");
               String size = scanner.nextLine();
               
               System.out.print("Enter Color: ");
               String color = scanner.nextLine();
               
               product = new Clothing(productId, productName, description, price, quantity, type, size, color);
           } else {
               product = new Product(productId, productName, description, price, quantity, type);
           }
           orderProcessor.createProduct(adminUser, product);
           System.out.println("Product created successfully!");
       }

       private static void createOrder() throws UserNotFoundException {
           System.out.println("\n--- Create New Order ---");
           System.out.print("Enter User ID: ");
           int userId = scanner.nextInt();
           scanner.nextLine();
           
           User user = new User();
           user.setUserId(userId);
           
           List<Product> products = new ArrayList<>();
           boolean addingProducts = true;
           while (addingProducts) {
               System.out.print("Enter Product ID to add to order (or 0 to finish): ");
               int productId = scanner.nextInt();
               scanner.nextLine();
               
               if (productId == 0) {
                   addingProducts = false;
               } else {
                   Product product = new Product();
                   product.setProductId(productId);
                   products.add(product);
               }
           }
           
           if (!products.isEmpty()) {
               orderProcessor.createOrder(user, products);
               System.out.println("Order created successfully!");
           } else {
               System.out.println("No products added to order.");
           }
       }
       private static void cancelOrder() throws UserNotFoundException, OrderNotFoundException {
           System.out.println("\n--- Cancel Order ---");
           System.out.print("Enter User ID: ");
           int userId = scanner.nextInt();
           scanner.nextLine();
           
           System.out.print("Enter Order ID to cancel: ");
           int orderId = scanner.nextInt();
           scanner.nextLine();
           
           orderProcessor.cancelOrder(userId, orderId);
           System.out.println("Order cancelled successfully!");
       }

       private static void getAllProducts() {
           System.out.println("\n--- All Products ---");
           List<Product> products = orderProcessor.getAllProducts();
           
           if (products.isEmpty()) {
               System.out.println("No products available.");
           } else {
               for (Product product : products) {
                   System.out.println("ID: " + product.getProductId() + 
                                    ", Name: " + product.getProductName() + 
                                    ", Price: " + product.getPrice() + 
                                    ", Type: " + product.getType());
               }
           }
       }

       private static void getOrdersByUser() throws UserNotFoundException {
           System.out.println("\n--- Orders by User ---");
           System.out.print("Enter User ID: ");
           int userId = scanner.nextInt();
           scanner.nextLine();
           
           User user = new User();
           user.setUserId(userId);
           
           List<Product> products = orderProcessor.getOrderByUser(user);
           
           if (products.isEmpty()) {
               System.out.println("No orders found for this user.");
           } else {
               System.out.println("Orders for User ID " + userId + ":");
               for (Product product : products) {
                   System.out.println("Product ID: " + product.getProductId() + 
                                    ", Name: " + product.getProductName() + 
                                    ", Price: " + product.getPrice());
               }
           }
       }
   } 