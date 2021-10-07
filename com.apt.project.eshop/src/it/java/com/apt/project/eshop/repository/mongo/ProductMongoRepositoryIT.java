package com.apt.project.eshop.repository.mongo;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
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
	private MongoCollection<Product> productCollection;
	private MongoCollection<Product> cartCollection;
	
	@Before
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		productRepository = new ProductMongoRepository(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(ESHOP_DB_NAME);
		// start with clean database
		database.drop();
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		productCollection = database.getCollection(PRODUCTS_COLLECTION_NAME, Product.class).withCodecRegistry(pojoCodecRegistry);
		cartCollection = database.getCollection("cart", Product.class).withCodecRegistry(pojoCodecRegistry);
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
		productCollection.insertOne(new Product("1", "Laptop", 1300));
		assertThat(productRepository.findAll()).containsExactly(new Product("1", "Laptop", 1300));
	}
	
	@Test
	public void testLoadCatalogWhenDatabaseIsEmpty() {
		productRepository.loadCatalog(asList(new Product("1", "Laptop", 1300)));
		assertThat(productCollection.find()).containsExactly(new Product("1", "Laptop", 1300));
	}
	
	@Test
	public void testLoadCatalogWhenDatabaseIsNotEmpty() {
		productCollection.insertOne(new Product("2", "Iphone", 1000));
		productRepository.loadCatalog(asList(new Product("1", "Laptop", 1300)));
		assertThat(productCollection.find()).containsExactly(new Product("1", "Laptop", 1300));
	}
	
	@Test
	public void testLoadCatalogWhenCatalogHasFewProducts() {
		productRepository.loadCatalog(asList(new Product("1", "Laptop", 1300), new Product("2", "Iphone", 1000)));
		assertThat(productCollection.find()).containsExactlyInAnyOrder(new Product("1", "Laptop", 1300), new Product("2", "Iphone", 1000));
	}
	
	@Test
	public void testFindByNameWhenDatabaseIsEmpty() {
		assertThat(productRepository.findByName("Laptop")).isEmpty();
	}
	
	@Test
	public void testFindByNameShouldShowIntheListOnlyProductsThatContainTheNameSearched () {
		productCollection.insertOne(new Product("1", "Laptop", 1300));
		productCollection.insertOne(new Product("2", "Iphone", 1000));
		productCollection.insertOne(new Product("3", "Lavatrice", 400));
		assertThat(productRepository.findByName("La")).containsExactlyInAnyOrder(
			new Product("1", "Laptop", 1300),
			new Product("3", "Lavatrice", 400)
		);
	}
	
	@Test
	public void testFindByNameWhenSearchedNameIsLowerCaseShouldShowIntheListOnlyProductsThatContainTheNameSearchedIgnoringTheCase () {
		productCollection.insertOne(new Product("1", "Laptop", 1300));
		productCollection.insertOne(new Product("2", "Iphone", 1000));
		productCollection.insertOne(new Product("3", "Lavatrice", 400));
		assertThat(productRepository.findByName("la")).containsExactlyInAnyOrder(
			new Product("1", "Laptop", 1300),
			new Product("3", "Lavatrice", 400)
		);
	}
	
	@Test
	public void testAddToCart() {
		Product product = new Product("1", "eBook", 300);
		productRepository.addToCart(product);
		assertThat(cartCollection.find()).containsExactly(new Product("1", "eBook", 300));
	}
	
	@Test
	public void testAddToCartWhenTheUserAddTwoTimesTheSameProduct() {
		Product product = new Product("1", "eBook", 300);
		cartCollection.insertOne(product);
		Product secondProduct = new Product("1", "eBook", 300);
		productRepository.addToCart(secondProduct);
		assertThat(cartCollection.find()).containsExactly(new Product("1", "eBook", 300, 2));
	}
	
	@Test
	public void testAllCartWhenCartCollectionIsEmpty() {
		assertThat(productRepository.allCart()).isEmpty();
	}
	
	@Test
	public void testAllCartWhenDatabaseIsNotEmpty() {
		Product product1 = new Product("1", "Laptop", 1300, 4);
		Product product2 = new Product("2", "eBook", 300, 3);
		cartCollection.insertOne(product1);
		cartCollection.insertOne(product2);
		assertThat(productRepository.allCart()).containsExactly(product1, product2);
	}
	
}
