package com.apt.project.eshop.repository;

import java.util.List;

import com.apt.project.eshop.model.Product;

public class ShopManager {

	private TransactionManager transactionManager;

	public ShopManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	public void checkout() {
		transactionManager.doInTransaction(
			productRepository -> {
				List<Product> products = productRepository.allCart();
				for (Product product : products) {
					productRepository.removeFromCart(product);
					productRepository.removeFromStorage(product);
				}
				return null;
			}		
		);
	}
	
	
	
}