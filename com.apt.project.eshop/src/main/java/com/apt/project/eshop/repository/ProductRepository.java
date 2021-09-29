package com.apt.project.eshop.repository;

import java.util.List;

import com.apt.project.eshop.model.Product;

public interface ProductRepository {

	public List<Product> findAll();

	public void loadCatalog(List<Product> products);

}
