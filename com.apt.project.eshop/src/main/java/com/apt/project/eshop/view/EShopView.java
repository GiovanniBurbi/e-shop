package com.apt.project.eshop.view;

import java.util.List;

import com.apt.project.eshop.model.Product;

public interface EShopView {

	void showAllProducts(List<Product> products);

	void showSearchedProducts(List<Product> searchedProducts);

	void showErrorProductNotFound(String product);

	void clearSearch(List<Product> products);

	void addToCartView(List<Product> products);

	void removeFromCartView(Product product);

}
