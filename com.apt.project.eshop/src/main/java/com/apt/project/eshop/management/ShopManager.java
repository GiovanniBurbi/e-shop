package com.apt.project.eshop.management;

import java.util.List;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.model.CartItem;
import com.apt.project.eshop.model.CatalogItem;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.RepositoryException;

public class ShopManager {

	private TransactionManager transactionManager;

	private EShopController shopController;

	public ShopManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setShopController(EShopController shopController) {
		this.shopController = shopController;
	}

	public void checkout() {
		transactionManager.doInTransaction((productRepository, cartRepository) -> {
			List<CartItem> items = cartRepository.allCart();
			try {
				for (CartItem item : items) {
					productRepository.removeFromStorage(item);
					cartRepository.removeFromCart(item.getProduct());
				}
			} catch (RepositoryException e) {
				shopController.checkoutFailure(e.getItem());
				throw new RepositoryException("Insufficient stock", e.getItem());
			}
			shopController.checkoutSuccess();
			return null;
		});
	}

	public List<CatalogItem> allProducts() {
		return transactionManager.doInTransaction((productRepository, cartRepository) -> productRepository.findAll());
	}

	public List<CatalogItem> productsByName(String nameToFind) {
		return transactionManager
				.doInTransaction((productRepository, cartRepository) -> productRepository.findByName(nameToFind));
	}

	public List<CartItem> cartProducts() {
		return transactionManager.doInTransaction((productRepository, cartRepository) -> cartRepository.allCart());
	}

	public void addToCart(Product product) {
		transactionManager.doInTransaction((productRepository, cartRepository) -> {
			cartRepository.addToCart(product);
			return null;
		});
	}

	public double cartCost() {
		return transactionManager
				.doInTransaction((productRepository, cartRepository) -> cartRepository.cartTotalCost());
	}

	public void removeFromCart(Product product) {
		transactionManager.doInTransaction((productRepository, cartRepository) -> {
			cartRepository.removeFromCart(product);
			return null;
		});
	}

	public void loadCatalog(List<CatalogItem> products) {
		transactionManager.doInTransaction((productRepository, cartRepository) -> {
			productRepository.loadCatalog(products);
			return null;
		});
	}
}
