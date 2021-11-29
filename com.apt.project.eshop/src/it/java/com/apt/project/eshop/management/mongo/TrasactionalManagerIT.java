package com.apt.project.eshop.management.mongo;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.management.ShopManager;
import com.apt.project.eshop.management.TransactionManager;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.CartItem;
import com.apt.project.eshop.repository.CartRepository;
import com.apt.project.eshop.repository.CatalogItem;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.repository.mongo.CartMongoRepository;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.apt.project.eshop.view.EShopView;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ClientSession;

public class TrasactionalManagerIT {

	private static final String PRODUCTS_COLLECTION_NAME = "products";
	private static final String ESHOP_DB_NAME = "eShop";
	private static final String CART_COLLECTION_NAME = "cart";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static GenericContainer mongo = new GenericContainer("mongo:4.4.3").withExposedPorts(27017)
			.withCommand("--replSet rs0");

	private MongoClient client;
	private ShopManager shopManager;
	private TransactionManager transactionManager;
	private ProductRepository productRepository;
	private CartRepository cartRepository;
	private EShopController shopController;
	@Mock
	private EShopView shopView;
	private AutoCloseable closeable;
	private List<CatalogItem> catalog;

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
		closeable = MockitoAnnotations.openMocks(this);
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		ClientSession session = client.startSession();
		catalog = asList(
			new CatalogItem(new Product("1", "Laptop", 1300), 2),
			new CatalogItem(new Product("2", "Iphone", 1000), 2),
			new CatalogItem(new Product("3", "Cuffie", 300), 2),
			new CatalogItem(new Product("4", "Lavatrice", 300), 2)
		);
		productRepository = new ProductMongoRepository(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME, session);
		cartRepository = new CartMongoRepository(client, ESHOP_DB_NAME, CART_COLLECTION_NAME, PRODUCTS_COLLECTION_NAME, session);
		// set initial state of the database through the repository
		productRepository.loadCatalog(catalog);
		transactionManager = new TransactionalShopManager(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME,
				CART_COLLECTION_NAME);
		shopManager = new ShopManager(transactionManager);
		shopController = new EShopController(shopView, shopManager);
		shopManager.setShopController(shopController);
	}

	@After
	public void tearDown() throws Exception {
		client.close();
		closeable.close();
	}

	@Test
	public void testCheckoutSuccess() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		cartRepository.addToCart(product1);
		cartRepository.addToCart(product2);
		cartRepository.addToCart(product2);
		shopManager.checkout();
		then(shopView).should().showSuccessLabel();
		assertThat(cartRepository.allCart()).isEmpty();
		CatalogItem product1RepositoryQuery = productRepository.findByName("Laptop").get(0);
		assertThat(product1RepositoryQuery.getStorage()).isOne();
		CatalogItem product2RepositoryQuery = productRepository.findByName("Iphone").get(0);
		assertThat(product2RepositoryQuery.getStorage()).isZero();
	}

	@Test
	public void testCheckoutWhenAProductIsOutOfStockShouldNotSellAnyProductsInTheCart() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		Product product3 = new Product("3", "Cuffie", 300);
		cartRepository.addToCart(product1);
		cartRepository.addToCart(product2);
		cartRepository.addToCart(product2);
		cartRepository.addToCart(product2);
		cartRepository.addToCart(product3);
		shopManager.checkout();
		then(shopView).should().showFailureLabel(new CatalogItem(product2, 2));
		assertThat(cartRepository.allCart()).containsExactly(
			new CartItem(new Product("1", "Laptop", 1300), 1),
			new CartItem(new Product("2", "Iphone", 1000), 3),
			new CartItem(new Product("3", "Cuffie", 300), 1)
		);
		CatalogItem product1RepositoryQuery = productRepository.findByName("Laptop").get(0);
		assertThat(product1RepositoryQuery.getStorage()).isEqualTo(2);
		CatalogItem product2RepositoryQuery = productRepository.findByName("Iphone").get(0);
		assertThat(product2RepositoryQuery.getStorage()).isEqualTo(2);
		CatalogItem product3RepositoryQuery = productRepository.findByName("Cuffie").get(0);
		assertThat(product3RepositoryQuery.getStorage()).isEqualTo(2);
	}
}
