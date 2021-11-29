package com.apt.project.eshop.view;

import java.util.List;

import com.apt.project.eshop.repository.CartItem;
import com.apt.project.eshop.repository.CatalogItem;

public interface EShopView {

	void showAllProducts(List<CatalogItem> products);

	void showSearchedProducts(List<CatalogItem> searchedProducts);

	void showErrorProductNotFound(String product);

	void clearSearch(List<CatalogItem> items);

	void addToCartView(List<CartItem> items);

	void removeFromCartView(CartItem item);

	void clearCart();

	void showSuccessLabel();

	void resetTotalCost();

	void showFailureLabel(CatalogItem itemWanted);

	void showAllCart(List<CartItem> cartItems);

	void showTotalCost(double cartPrice);

}
