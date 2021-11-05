package com.apt.project.eshop.repository;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.model.Product;
import com.mongodb.MongoException;


public class TransactionalShopManagerTest {
	
	
	@Mock
	private TransactionManager transactionManager;
	@InjectMocks
	private ShopManager shopManager;
	@Mock
	private ProductRepository productRepository;
	@Mock
	private CartRepository cartRepository;
	@Mock
	private EShopController shopController;
	
	private AutoCloseable closeable;
	
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		// make sure the lambda passed to the TransactionManager
		// is executed, using the mock repository
		given(transactionManager.doInTransaction(any()))
			.willAnswer(
				answer((TransactionCode<?> code) -> code.apply(productRepository, cartRepository)));
		given(transactionManager.doInTransactionAndReturn(any()))
		.willAnswer(
			answer((TransactionCode<?> code) -> code.apply(productRepository, cartRepository)));
		shopManager.setShopController(shopController);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testCheckoutWhenSuccessfull() {
		Product product1 = new Product("1", "laptop", 1300);
		Product product2 = new Product("1", "eBook", 300);
		given(cartRepository.allCart()).willReturn(asList(product1, product2));
		shopManager.checkout();
		InOrder inOrder = inOrder(productRepository, cartRepository, shopController);
		try {
			then(productRepository).should(inOrder).removeFromStorage(product1);
		} catch (RepositoryException e) {
			fail("Should not throw an exception in this test case!");
		}
		then(cartRepository).should(inOrder).removeFromCart(product1);
		try {
			then(productRepository).should(inOrder).removeFromStorage(product2);
		} catch (RepositoryException e) {
			fail("Should not throw an exception in this test case!");
		}
		then(cartRepository).should(inOrder).removeFromCart(product2);
		then(shopController).should(inOrder).checkoutSuccess();
		then(transactionManager).should(times(1)).doInTransaction(any());
	}
	
	@Test
	public void testCheckoutWhenThereIsNotEnoughStockShouldThrowMongoExceptionAndDelegateToControllerSuccessCheckout() throws RepositoryException {
		Product productNotAvailable = new Product("1", "Laptop", 1300, 2);
		Product product2 = new Product("2", "eBook", 300, 1);
		given(cartRepository.allCart()).willReturn(asList(productNotAvailable, product2));
		willThrow(new RepositoryException("Insufficient stock", productNotAvailable)).given(productRepository).removeFromStorage(productNotAvailable);
		assertThatThrownBy(() -> shopManager.checkout())
			.isInstanceOf(MongoException.class).hasMessage("Insufficient stock");
		then(shopController).should().checkoutFailure(productNotAvailable);
		then(transactionManager).should(times(1)).doInTransaction(any());
	}
	
	@Test
	public void testCheckoutWhenThereIsNotEnoughStockOfAProductShouldNotRemoveAnyProductFromTheCartAndDelegateToControllerCheckoutFailure() throws RepositoryException {
		Product product1 = new Product("3", "Iphone", 1000.0, 1);
		Product productNotAvailable = new Product("1", "Laptop", 1300, 2);
		Product product2 = new Product("2", "eBook", 300, 1);
		given(cartRepository.allCart()).willReturn(asList(product1, productNotAvailable, product2));
		willThrow(new RepositoryException("Insufficient stock", productNotAvailable)).given(productRepository).removeFromStorage(productNotAvailable);
		InOrder inOrder = inOrder(productRepository, cartRepository, shopController);
		assertThatThrownBy(() -> shopManager.checkout())
			.isInstanceOf(MongoException.class).hasMessage("Insufficient stock");
		then(productRepository).should(inOrder).removeFromStorage(product1);
		then(cartRepository).should(inOrder).removeFromCart(product1);
		then(productRepository).should(inOrder).removeFromStorage(productNotAvailable);
		then(shopController).should(inOrder).checkoutFailure(productNotAvailable);
		verifyNoMoreInteractions(ignoreStubs(productRepository));
		verifyNoMoreInteractions(ignoreStubs(cartRepository));
		then(transactionManager).should(times(1)).doInTransaction(any());
	}
	
	@Test
	public void testAllProductsShouldDelegateToProductRepositoryAndReturnAllProductsInTheDatabase() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "eBook", 300);
		given(productRepository.findAll()).willReturn(asList(product1, product2));
		assertThat(shopManager.allProducts()).containsExactly(product1, product2);
		then(productRepository).should().findAll();
	}
	
	@Test
	public void testProductsByNameShouldDelegateToProductRepositoryAndReturnAllProductsThatMatchAStringInTheDatabase() {
		String nameToSearch = "Laptop";
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("3", "Laptop MSI", 1250);
		given(productRepository.findByName(nameToSearch)).willReturn(asList(product1, product2));
		assertThat(shopManager.productsByName(nameToSearch)).containsExactly(product1, product2);
		then(productRepository).should().findByName(nameToSearch);
	}
	
	@Test
	public void testCartProductsShouldDelegateToCartRepositoryAndReturnAllProductsInTheCartInsideTheDatabase() {
		Product product1 = new Product("1", "Laptop", 1300, 3);
		Product product2 = new Product("2", "eBook", 300, 2);
		given(cartRepository.allCart()).willReturn(asList(product1, product2));
		assertThat(shopManager.cartProducts()).containsExactly(product1, product2);
		then(cartRepository).should().allCart();
	}
	
	@Test
	public void testAddToCartShouldDelegateToCartRepository() {
		Product product = new Product("1", "Laptop", 1300);
		shopManager.addToCart(product);
		then(cartRepository).should().addToCart(product);
	}
	
	@Test
	public void testCartCostShouldDelegateToCartRepositoryAndReturnADouble() {
		double totalCart = 1250.0;
		given(cartRepository.cartTotalCost()).willReturn(totalCart);
		assertThat(shopManager.cartCost()).isEqualTo(totalCart);
		then(cartRepository).should().cartTotalCost();
	}
	
	@Test
	public void testRemoveFromCartShouldDelegateToCartRepository() {
		Product product = new Product("1", "Laptop", 1300);
		shopManager.removeFromCart(product);
		then(cartRepository).should().removeFromCart(product);
	}
	
	@Test
	public void testLoadCatalogShouldDelegateToProductRepository() {
		List<Product> products = asList(
				new Product("1", "Laptop", 1300),
				new Product("2", "eBook", 300)
		);
		shopManager.loadCatalog(products);
		then(productRepository).should().loadCatalog(products);
	}
}
