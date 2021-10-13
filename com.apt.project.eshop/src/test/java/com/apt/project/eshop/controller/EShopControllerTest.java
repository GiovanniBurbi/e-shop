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
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.view.EShopView;


public class EShopControllerTest {

	@Mock
	private ProductRepository productRepository;
	@Mock
	private EShopView eShopView;
	@InjectMocks
	private EShopController eShopController;
	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testAllProducts() {
		List<Product> products = asList(new Product("1", "laptop", 1300));
		given(productRepository.findAll()).willReturn(products);
		eShopController.allProducts();
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
	public void testRemoveCartProduct() {
		Product product = new Product("1", "Laptop", 1300);
		doNothing().when(productRepository).removeFromCart(product);
		eShopController.removeCartProduct(product);
		InOrder inOrder = inOrder(productRepository, eShopView);
		then(productRepository).should(inOrder).removeFromCart(product);
		then(eShopView).should(inOrder).removeFromCartView(product);
		verifyNoMoreInteractions(productRepository);
		verifyNoMoreInteractions(eShopView);
	}
	
	@Test
	public void testNewCartProductShouldUpdateTotalCostOfTheCartInTheView() {
		Product product = new Product("1", "Laptop", 1300);
		given(productRepository.allCart()).willReturn(asList(product));
		eShopController.newCartProduct(product);
		InOrder inOrder = inOrder(productRepository, eShopView);
		then(productRepository).should(inOrder).addToCart(product);
		then(eShopView).should(inOrder).addToCartView(asList(product));
		then(eShopView).should(inOrder).updateTotal(product.getPrice());
		verifyNoMoreInteractions(ignoreStubs(productRepository));
		verifyNoMoreInteractions(ignoreStubs(eShopView));
	}
	
	@Test
	public void testNewCartProductWhenCartHasFewProductsAlreadyShouldUpdateTotalCostOfTheCartInTheView() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		given(productRepository.allCart()).willReturn(asList(product1, product2));
		eShopController.newCartProduct(product2);
		InOrder inOrder = inOrder(productRepository, eShopView);
		then(productRepository).should(inOrder).addToCart(product2);
		then(eShopView).should(inOrder).addToCartView(asList(product1, product2));
		then(eShopView).should(inOrder).updateTotal(product2.getPrice());
		verifyNoMoreInteractions(ignoreStubs(productRepository));
	}
}
