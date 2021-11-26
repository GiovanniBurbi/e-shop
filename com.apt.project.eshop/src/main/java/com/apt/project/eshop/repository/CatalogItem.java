package com.apt.project.eshop.repository;

import com.apt.project.eshop.model.Product;

public class CatalogItem {
	
	private Product product;
	private int storage;
	
	public CatalogItem(Product product, int storage) {
		this.product = product;
		this.storage = storage;
	}

	public Product getProduct() {
		return product;
	}

	public int getStorage() {
		return storage;
	}	
}
