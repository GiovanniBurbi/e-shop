package com.apt.project.eshop.repository;

import java.util.List;

import com.apt.project.eshop.model.Product;

public class TransactionalShopManager {

	private TransactionManager transactionManager;

	public TransactionalShopManager(TransactionManager transactionManager) {
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
