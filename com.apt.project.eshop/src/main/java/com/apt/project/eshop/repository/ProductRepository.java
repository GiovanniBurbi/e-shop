package com.apt.project.eshop.repository;

import java.util.List;

import com.apt.project.eshop.model.Product;

public interface ProductRepository {

	public List<Product> findAll();

	public List<Product> findByName(String nameSearch);

	public void loadCatalog(List<Product> products);

	public void addToCart(Product product);

	public List<Product> allCart();

	public void removeFromCart(Product product);
}
