package com.apt.project.eshop.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.apt.project.eshop.view.EShopView;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class EShopControllerIT {

	private static final String PRODUCTS_COLLECTION_NAME = "products";
	private static final String ESHOP_DB_NAME = "eShop";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongo = new GenericContainer("mongo:4.4.3").withExposedPorts(27017);
	
	private MongoClient client;
	
	@Spy
	private EShopView eShopView;
	
	private EShopController eShopController;
	private ProductRepository productRepository;
	
	private AutoCloseable closeable;
	private List<Product> catalog;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		catalog = asList(
				new Product("1", "Laptop", 1300),
				new Product("2", "Iphone", 1000),
				new Product("3", "Cuffie", 300),
				new Product("4", "Lavatrice", 300)
			);
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		productRepository = new ProductMongoRepository(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME);
		// set initial state of the database through the repository
		productRepository.loadCatalog(catalog);
		eShopController = new EShopController(productRepository, eShopView);
	}

	@After
	public void tearDown() throws Exception {
		client.close();
		closeable.close();
	}

	@Test
	public void testAllProducts() {
		eShopController.allProducts();
		verify(eShopView).showAllProducts(catalog);
	}
	
	@Test
	public void testSearchProducts() {
		eShopController.searchProducts("la");
		verify(eShopView).showSearchedProducts(asList(
				new Product("1", "Laptop", 1300),
				new Product("4", "Lavatrice", 300)
		));
	}
}

