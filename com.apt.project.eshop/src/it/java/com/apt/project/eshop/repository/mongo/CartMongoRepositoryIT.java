package com.apt.project.eshop.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.model.Product;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class CartMongoRepositoryIT {
	
	private static final String CART_COLLECTION_NAME = "cart";
	private static final String ESHOP_DB_NAME = "eShop";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static GenericContainer mongo = new GenericContainer("mongo:4.4.3").withExposedPorts(27017)
			.withCommand("--replSet rs0");
	private MongoClient client;
	private CartMongoRepository cartRepository;
	private MongoCollection<Product> cartCollection;

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
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		MongoDatabase database = client.getDatabase(ESHOP_DB_NAME);
		cartRepository = new CartMongoRepository(client, ESHOP_DB_NAME, CART_COLLECTION_NAME);
		// start with clean database
		database.drop();
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
				fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		cartCollection = database.getCollection(CART_COLLECTION_NAME, Product.class).withCodecRegistry(pojoCodecRegistry);
	}

	@After
	public void tearDown() {
		client.close();
	}
	
	@Test
	public void testAddToCart() {
		Product product = new Product("1", "eBook", 300);
		cartRepository.addToCart(product);
		assertThat(cartCollection.find()).containsExactly(new Product("1", "eBook", 300));
	}

	@Test
	public void testAddToCartWhenTheUserAddTwoTimesTheSameProduct() {
		Product product = new Product("1", "eBook", 300);
		cartCollection.insertOne(product);
		Product secondProduct = new Product("1", "eBook", 300);
		cartRepository.addToCart(secondProduct);
		assertThat(cartCollection.find()).containsExactly(new Product("1", "eBook", 300, 2));
	}
	
	@Test
	public void testAllCartWhenCartCollectionIsEmpty() {
		assertThat(cartRepository.allCart()).isEmpty();
	}

	@Test
	public void testAllCartWhenCartCollectionIsNotEmpty() {
		Product product1 = new Product("1", "Laptop", 1300, 4);
		Product product2 = new Product("2", "eBook", 300, 3);
		cartCollection.insertOne(product1);
		cartCollection.insertOne(product2);
		assertThat(cartRepository.allCart()).containsExactly(product1, product2);
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
}
