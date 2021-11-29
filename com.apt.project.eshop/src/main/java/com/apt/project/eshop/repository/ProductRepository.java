package com.apt.project.eshop.repository;

import java.util.List;

import com.apt.project.eshop.model.CartItem;
import com.apt.project.eshop.model.CatalogItem;

public interface ProductRepository {

	public List<CatalogItem> findAll();

	public List<CatalogItem> findByName(String nameSearch);

	public void loadCatalog(List<CatalogItem> items);

	public void removeFromStorage(CartItem item) throws RepositoryException;
}
