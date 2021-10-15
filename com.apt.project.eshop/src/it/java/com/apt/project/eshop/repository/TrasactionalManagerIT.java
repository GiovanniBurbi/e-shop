package com.apt.project.eshop.repository;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class TrasactionalManagerIT {

	private static final String PRODUCTS_COLLECTION_NAME = "products";
	private static final String ESHOP_DB_NAME = "eShop";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongo = new GenericContainer("mongo:4.4.3").withExposedPorts(27017);

	private MongoClient client;

	private ShopManager shopManager;

	private TransactionManager transactionManager;

	private ProductRepository productRepository;
	private List<Product> catalog;

	@Before
	public void setup() {
		catalog = asList(
				new Product("1", "Laptop", 1300, 2),
				new Product("2", "Iphone", 1000, 2),
				new Product("3", "Cuffie", 300, 2),
				new Product("4", "Lavatrice", 300, 2)
		);
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		productRepository = new ProductMongoRepository(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME);
		// set initial state of the database through the repository
		productRepository.loadCatalog(catalog);
		transactionManager = new TransactionalShopManager(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME);
		shopManager = new ShopManager(transactionManager);
	}

	@After
	public void tearDown() throws Exception {
		client.close();
	}

	@Test
	public void testCheckoutSuccess() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		productRepository.addToCart(product1);
		productRepository.addToCart(product2);
		productRepository.addToCart(product2);
		shopManager.checkout();
		assertThat(productRepository.allCart()).isEmpty();
		Product product1RepositoryQuery = productRepository.findByName("Laptop").get(0);
		assertThat(product1RepositoryQuery.getQuantity()).isEqualTo(1);
		Product product2RepositoryQuery = productRepository.findByName("Iphone").get(0);
		assertThat(product2RepositoryQuery.getQuantity()).isZero();
	}

}