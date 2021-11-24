package com.apt.project.eshop.controller.mongo;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.management.ShopManager;
import com.apt.project.eshop.management.TransactionManager;
import com.apt.project.eshop.management.mongo.TransactionalShopManager;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.CartRepository;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.repository.mongo.CartMongoRepository;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.apt.project.eshop.view.EShopView;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ClientSession;

public class EShopControllerIT {

	private static final String PRODUCTS_COLLECTION_NAME = "products";
	private static final String ESHOP_DB_NAME = "eShop";
	private static final String CART_COLLECTION_NAME = "cart";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static GenericContainer mongo = new GenericContainer("mongo:4.4.3").withExposedPorts(27017)
			.withCommand("--replSet rs0");

	private MongoClient client;

	@Spy
	private EShopView eShopView;

	private EShopController eShopController;
	private ProductRepository productRepository;
	private CartRepository cartRepository;
	private AutoCloseable closeable;
	private List<Product> catalog;
	private ShopManager shopManager;
	private TransactionManager transactionManager;

	@BeforeClass
	public static void mongoConfiguration() {
		// configure replica set in MongoDB with TestContainers
		try {
			mongo.execInContainer("/bin/bash", "-c", "mongo --eval 'printjson(rs.initiate())' " + "--quiet");
			// '> /dev/null 2>&1' has the effect of silencing the command by throwing away all of its output
			mongo.execInContainer("/bin/bash", "-c",
					"until mongo --eval \"printjson(rs.isMaster())\" | grep ismaster | grep true > /dev/null 2>&1;"
							+ "do sleep 1;done");
		} catch (Exception e) {
			throw new IllegalStateException("Failed to initiate rs.", e);
		}
	}

	@Before
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		ClientSession session = client.startSession();
		closeable = MockitoAnnotations.openMocks(this);
		catalog = asList(
			new Product("1", "Laptop", 1300),
			new Product("2", "Iphone", 1000),
			new Product("3", "Cuffie", 300),
			new Product("4", "Lavatrice", 300)
		);
		transactionManager = new TransactionalShopManager(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME,
				CART_COLLECTION_NAME);
		shopManager = new ShopManager(transactionManager);
		productRepository = new ProductMongoRepository(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME, session);
		cartRepository = new CartMongoRepository(client, ESHOP_DB_NAME, CART_COLLECTION_NAME, session);
		// set initial state of the database through the repository
		productRepository.loadCatalog(catalog);
		eShopController = new EShopController(eShopView, shopManager);
		shopManager.setShopController(eShopController);
	}

	@After
	public void tearDown() throws Exception {
		client.close();
		closeable.close();
	}

	@Test
	public void testAllProducts() {
		eShopController.allProducts();
		then(eShopView).should().showAllProducts(catalog);
	}

	@Test
	public void testSearchProducts() {
		eShopController.searchProducts("la");
		then(eShopView).should().showSearchedProducts(asList(
			new Product("1", "Laptop", 1300),
			new Product("4", "Lavatrice", 300))
		);
	}

	@Test
	public void testResetSearch() {
		eShopController.resetSearch();
		then(eShopView).should().clearSearch(catalog);
	}

	@Test
	public void testNewCartProduct() {
		Product product = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		cartRepository.addToCart(product2);
		eShopController.newCartProduct(product);
		assertThat(cartRepository.allCart()).containsExactly(product2, product);
		InOrder inOrder = inOrder(eShopView);
		then(eShopView).should(inOrder).addToCartView(asList(product2, product));
		then(eShopView).should(inOrder).showTotalCost(product.getPrice() + product2.getPrice());
	}

	@Test
	public void testRemoveCartProduct() {
		Product product = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		cartRepository.addToCart(product);
		cartRepository.addToCart(product2);
		eShopController.removeCartProduct(product);
		assertThat(cartRepository.allCart()).containsExactly(product2);
		InOrder inOrder = inOrder(eShopView);
		then(eShopView).should(inOrder).removeFromCartView(product);
		then(eShopView).should(inOrder).showTotalCost(product2.getPrice());
	}

	@Test
	public void testCheckoutCartWhenSuccessfull() {
		Product product = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		cartRepository.addToCart(product);
		cartRepository.addToCart(product2);
		eShopController.checkoutCart();
		InOrder inOrder = inOrder(eShopView);
		then(eShopView).should(inOrder).showSuccessLabel();
		then(eShopView).should(inOrder).clearCart();
		then(eShopView).should(inOrder).resetTotalCost();
		assertThat(cartRepository.allCart()).isEmpty();
		assertThat(productRepository.findAll()).containsExactly(
				new Product("1", "Laptop", 1300, 0),
				new Product("2", "Iphone", 1000, 0),
				new Product("3", "Cuffie", 300),
				new Product("4", "Lavatrice", 300)
		);
	}

	@Test
	public void testCheckoutCartWhenTheCheckoutFails() {
		Product product = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		cartRepository.addToCart(product);
		cartRepository.addToCart(product2);
		cartRepository.addToCart(product2);
		eShopController.checkoutCart();
		then(eShopView).should().showFailureLabel(product2);
		verifyNoMoreInteractions(eShopView);
		assertThat(cartRepository.allCart()).contains(
				new Product("1", "Laptop", 1300),
				new Product("2", "Iphone", 1000, 2)
		);
		assertThat(productRepository.findAll()).containsExactly(
				new Product("1", "Laptop", 1300, 1),
				new Product("2", "Iphone", 1000, 1),
				new Product("3", "Cuffie", 300, 1),
				new Product("4", "Lavatrice", 300, 1)
		);
	}

	@Test
	public void testShowCart() {
		Product product = new Product("1", "Laptop", 1300);
		cartRepository.addToCart(product);
		eShopController.showCart();
		then(eShopView).should().showAllCart(asList(product));
	}

	@Test
	public void testShowCartCost() {
		Product product = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		cartRepository.addToCart(product);
		cartRepository.addToCart(product2);
		cartRepository.addToCart(product2);
		eShopController.showCartCost();
		then(eShopView).should().showTotalCost(3300.0);
	}
}
