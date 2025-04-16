package com.hexaware.Dao;

import com.hexaware.Entity.Product;
import com.hexaware.Entity.User;
import com.hexaware.Exception.UserNotFoundException;
import com.hexaware.Exception.OrderNotFoundException;
import com.hexaware.Exception.AuthorizationException;
import java.util.List;

public interface IOrderManagementRepository {
	
    void createOrder(User user, List<Product> products) throws UserNotFoundException;
    
    void cancelOrder(int userId, int orderId) throws UserNotFoundException, OrderNotFoundException;
    
    void createProduct(User user, Product product) throws AuthorizationException;
    
    void createUser(User user);
    
    List<Product> getAllProducts();
    
    List<Product> getOrderByUser(User user) throws UserNotFoundException;
    
}
