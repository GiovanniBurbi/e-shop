package com.apt.project.eshop.controller;

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
		//TODO
	}
}
