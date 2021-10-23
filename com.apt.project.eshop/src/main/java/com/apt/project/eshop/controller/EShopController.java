package com.apt.project.eshop.controller;

import java.util.List;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.repository.ShopManager;
import com.apt.project.eshop.view.EShopView;

public class EShopController {

	private ProductRepository productRepository;
	private EShopView eShopView;
	private ShopManager shopManager;
	
	public EShopController(ProductRepository productRepository, EShopView eShopView, ShopManager shopManager) {
		this.productRepository = productRepository;
		this.eShopView = eShopView;
		this.shopManager = shopManager;
	}

	public void allProducts() {	
		eShopView.showAllProducts(productRepository.findAll());
	}

	public void searchProducts(String searchName) {
		List<Product> productsFound = productRepository.findByName(searchName);
		if (!productsFound.isEmpty()) {
			eShopView.showSearchedProducts(productsFound);
			return;
		}
		eShopView.showErrorProductNotFound(searchName);
	}

	public void resetSearch() {
		eShopView.clearSearch(productRepository.findAll());
	}

	public void newCartProduct(Product productToAdd) {
		productRepository.addToCart(productToAdd);
		eShopView.addToCartView(productRepository.allCart());
		eShopView.updateTotal(productToAdd.getPrice());
	}

	public void removeCartProduct(Product product) {
		productRepository.removeFromCart(product);
		eShopView.removeFromCartView(product);
		double amountToRemove = product.getPrice() * product.getQuantity();
		eShopView.updateTotal(-(amountToRemove));
	}

	public void checkoutCart() {
		shopManager.checkout();
	}
	
	public void checkoutSuccess() {
		eShopView.showSuccessLabel();
		eShopView.clearCart();
		eShopView.resetTotalCost();
	}
	
	public void checkoutFailure(Product productWanted) {
		eShopView.showFailureLabel(productWanted);
	}
}
