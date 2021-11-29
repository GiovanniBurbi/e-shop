package com.apt.project.eshop.controller;

import java.util.List;

import com.apt.project.eshop.management.ShopManager;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.CartItem;
import com.apt.project.eshop.repository.CatalogItem;
import com.apt.project.eshop.view.EShopView;

public class EShopController {

	private EShopView eShopView;
	private ShopManager shopManager;

	public EShopController(EShopView eShopView, ShopManager shopManager) {
		this.eShopView = eShopView;
		this.shopManager = shopManager;
	}

	public void allProducts() {
		eShopView.showAllProducts(shopManager.allProducts());
	}

	public void searchProducts(String searchName) {
		List<CatalogItem> productsFound = shopManager.productsByName(searchName);
		if (!productsFound.isEmpty()) {
			eShopView.showSearchedProducts(productsFound);
			return;
		}
		eShopView.showErrorProductNotFound(searchName);
	}

	public void resetSearch() {
		eShopView.clearSearch(shopManager.allProducts());
	}

	public void newCartProduct(Product productToAdd) {
		shopManager.addToCart(productToAdd);
		eShopView.addToCartView(shopManager.cartProducts());
		eShopView.showTotalCost(shopManager.cartCost());
	}

	public void removeCartProduct(CartItem item) {
		shopManager.removeFromCart(item.getProduct());
		eShopView.removeFromCartView(item);
		eShopView.showTotalCost(shopManager.cartCost());
	}

	public void checkoutCart() {
		shopManager.checkout();
	}

	public void checkoutSuccess() {
		eShopView.showSuccessLabel();
		eShopView.clearCart();
		eShopView.resetTotalCost();
	}

	public void checkoutFailure(CatalogItem productWanted) {
		eShopView.showFailureLabel(productWanted);
	}

	public void showCart() {
		eShopView.showAllCart(shopManager.cartProducts());
	}

	public void showCartCost() {
		eShopView.showTotalCost(shopManager.cartCost());
	}
}
