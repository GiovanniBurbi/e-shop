package com.apt.project.eshop.repository.mongo;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.RepositoryException;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class ProductMongoRepositoryIT {

	private static final String PRODUCTS_COLLECTION_NAME = "products";
	private static final String ESHOP_DB_NAME = "eShop";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static GenericContainer mongo = new GenericContainer("mongo:4.4.3").withExposedPorts(27017)
			.withCommand("--replSet rs0");
	private MongoClient client;
	private ProductMongoRepository productRepository;
	private MongoCollection<Product> productCollection;

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
		productRepository = new ProductMongoRepository(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME, session);
		MongoDatabase database = client.getDatabase(ESHOP_DB_NAME);
		// start with clean database
		database.drop();
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
				fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		productCollection = database.getCollection(PRODUCTS_COLLECTION_NAME, Product.class)
				.withCodecRegistry(pojoCodecRegistry);
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
		assertThat(productCollection.find()).containsExactlyInAnyOrder(new Product("1", "Laptop", 1300),
				new Product("2", "Iphone", 1000));
	}

	@Test
	public void testFindByNameWhenDatabaseIsEmpty() {
		assertThat(productRepository.findByName("Laptop")).isEmpty();
	}

	@Test
	public void testFindByNameShouldShowIntheListOnlyProductsThatContainTheNameSearched() {
		productCollection.insertOne(new Product("1", "Laptop", 1300));
		productCollection.insertOne(new Product("2", "Iphone", 1000));
		productCollection.insertOne(new Product("3", "Lavatrice", 400));
		assertThat(productRepository.findByName("La")).containsExactlyInAnyOrder(new Product("1", "Laptop", 1300),
				new Product("3", "Lavatrice", 400));
	}

	@Test
	public void testFindByNameWhenSearchedNameIsLowerCaseShouldShowIntheListOnlyProductsThatContainTheNameSearchedIgnoringTheCase() {
		productCollection.insertOne(new Product("1", "Laptop", 1300));
		productCollection.insertOne(new Product("2", "Iphone", 1000));
		productCollection.insertOne(new Product("3", "Lavatrice", 400));
		assertThat(productRepository.findByName("la")).containsExactlyInAnyOrder(new Product("1", "Laptop", 1300),
				new Product("3", "Lavatrice", 400));
	}

	@Test
	public void testRemoveFromStorageWhenThereIsEnoughStorage() {
		Product product = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "eBook", 300);
		productCollection.insertOne(product);
		productCollection.insertOne(product2);
		try {
			productRepository.removeFromStorage(product);
		} catch (RepositoryException e) {
			fail("Should not throw an exception in this test case!");
		}
		assertThat(productCollection.find()).containsExactly(product, product2);
		Bson filterNameProduct = Filters.eq("name", product.getName());
		assertThat(productCollection.find(filterNameProduct).first().getQuantity()).isZero();
	}

	@Test
	public void testRemoveFromStorageWhenThereIsNotEnoughStorageShouldThrowRepositoryException() {
		Product product = new Product("1", "Laptop", 1300, 1);
		Product product2 = new Product("2", "eBook", 300, 1);
		productCollection.insertOne(product);
		productCollection.insertOne(product2);
		Product productNotAvailable = new Product("1", "Laptop", 1300, 2);
		assertThatThrownBy(() -> productRepository.removeFromStorage(productNotAvailable))
				.isInstanceOf(RepositoryException.class)
				.hasMessage("Repository exception! Insufficient stock, Laptop left in stock: 1");
		assertThat(productCollection.find()).containsExactly(product, product2);
		Bson filterNameProduct = Filters.eq("name", productNotAvailable.getName());
		assertThat(productCollection.find(filterNameProduct).first().getQuantity()).isOne();
	}
}
