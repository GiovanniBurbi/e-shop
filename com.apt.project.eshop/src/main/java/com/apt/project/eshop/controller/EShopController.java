package com.apt.project.eshop.controller;

import java.util.List;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.CartRepository;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.repository.ShopManager;
import com.apt.project.eshop.view.EShopView;

public class EShopController {

	private ProductRepository productRepository;
	private CartRepository cartRepository;
	private EShopView eShopView;
	private ShopManager shopManager;
	
	public EShopController(ProductRepository productRepository, CartRepository cartRepository, EShopView eShopView, ShopManager shopManager) {
		this.productRepository = productRepository;
		this.cartRepository = cartRepository;
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
		cartRepository.addToCart(productToAdd);
		eShopView.addToCartView(cartRepository.allCart());
		eShopView.showTotalCost(cartRepository.cartTotalCost());
	}

	public void removeCartProduct(Product product) {
		cartRepository.removeFromCart(product);
		eShopView.removeFromCartView(product);
		eShopView.showTotalCost(cartRepository.cartTotalCost());
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

	public void showCart() {
		eShopView.showAllCart(cartRepository.allCart());
	}

	public void showCartCost() {
		eShopView.showTotalCost(cartRepository.cartTotalCost());
	}
}
