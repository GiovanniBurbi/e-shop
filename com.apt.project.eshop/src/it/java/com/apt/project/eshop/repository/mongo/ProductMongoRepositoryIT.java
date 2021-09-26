package com.apt.project.eshop.repository.mongo;


import static org.assertj.core.api.Assertions.assertThat;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.model.Product;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ProductMongoRepositoryIT {
	
	private static final String PRODUCTS_COLLECTION_NAME = "products";
	private static final String ESHOP_DB_NAME = "eShop";
	
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongo = new GenericContainer("mongo:4.4.3").withExposedPorts(27017);

	private MongoClient client;
	private ProductMongoRepository productRepository;
	private MongoCollection<Document> productCollection;
	
	@Before
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		productRepository = new ProductMongoRepository(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(ESHOP_DB_NAME);
		// start with clean database
		database.drop();
		productCollection = database.getCollection(PRODUCTS_COLLECTION_NAME);
	}
	@After
	public void tearDown() {
		client.close();
	}
	
	@Test
	public void testFindAllWhenDatabaseIsEmpty() {
		assertThat(productRepository.findAll()).isEmpty();
	}
	
	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() {
		addTestProductToTheDatabase("1", "Laptop", 1300);
		
		assertThat(productRepository.findAll()).containsExactly(new Product("1", "Laptop", 1300));
	}
	private void addTestProductToTheDatabase(String id, String name, double price) {
		productCollection.insertOne(new Document().append("id", id).append("name", name).append("price", price));
	}
}