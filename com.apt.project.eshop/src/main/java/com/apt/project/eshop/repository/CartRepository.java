package com.apt.project.eshop.repository;

import java.util.List;

import com.apt.project.eshop.model.CartItem;
import com.apt.project.eshop.model.Product;

public interface CartRepository {

	void addToCart(Product product);

	List<CartItem> allCart();

	void removeFromCart(Product product);

	double cartTotalCost();
	
	

}
