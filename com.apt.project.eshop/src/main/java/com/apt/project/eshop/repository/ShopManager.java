package com.apt.project.eshop.repository;

import java.util.List;

import com.apt.project.eshop.model.Product;
import com.mongodb.MongoException;

public class ShopManager {

	private TransactionManager transactionManager;

	public ShopManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	public void checkout() {
		transactionManager.doInTransaction(
			productRepository -> {
				List<Product> products = productRepository.allCart();
				try {
					for (Product product : products) {
						productRepository.removeFromStorage(product);
						productRepository.removeFromCart(product);
					} 
				} catch (RepositoryException e) {
					throw new MongoException("Insufficient stock");
				}
				return null;
			}		
		);
	}
	
	
	
}
