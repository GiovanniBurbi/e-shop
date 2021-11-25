package com.apt.project.eshop.repository;

import com.apt.project.eshop.model.Product;

public class CartItem {
	
	private Product product;
	private int quantity;

	public CartItem(Product product, int quantity) {
		this.product = product;
		this.quantity = quantity;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}
}
