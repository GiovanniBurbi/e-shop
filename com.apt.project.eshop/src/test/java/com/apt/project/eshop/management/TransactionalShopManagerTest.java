package com.apt.project.eshop.management;

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
import com.apt.project.eshop.repository.CartItem;
import com.apt.project.eshop.repository.CartRepository;
import com.apt.project.eshop.repository.CatalogItem;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.repository.RepositoryException;

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
				.willAnswer(answer(
						(TransactionCode<?> code) -> code.apply(productRepository, cartRepository)));
		shopManager.setShopController(shopController);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testCheckoutWhenSuccessfull() {
		CartItem item1 = new CartItem(new Product("1", "laptop", 1300), 1);
		CartItem item2 = new CartItem(new Product("1", "eBook", 300), 1);
		given(cartRepository.allCart()).willReturn(asList(item1, item2));
		shopManager.checkout();
		InOrder inOrder = inOrder(productRepository, cartRepository, shopController);
		try {
			then(productRepository).should(inOrder).removeFromStorage(item1);
		} catch (RepositoryException e) {
			fail("Should not throw an exception in this test case!");
		}
		then(cartRepository).should(inOrder).removeFromCart(item1.getProduct());
		try {
			then(productRepository).should(inOrder).removeFromStorage(item2);
		} catch (RepositoryException e) {
			fail("Should not throw an exception in this test case!");
		}
		then(cartRepository).should(inOrder).removeFromCart(item2.getProduct());
		then(shopController).should(inOrder).checkoutSuccess();
		then(transactionManager).should(times(1)).doInTransaction(any());
	}

	@Test
	public void testCheckoutWhenThereIsNotEnoughStockShouldThrowRepositoryExceptionAndDelegateToControllerSuccessCheckout()
			throws RepositoryException {
		CatalogItem itemNotAvailable = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		CartItem item1 = new CartItem(new Product("1", "Laptop", 1300), 2);
		CartItem item2 = new CartItem(new Product("2", "eBook", 300), 1);
		given(cartRepository.allCart()).willReturn(asList(item1, item2));
		willThrow(new RepositoryException("Insufficient stock", itemNotAvailable)).given(productRepository)
				.removeFromStorage(item1);
		assertThatThrownBy(() -> shopManager.checkout()).isInstanceOf(RepositoryException.class)
				.hasMessage("Repository exception! Insufficient stock, " + itemNotAvailable.getProduct().getName()
						+ " left in stock: " + itemNotAvailable.getStorage());
		then(shopController).should().checkoutFailure(itemNotAvailable);
		then(transactionManager).should(times(1)).doInTransaction(any());
	}

	@Test
	public void testCheckoutWhenThereIsNotEnoughStockOfAProductShouldNotRemoveAnyProductFromTheCartAndDelegateToControllerCheckoutFailure()
			throws RepositoryException {
		CartItem item1 = new CartItem(new Product("3", "Iphone", 1000.0), 1);
		CartItem itemNotAvailable = new CartItem(new Product("1", "Laptop", 1300), 2);
		CatalogItem catalogItemNoStock = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		CartItem item2 = new CartItem(new Product("2", "eBook", 300), 1);
		given(cartRepository.allCart()).willReturn(asList(item1, itemNotAvailable, item2));
		willThrow(new RepositoryException("Insufficient stock", catalogItemNoStock)).given(productRepository)
				.removeFromStorage(itemNotAvailable);
		InOrder inOrder = inOrder(productRepository, cartRepository, shopController);
		assertThatThrownBy(() -> shopManager.checkout()).isInstanceOf(RepositoryException.class)
				.hasMessage("Repository exception! Insufficient stock, " + catalogItemNoStock.getProduct().getName()
						+ " left in stock: " + catalogItemNoStock.getStorage());
		then(productRepository).should(inOrder).removeFromStorage(item1);
		then(cartRepository).should(inOrder).removeFromCart(item1.getProduct());
		then(productRepository).should(inOrder).removeFromStorage(itemNotAvailable);
		then(shopController).should(inOrder).checkoutFailure(catalogItemNoStock);
		verifyNoMoreInteractions(ignoreStubs(productRepository));
		verifyNoMoreInteractions(ignoreStubs(cartRepository));
		then(transactionManager).should(times(1)).doInTransaction(any());
	}

	@Test
	public void testAllProductsShouldDelegateToProductRepositoryAndReturnAllProductsInTheDatabase() {
		CatalogItem item1 = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		CatalogItem item2 = new CatalogItem(new Product("2", "eBook", 300), 1);
		given(productRepository.findAll()).willReturn(asList(item1, item2));
		assertThat(shopManager.allProducts()).containsExactly(item1, item2);
		then(productRepository).should().findAll();
	}

	@Test
	public void testProductsByNameShouldDelegateToProductRepositoryAndReturnAllProductsThatMatchAStringInTheDatabase() {
		String nameToSearch = "Laptop";
		CatalogItem item1 = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		CatalogItem item2 = new CatalogItem(new Product("3", "Laptop MSI", 1250), 1);
		given(productRepository.findByName(nameToSearch)).willReturn(asList(item1, item2));
		assertThat(shopManager.productsByName(nameToSearch)).containsExactly(item1, item2);
		then(productRepository).should().findByName(nameToSearch);
	}

	@Test
	public void testCartProductsShouldDelegateToCartRepositoryAndReturnAllProductsInTheCartInsideTheDatabase() {
		CartItem item1 = new CartItem(new Product("1", "Laptop", 1300), 3);
		CartItem item2 = new CartItem(new Product("2", "eBook", 300), 2);
		given(cartRepository.allCart()).willReturn(asList(item1, item2));
		assertThat(shopManager.cartProducts()).containsExactly(item1, item2);
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
		List<CatalogItem> items = asList(
				new CatalogItem(new Product("1", "Laptop", 1300), 1),
				new CatalogItem(new Product("2", "eBook", 300), 1));
		shopManager.loadCatalog(items);
		then(productRepository).should().loadCatalog(items);
	}
}
