package com.apt.project.eshop.repository;

import static java.util.Arrays.asList;
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
	private EShopController shopController;
	
	private AutoCloseable closeable;
	
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		// make sure the lambda passed to the TransactionManager
		// is executed, using the mock repository
		given(transactionManager.doInTransaction(any()))
			.willAnswer(
				answer((TransactionCode<?> code) -> code.apply(productRepository)));
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
		given(shopController.allCartProducts()).willReturn(asList(product1, product2));
		shopManager.checkout();
		InOrder inOrder = inOrder(productRepository, shopController);
		try {
			then(productRepository).should(inOrder).removeFromStorage(product1);
		} catch (RepositoryException e) {
			fail("Should not throw an exception in this test case!");
		}
		try {
			then(productRepository).should(inOrder).removeFromStorage(product2);
		} catch (RepositoryException e) {
			fail("Should not throw an exception in this test case!");
		}
		then(shopController).should(inOrder).removeCartProduct(product1);
		then(shopController).should(inOrder).removeCartProduct(product2);
		then(shopController).should(inOrder).checkoutSuccess();
		then(transactionManager).should(times(1)).doInTransaction(any());
	}
	
	@Test
	public void testCheckoutWhenThereIsNotEnoughStockShouldThrowMongoExceptionAndDelegateToControllerSuccessCheckout() throws RepositoryException {
		Product productNotAvailable = new Product("1", "Laptop", 1300, 2);
		Product product2 = new Product("2", "eBook", 300, 1);
		given(shopController.allCartProducts()).willReturn(asList(productNotAvailable, product2));
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
		given(shopController.allCartProducts()).willReturn(asList(product1, productNotAvailable, product2));
		willThrow(new RepositoryException("Insufficient stock", productNotAvailable)).given(productRepository).removeFromStorage(productNotAvailable);
		InOrder inOrder = inOrder(productRepository, shopController);
		assertThatThrownBy(() -> shopManager.checkout())
		.isInstanceOf(MongoException.class).hasMessage("Insufficient stock");
		then(productRepository).should(inOrder).removeFromStorage(product1);
		then(productRepository).should(inOrder).removeFromStorage(productNotAvailable);
		then(shopController).should(inOrder).checkoutFailure(productNotAvailable);
		verifyNoMoreInteractions(ignoreStubs(productRepository));
		then(transactionManager).should(times(1)).doInTransaction(any());
	}
}
