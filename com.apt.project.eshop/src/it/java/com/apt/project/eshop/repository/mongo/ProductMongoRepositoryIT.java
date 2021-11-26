package com.apt.project.eshop.repository.mongo;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

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
import com.apt.project.eshop.repository.CatalogItem;
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
	private MongoCollection<Document> productCollection;

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
		addTestItemToDatabase("1", "Laptop", 1300.0, 1);
		assertThat(productRepository.findAll()).containsExactly(new Product("1", "Laptop", 1300));
	}

	@Test
	public void testLoadCatalogWhenDatabaseIsEmpty() {
		CatalogItem item = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		productRepository.loadCatalog(asList(item));
		assertThat(readAllCatalogFromDatabase()).containsExactly(item);
	}

	@Test
	public void testLoadCatalogWhenDatabaseIsNotEmpty() {
		CatalogItem item = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		addTestItemToDatabase("2", "Iphone", 1000.0, 1);
		productRepository.loadCatalog(asList(item));
		assertThat(readAllCatalogFromDatabase()).containsExactly(item);
	}

	@Test
	public void testLoadCatalogWhenCatalogHasFewProducts() {
		CatalogItem item1 = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		CatalogItem item2 = new CatalogItem(new Product("2", "Iphone", 1000), 1);
		productRepository.loadCatalog(asList(item1, item2));
		assertThat(readAllCatalogFromDatabase()).containsExactly(item1, item2);
	}

	@Test
	public void testFindByNameWhenDatabaseIsEmpty() {
		assertThat(productRepository.findByName("Laptop")).isEmpty();
	}

	@Test
	public void testFindByNameShouldShowIntheListOnlyProductsThatContainTheNameSearched() {
		addTestItemToDatabase("1", "Laptop", 1300.0, 1);
		addTestItemToDatabase("2", "Iphone", 1000.0, 1);
		addTestItemToDatabase("3", "Lavatrice", 400.0, 1);
		assertThat(productRepository.findByName("La")).containsExactlyInAnyOrder(new Product("1", "Laptop", 1300),
				new Product("3", "Lavatrice", 400));
	}

	@Test
	public void testFindByNameWhenSearchedNameIsLowerCaseShouldShowIntheListOnlyProductsThatContainTheNameSearchedIgnoringTheCase() {
		addTestItemToDatabase("1", "Laptop", 1300.0, 1);
		addTestItemToDatabase("2", "Iphone", 1000.0, 1);
		addTestItemToDatabase("3", "Lavatrice", 400.0, 1);
		assertThat(productRepository.findByName("la")).containsExactlyInAnyOrder(new Product("1", "Laptop", 1300),
				new Product("3", "Lavatrice", 400));
	}

	@Test
	public void testRemoveFromStorageWhenThereIsEnoughStorage() {
		Product product = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "eBook", 300);
		addTestItemToDatabase("1", "Laptop", 1300.0, 1);
		addTestItemToDatabase("2", "eBook", 300.0, 1);
		try {
			productRepository.removeFromStorage(product);
		} catch (RepositoryException e) {
			fail("Should not throw an exception in this test case!");
		}
		// assert that the product removed has its storage field updated in the product collection
		assertThat(readAllCatalogFromDatabase()).containsExactly(new CatalogItem(product, 0), new CatalogItem(product2, 1));
	}

	@Test
	public void testRemoveFromStorageWhenThereIsNotEnoughStorageShouldThrowRepositoryException() {
		Product product = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "eBook", 300);
		addTestItemToDatabase("1", "Laptop", 1300.0, 1);
		addTestItemToDatabase("2", "eBook", 300.0, 1);
		CatalogItem catalogItemNotAvailable = new CatalogItem(product, 2);
		assertThatThrownBy(() -> productRepository.removeFromStorage(productNotAvailable))
				.isInstanceOf(RepositoryException.class)
				.hasMessage("Repository exception! Insufficient stock, Laptop left in stock: 1");
		// assert that the product with not enough storage has not its storage field updated in the product collection
		assertThat(readAllCatalogFromDatabase()).containsExactly(new CatalogItem(product, 1), new CatalogItem(product2, 1));
	}
	
	private List<CatalogItem> readAllCatalogFromDatabase() {
		return StreamSupport.
			stream(productCollection.find().spliterator(), false)
				.map(d -> new CatalogItem(new Product(""+d.get("id"), ""+d.get("name"), d.getDouble("price")), d.getInteger("storage")))
				.collect(Collectors.toList());
	}
	
	private void addTestItemToDatabase(String id, String name, Double price, int quantity) {
		productCollection.insertOne(
				new Document()
					.append("id", id)
					.append("name", name)
					.append("price", price)
					.append("quantity", quantity)
		);
	}
}
