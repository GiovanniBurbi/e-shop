package com.apt.project.eshop.controller;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.apt.project.eshop.management.ShopManager;
import com.apt.project.eshop.model.CartItem;
import com.apt.project.eshop.model.CatalogItem;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.CartRepository;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.view.EShopView;

public class EShopControllerTest {

	@Mock
	private ProductRepository productRepository;
	@Mock
	private CartRepository cartRepository;
	@Mock
	private EShopView eShopView;
	@InjectMocks
	private EShopController eShopController;
	private AutoCloseable closeable;
	@Mock
	private ShopManager shopManager;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testAllProductsShouldDelegateToShopManager() {
		List<CatalogItem> items = asList(new CatalogItem(new Product("1", "laptop", 1300), 1));
		given(shopManager.allProducts()).willReturn(items);
		eShopController.allProducts();
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).allProducts();
		then(eShopView).should(inOrder).showAllProducts(items);
		verifyNoMoreInteractions(ignoreStubs(shopManager));
	}

	@Test
	public void testSearchedProductsShouldDelegateToShopManager() {
		String nameSearch = "laptop";
		List<CatalogItem> searchedProducts = asList(new CatalogItem(new Product("1", "laptop", 1300), 1), new CatalogItem(new Product("3", "laptop MSI", 1200), 1));
		given(shopManager.productsByName(nameSearch)).willReturn(searchedProducts);
		eShopController.searchProducts(nameSearch);
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).productsByName(nameSearch);
		then(eShopView).should(inOrder).showSearchedProducts(searchedProducts);
		verifyNoMoreInteractions(ignoreStubs(shopManager));
	}

	@Test
	public void testSearchedProductsWhenTheSearchedProductDoesNotExist() {
		String nameSearch = "samsung";
		given(shopManager.productsByName(nameSearch)).willReturn(emptyList());
		eShopController.searchProducts(nameSearch);
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).productsByName(nameSearch);
		then(eShopView).should(inOrder).showErrorProductNotFound(nameSearch);
		verifyNoMoreInteractions(ignoreStubs(shopManager));
	}

	@Test
	public void testResetSearch() {
		List<CatalogItem> products = asList(new CatalogItem(new Product("1", "laptop", 1300), 1));
		given(shopManager.allProducts()).willReturn(products);
		eShopController.resetSearch();
		then(eShopView).should().clearSearch(products);
	}

	@Test
	public void testNewCartProductShouldDelegateToShopManagerAndAddToCartViewAndUpdateTotalCostOfTheCartInTheView() {
		CartItem item = new CartItem(new Product("1", "Laptop", 1300), 2);
		double cartCost = item.getProduct().getPrice() * item.getQuantity();
		given(shopManager.cartProducts()).willReturn(asList(item));
		given(shopManager.cartCost()).willReturn(cartCost);
		eShopController.newCartProduct(item.getProduct());
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).addToCart(item.getProduct());
		then(eShopView).should(inOrder).addToCartView(asList(item));
		then(shopManager).should(inOrder).cartCost();
		then(eShopView).should(inOrder).showTotalCost(cartCost);
		verifyNoMoreInteractions(ignoreStubs(shopManager));
		verifyNoMoreInteractions(eShopView);
	}

	@Test
	public void testNewCartProductWhenCartHasFewProductsAlreadyShouldUpdateTotalCostOfTheCartInTheView() {
		CartItem item1= new CartItem(new Product("1", "Laptop", 1300), 1);
		CartItem item2 = new CartItem(new Product("2", "Iphone", 1000), 2);
		given(shopManager.cartProducts()).willReturn(asList(item1, item2));
		// total cost cart after new product2 is added
		double newAmount = item1.getProduct().getPrice() + (item2.getProduct().getPrice() * item2.getQuantity());
		given(shopManager.cartCost()).willReturn(newAmount);
		eShopController.newCartProduct(item2.getProduct());
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).addToCart(item2.getProduct());
		then(shopManager).should(inOrder).cartProducts();
		then(eShopView).should(inOrder).addToCartView(asList(item1, item2));
		then(shopManager).should(inOrder).cartCost();
		then(eShopView).should(inOrder).showTotalCost(newAmount);
		verifyNoMoreInteractions(ignoreStubs(shopManager));
		verifyNoMoreInteractions(eShopView);
	}

	@Test
	public void testRemoveCartProductShouldDelegateToShopManagerAndUpdateTotalCostCart() {
		CartItem onlyItemInCart = new CartItem(new Product("1", "Laptop", 1300), 1);
		doNothing().when(shopManager).removeFromCart(onlyItemInCart.getProduct());
		given(shopManager.cartCost()).willReturn(0.0);
		eShopController.removeCartProduct(onlyItemInCart);
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).removeFromCart(onlyItemInCart.getProduct());
		then(eShopView).should(inOrder).removeFromCartView(onlyItemInCart);
		then(shopManager).should().cartCost();
		then(eShopView).should(inOrder).showTotalCost(0.0);
		verifyNoMoreInteractions(shopManager);
		verifyNoMoreInteractions(eShopView);
	}

	@Test
	public void testRemoveCartProductWhenCartHasMoreThanOneItemOfTheSameProductShouldUpdateCartCostForAllItemsOfTheSameProduct() {
		CartItem item = new CartItem(new Product("1", "Laptop", 1300), 2);
		doNothing().when(shopManager).removeFromCart(item.getProduct());
		given(shopManager.cartCost()).willReturn(0.0);
		eShopController.removeCartProduct(item);
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).removeFromCart(item.getProduct());
		then(eShopView).should(inOrder).removeFromCartView(item);
		then(eShopView).should(inOrder).showTotalCost(0.0);
		verifyNoMoreInteractions(ignoreStubs(shopManager));
		verifyNoMoreInteractions(eShopView);
	}

	@Test
	public void testCheckoutCartShouldDelegateToShopManager() {
		eShopController.checkoutCart();
		then(shopManager).should().checkout();
	}

	@Test
	public void testCheckoutSuccessShouldClearCartAndShowSuccessfullCheckoutAndResetTheTotalCost() {
		eShopController.checkoutSuccess();
		InOrder inOrder = inOrder(eShopView);
		then(eShopView).should(inOrder).showSuccessLabel();
		then(eShopView).should(inOrder).clearCart();
		then(eShopView).should(inOrder).resetTotalCost();
	}

	@Test
	public void testCheckoutFailureShouldShowCheckoutFailureLabel() {
		CatalogItem item = new CatalogItem(new Product("1", "Laptop", 1300.0), 1);
		eShopController.checkoutFailure(item);
		then(eShopView).should().showFailureLabel(item);
		verifyNoMoreInteractions(eShopView);
	}

	@Test
	public void testShowCart() {
		List<CartItem> products = asList(new CartItem(new Product("1", "laptop", 1300), 1));
		given(shopManager.cartProducts()).willReturn(products);
		eShopController.showCart();
		then(eShopView).should().showAllCart(products);
	}

	@Test
	public void testShowCartCost() {
		double totalCart = 2500;
		given(shopManager.cartCost()).willReturn(totalCart);
		eShopController.showCartCost();
		then(eShopView).should().showTotalCost(totalCart);
	}
}
