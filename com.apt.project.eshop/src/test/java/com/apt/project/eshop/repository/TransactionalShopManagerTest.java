package com.apt.project.eshop.repository;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.apt.project.eshop.model.Product;


public class TransactionalShopManagerTest {
	
	private TransactionalShopManager shopManager;
	
	@Mock
	private TransactionManager transactionManager;
	@Mock
	private ProductRepository productRepository;
	
	private AutoCloseable closeable;
	
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		// make sure the lambda passed to the TransactionManager
		// is executed, using the mock repository
		given(transactionManager.doInTransaction(any()))
			.willAnswer(
				answer((TransactionCode<?> code) -> code.apply(productRepository)));
		shopManager = new TransactionalShopManager(transactionManager);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testCheckoutWhenSuccessfull() {
		Product product1 = new Product("1", "laptop", 1300);
		Product product2 = new Product("1", "eBook", 300);
		given(productRepository.allCart()).willReturn(asList(product1, product2));
		shopManager.checkout();
		then(productRepository).should().removeFromCart(product1);
		then(productRepository).should().removeFromStorage(product1);
		then(productRepository).should().removeFromCart(product2);
		then(productRepository).should().removeFromStorage(product2);
		then(transactionManager).should(times(1)).doInTransaction(any());
	}

}
