package com.apt.project.eshop.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static com.mongodb.client.model.Aggregates.lookup;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.CartItem;
import com.apt.project.eshop.repository.CatalogItem;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class CartMongoRepositoryIT {

	private static final String PRODUCT_COLLECTION_NAME = "products";
	private static final String CART_COLLECTION_NAME = "cart";
	private static final String ESHOP_DB_NAME = "eShop";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static GenericContainer mongo = new GenericContainer("mongo:4.4.3").withExposedPorts(27017)
			.withCommand("--replSet rs0");
	private MongoClient client;
	private CartMongoRepository cartRepository;
	private MongoCollection<Document> cartCollection;
	private ProductMongoRepository productRepository;
	private ClientSession session;

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
		session = client.startSession();
		MongoDatabase database = client.getDatabase(ESHOP_DB_NAME);
		cartRepository = new CartMongoRepository(client, ESHOP_DB_NAME, CART_COLLECTION_NAME, PRODUCT_COLLECTION_NAME, session);
		productRepository = new ProductMongoRepository(client, ESHOP_DB_NAME, PRODUCT_COLLECTION_NAME, session);
		// start with a default database
		productRepository.loadCatalog(asList(
				new CatalogItem(new Product("1", "Laptop", 1300), 2),
				new CatalogItem(new Product("2", "eBook", 300), 2)				
		));
		cartCollection = database.getCollection(CART_COLLECTION_NAME);
	}

	@After
	public void tearDown() {
		client.close();
	}

	@Test
	public void testAddToCart() {
		Product product = new Product("1", "Laptop", 1300);
		cartRepository.addToCart(product);
		// retrive all documents in cartCollection without field _id
		assertThat(cartCollection.find(session).projection(excludeId())).containsExactly(new Document()
				.append("productId", product.getId())
				.append("quantity", 1)
		);
	}

	@Test
	public void testAddToCartWhenTheUserAddTwoTimesTheSameProduct() {
		Product product = new Product("1", "Laptop", 1300);
		addTestItemToDatabase(product.getId(), 1);
		cartRepository.addToCart(product);
		assertThat(cartCollection.find(session).projection(excludeId())).containsExactly(new Document()
				.append("productId", product.getId())
				.append("quantity", 2)
		);
	}

	@Test
	public void testAllCartWhenCartCollectionIsEmpty() {
		assertThat(cartRepository.allCart()).isEmpty();
	}

	@Test
	public void testAllCartWhenCartCollectionIsNotEmpty() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "eBook", 300);
		addTestItemToDatabase(product1.getId(), 2);
		addTestItemToDatabase(product2.getId(), 3);
		assertThat(cartRepository.allCart()).containsExactly(
				new CartItem(product1, 2),
				new CartItem(product2, 3)
		);
	}

	@Test
	public void testRemoveFromCart() {
		Product product = new Product("1", "Laptop", 1300, 4);
		Product product2 = new Product("2", "eBook", 300, 3);
		cartCollection.insertOne(product);
		cartCollection.insertOne(product2);
		cartRepository.removeFromCart(product);
		assertThat(cartCollection.find()).containsExactly(product2);
	}

	@Test
	public void testCartTotalCostWhenCartIsEmpty() {
		assertThat(cartRepository.cartTotalCost()).isEqualTo(0.0);
	}

	@Test
	public void testCartTotalCost() {
		Product product1 = new Product("1", "Laptop", 1300, 4);
		Product product2 = new Product("2", "eBook", 300, 3);
		cartCollection.insertOne(product1);
		cartCollection.insertOne(product2);
		assertThat(cartRepository.cartTotalCost()).isEqualTo(6100);
	}
	
	private void addTestItemToDatabase(String id, int quantity) {
		cartCollection.insertOne(
				new Document()
					.append("product", id)
					.append("quantity", quantity)
		);
	}
}
