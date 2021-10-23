package com.apt.project.eshop.repository;

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
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ClientSession;

public class TrasactionalManagerIT {

	private static final String PRODUCTS_COLLECTION_NAME = "products";
	private static final String ESHOP_DB_NAME = "eShop";

	@SuppressWarnings("rawtypes")
	@ClassRule
    public static GenericContainer mongo = new GenericContainer("mongo:4.4.3")
            .withExposedPorts(27017)
            .withCommand("--replSet rs0");
	private MongoClient client;

	private ShopManager shopManager;

	private TransactionManager transactionManager;

	private ProductRepository productRepository;
	
	@Mock
	private EShopController shopController;
	
	private AutoCloseable closeable;
	private List<Product> catalog;
	
	@BeforeClass
	public static void mongoConfiguration() {
		// configure replica set in MongoDB with TestContainers
		try {
			mongo.execInContainer("/bin/bash", "-c", "mongo --eval 'printjson(rs.initiate())' " + "--quiet");
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
				new Product("1", "Laptop", 1300, 2),
				new Product("2", "Iphone", 1000, 2),
				new Product("3", "Cuffie", 300, 2),
				new Product("4", "Lavatrice", 300, 2)
		);
		productRepository = new ProductMongoRepository(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME, session);
		// set initial state of the database through the repository
		productRepository.loadCatalog(catalog);
		transactionManager = new TransactionalShopManager(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME);
		shopManager = new ShopManager(transactionManager);
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
		productRepository.addToCart(product1);
		productRepository.addToCart(product2);
		productRepository.addToCart(product2);
		shopManager.checkout();
		then(shopController).should().checkoutSuccess();
		assertThat(productRepository.allCart()).isEmpty();
		Product product1RepositoryQuery = productRepository.findByName("Laptop").get(0);
		assertThat(product1RepositoryQuery.getQuantity()).isEqualTo(1);
		Product product2RepositoryQuery = productRepository.findByName("Iphone").get(0);
		assertThat(product2RepositoryQuery.getQuantity()).isZero();
	}
		
	@Test
	public void testCheckoutWhenAProductIsOutOfStockShouldNotSellAnyProductsInTheCart() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		Product product3 = new Product("3", "Cuffie", 300);
		productRepository.addToCart(product1);
		productRepository.addToCart(product2);
		productRepository.addToCart(product2);
		productRepository.addToCart(product2);
		productRepository.addToCart(product3);
		shopManager.checkout();
		then(shopController).should().checkoutFailure(product2);
		assertThat(productRepository.allCart()).containsExactly(new Product("1", "Laptop", 1300, 1), new Product("2", "Iphone", 1000, 3),  new Product("3", "Cuffie", 300, 1));
		Product product1RepositoryQuery = productRepository.findByName("Laptop").get(0);
		assertThat(product1RepositoryQuery.getQuantity()).isEqualTo(2);
		Product product2RepositoryQuery = productRepository.findByName("Iphone").get(0);
		assertThat(product2RepositoryQuery.getQuantity()).isEqualTo(2);
		Product product3RepositoryQuery = productRepository.findByName("Cuffie").get(0);
		assertThat(product3RepositoryQuery.getQuantity()).isEqualTo(2);
	}
}
