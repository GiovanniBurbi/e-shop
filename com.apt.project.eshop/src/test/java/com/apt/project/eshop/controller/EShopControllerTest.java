package com.apt.project.eshop.controller;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
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
		then(shopManager).should().allProducts();
		then(eShopView).should().showAllProducts(products);
	}
	
	@Test
	public void testSearchedProducts() {
		String nameSearch = "laptop";
		List<Product> searchedProducts = asList(
									new Product("1", "laptop", 1300),
									new Product("3", "laptop MSI", 1200)
								);
		given(productRepository.findByName(nameSearch)).willReturn(searchedProducts);
		eShopController.searchProducts(nameSearch);
		then(eShopView).should().showSearchedProducts(searchedProducts);
	}
	
	@Test
	public void testSearchedProductsWhenTheSearchedProductDoesNotExist() {
		String nameSearch = "samsung";
		given(productRepository.findByName(nameSearch)).willReturn(emptyList());
		eShopController.searchProducts(nameSearch);
		then(eShopView).should().showErrorProductNotFound(nameSearch);
		verifyNoMoreInteractions(ignoreStubs(productRepository));
	}
	
	@Test
	public void testResetSearch() {
		List<Product> products = asList(new Product("1", "laptop", 1300));
		given(productRepository.findAll()).willReturn(products);
		eShopController.resetSearch();
		then(eShopView).should().clearSearch(products);
	}
	
	@Test
	public void testNewCartProductShouldUpdateTotalCostOfTheCartInTheView() {
		Product product = new Product("1", "Laptop", 1300);
		given(cartRepository.allCart()).willReturn(asList(product));
		given(cartRepository.cartTotalCost()).willReturn(product.getPrice());
		eShopController.newCartProduct(product);
		InOrder inOrder = inOrder(cartRepository, eShopView);
		then(cartRepository).should(inOrder).addToCart(product);
		then(eShopView).should(inOrder).addToCartView(asList(product));
		then(cartRepository).should(inOrder).cartTotalCost();
		then(eShopView).should(inOrder).showTotalCost(product.getPrice());
		verifyNoMoreInteractions(ignoreStubs(productRepository));
		verifyNoMoreInteractions(eShopView);
	}
	
	@Test
	public void testNewCartProductWhenCartHasFewProductsAlreadyShouldUpdateTotalCostOfTheCartInTheView() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		given(cartRepository.allCart()).willReturn(asList(product1, product2));
		double newAmount = product1.getPrice() + (product2.getPrice()*2);
		given(cartRepository.cartTotalCost()).willReturn(newAmount);
		eShopController.newCartProduct(product2);
		InOrder inOrder = inOrder(cartRepository, eShopView);
		then(cartRepository).should(inOrder).addToCart(product2);
		then(cartRepository).should(inOrder).allCart();
		then(eShopView).should(inOrder).addToCartView(asList(product1, product2));
		then(cartRepository).should(inOrder).cartTotalCost();
		then(eShopView).should(inOrder).showTotalCost(newAmount);
		verifyNoMoreInteractions(ignoreStubs(productRepository));
		verifyNoMoreInteractions(eShopView);
	}
	
	@Test
	public void testRemoveCartProduct() {
		Product product = new Product("1", "Laptop", 1300);
		doNothing().when(cartRepository).removeFromCart(product);
		given(cartRepository.cartTotalCost()).willReturn(0.0);
		eShopController.removeCartProduct(product);
		InOrder inOrder = inOrder(cartRepository, eShopView);
		then(cartRepository).should(inOrder).removeFromCart(product);
		then(eShopView).should(inOrder).removeFromCartView(product);
		then(cartRepository).should().cartTotalCost();
		then(eShopView).should(inOrder).showTotalCost(0.0);
		verifyNoMoreInteractions(productRepository);
		verifyNoMoreInteractions(eShopView);
	}
	
	@Test
	public void testRemoveCartProductWhenCartHasMoreThanOneItemOfTheSameProductShouldUpdateCartCostForAllItemsOfTheSameProduct() { 
		Product product = new Product("1", "Laptop", 1300, 2);
		doNothing().when(cartRepository).removeFromCart(product);
		given(cartRepository.cartTotalCost()).willReturn(0.0);
		eShopController.removeCartProduct(product);
		InOrder inOrder = inOrder(cartRepository, eShopView);
		then(cartRepository).should(inOrder).removeFromCart(product);
		then(eShopView).should(inOrder).removeFromCartView(product);
		then(eShopView).should(inOrder).showTotalCost(0.0);
		verifyNoMoreInteractions(productRepository);
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
		given(cartRepository.allCart()).willReturn(products);
		eShopController.showCart();
		then(eShopView).should().showAllCart(products);
	}
	
	@Test
	public void testShowCartCost() {
		double totalCart = 2500;
		given(cartRepository.cartTotalCost()).willReturn(totalCart);
		eShopController.showCartCost();
		then(eShopView).should().showTotalCost(totalCart);
	}
	
	@Test
	public void testAllCartProductsDelegateToCartRepository() {
		eShopController.allCartProducts();
		then(cartRepository).should().allCart();
	}
	
	@Test
	public void testAllCartProductsShouldReturnCartProducts(){
		List<Product> products = asList(new Product("1", "laptop", 1300, 2), new Product("2", "Iphone", 1000, 3));
		given(cartRepository.allCart()).willReturn(products);
		assertThat(eShopController.allCartProducts()).contains(new Product("1", "laptop", 1300, 2), new Product("2", "Iphone", 1000, 3));
	}
}
