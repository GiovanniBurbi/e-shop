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

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.CartRepository;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.repository.ShopManager;
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
		List<Product> products = asList(new Product("1", "laptop", 1300));
		given(shopManager.allProducts()).willReturn(products);
		eShopController.allProducts();
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).allProducts();
		then(eShopView).should(inOrder).showAllProducts(products);
		verifyNoMoreInteractions(ignoreStubs(shopManager));
	}
	
	@Test
	public void testSearchedProductsShouldDelegateToShopManager() {
		String nameSearch = "laptop";
		List<Product> searchedProducts = asList(
									new Product("1", "laptop", 1300),
									new Product("3", "laptop MSI", 1200)
								);
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
		List<Product> products = asList(new Product("1", "laptop", 1300));
		given(shopManager.allProducts()).willReturn(products);
		eShopController.resetSearch();
		then(eShopView).should().clearSearch(products);
	}
	
	@Test
	public void testNewCartProductShouldDelegateToShopManagerAndAddToCartViewAndUpdateTotalCostOfTheCartInTheView() {
		Product product = new Product("1", "Laptop", 1300, 2);
		double cartCost = product.getPrice() * product.getQuantity();
		given(shopManager.cartProducts()).willReturn(asList(product));
		given(shopManager.cartCost()).willReturn(cartCost);
		eShopController.newCartProduct(product);
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).addToCart(product);
		then(eShopView).should(inOrder).addToCartView(asList(product));
		then(shopManager).should(inOrder).cartCost();
		then(eShopView).should(inOrder).showTotalCost(cartCost);
		verifyNoMoreInteractions(ignoreStubs(shopManager));
		verifyNoMoreInteractions(eShopView);
	}
	
	@Test
	public void testNewCartProductWhenCartHasFewProductsAlreadyShouldUpdateTotalCostOfTheCartInTheView() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000, 2);
		given(shopManager.cartProducts()).willReturn(asList(product1, product2));
		// total cost cart after new product2 is added
		double newAmount = product1.getPrice() + (product2.getPrice()*product2.getQuantity());
		given(shopManager.cartCost()).willReturn(newAmount);
		eShopController.newCartProduct(product2);
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).addToCart(product2);
		then(shopManager).should(inOrder).cartProducts();
		then(eShopView).should(inOrder).addToCartView(asList(product1, product2));
		then(shopManager).should(inOrder).cartCost();
		then(eShopView).should(inOrder).showTotalCost(newAmount);
		verifyNoMoreInteractions(ignoreStubs(shopManager));
		verifyNoMoreInteractions(eShopView);
	}
	
	@Test
	public void testRemoveCartProductShouldDelegateToShopManagerAndUpdateTotalCostCart() {
		Product onlyProductInCart = new Product("1", "Laptop", 1300);
		doNothing().when(shopManager).removeFromCart(onlyProductInCart);
		given(shopManager.cartCost()).willReturn(0.0);
		eShopController.removeCartProduct(onlyProductInCart);
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).removeFromCart(onlyProductInCart);
		then(eShopView).should(inOrder).removeFromCartView(onlyProductInCart);
		then(shopManager).should().cartCost();
		then(eShopView).should(inOrder).showTotalCost(0.0);
		verifyNoMoreInteractions(shopManager);
		verifyNoMoreInteractions(eShopView);
	}
	
	@Test
	public void testRemoveCartProductWhenCartHasMoreThanOneItemOfTheSameProductShouldUpdateCartCostForAllItemsOfTheSameProduct() { 
		Product product = new Product("1", "Laptop", 1300, 2);
		doNothing().when(shopManager).removeFromCart(product);
		given(shopManager.cartCost()).willReturn(0.0);
		eShopController.removeCartProduct(product);
		InOrder inOrder = inOrder(shopManager, eShopView);
		then(shopManager).should(inOrder).removeFromCart(product);
		then(eShopView).should(inOrder).removeFromCartView(product);
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
		Product product = new Product("1", "Laptop", 1300.0, 1);
		eShopController.checkoutFailure(product);
		then(eShopView).should().showFailureLabel(product);
		verifyNoMoreInteractions(eShopView);
	}
	
	@Test
	public void testShowCart() {
		List<Product> products = asList(new Product("1", "laptop", 1300));
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
