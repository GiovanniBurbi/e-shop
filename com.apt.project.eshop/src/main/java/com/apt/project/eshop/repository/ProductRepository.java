package com.apt.project.eshop.repository;

import java.util.List;

import com.apt.project.eshop.model.Product;

public interface ProductRepository {

	public List<Product> findAll();

	public List<Product> findByName(String nameSearch);

	public void loadCatalog(List<CatalogItem> items);

	public void removeFromStorage(Product product) throws RepositoryException;
}
