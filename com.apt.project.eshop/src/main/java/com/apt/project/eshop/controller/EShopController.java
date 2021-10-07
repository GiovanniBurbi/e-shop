package com.apt.project.eshop.controller;

import static java.util.Arrays.asList;

import java.util.List;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.view.EShopView;

public class EShopController {

	private ProductRepository productRepository;
	private EShopView eShopView;
	
	public EShopController(ProductRepository productRepository, EShopView eShopView) {
		this.productRepository = productRepository;
		this.eShopView = eShopView;
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
		eShopView.addToCartView(asList(productToAdd));
	}
}
