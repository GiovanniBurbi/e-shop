package com.apt.project.eshop.repository;

import java.util.List;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.model.Product;
import com.mongodb.MongoException;

public class ShopManager {

	private TransactionManager transactionManager;
	
	private EShopController shopController;

	public ShopManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void checkout() {
		transactionManager.doInTransaction(productRepository -> {
			List<Product> products = shopController.allCartProducts();
			try {
				for (Product product : products) {
					productRepository.removeFromStorage(product);
				}
			} catch (RepositoryException e) {
				shopController.checkoutFailure(e.getProduct());
				throw new MongoException("Insufficient stock");
			}
			shopController.checkoutSuccess();
			products.stream().forEach(shopController::removeCartProduct);
			return null;
		});
	}

	public void setShopController(EShopController shopController) {
		this.shopController = shopController;
	}

}
