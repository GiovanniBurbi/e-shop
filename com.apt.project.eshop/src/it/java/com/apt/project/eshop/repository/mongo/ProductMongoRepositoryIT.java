package com.apt.project.eshop.repository.mongo;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.model.CartItem;
import com.apt.project.eshop.model.CatalogItem;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.RepositoryException;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ProductMongoRepositoryIT {

	private static final String PRODUCTS_COLLECTION_NAME = "products";
	private static final String ESHOP_DB_NAME = "eShop";
	
	private static final String PRICE_FIELD_NAME = "price";
	private static final String NAME_FIELD_NAME = "name";
	private static final String ID_FIELD_NAME = "_id";
	private static final String STORAGE_FIELD_NAME = "storage";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static GenericContainer mongo = new GenericContainer("mongo:4.4.3").withExposedPorts(27017)
			.withCommand("--replSet rs0");
	private MongoClient client;
	private ProductMongoRepository productRepository;
	private MongoCollection<Document> productCollection;
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
		assertThat(productRepository.findAll()).containsExactly(new CatalogItem(new Product("1", "Laptop", 1300), 1));
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
		assertThat(productRepository.findByName("La")).containsExactlyInAnyOrder(
				new CatalogItem(new Product("1", "Laptop", 1300), 1),
				new CatalogItem(new Product("3", "Lavatrice", 400), 1));
	}

	@Test
	public void testFindByNameWhenSearchedNameIsLowerCaseShouldShowIntheListOnlyProductsThatContainTheNameSearchedIgnoringTheCase() {
		addTestItemToDatabase("1", "Laptop", 1300.0, 1);
		addTestItemToDatabase("2", "Iphone", 1000.0, 1);
		addTestItemToDatabase("3", "Lavatrice", 400.0, 1);
		assertThat(productRepository.findByName("la")).containsExactlyInAnyOrder(
				new CatalogItem(new Product("1", "Laptop", 1300), 1),
				new CatalogItem (new Product("3", "Lavatrice", 400), 1));
	}

	@Test
	public void testRemoveFromStorageWhenThereIsEnoughStorage() {
		Product product = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "eBook", 300);
		addTestItemToDatabase("1", "Laptop", 1300.0, 1);
		addTestItemToDatabase("2", "eBook", 300.0, 1);
		CartItem itemAvailable = new CartItem(product, 1);
		try {
			productRepository.removeFromStorage(itemAvailable);
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
		CartItem ItemNotAvailable = new CartItem(product, 2);
		assertThatThrownBy(() -> productRepository.removeFromStorage(ItemNotAvailable))
				.isInstanceOf(RepositoryException.class)
				.hasMessage("Repository exception! Insufficient stock, Laptop left in stock: 1");
		// assert that the product with not enough storage has not its storage field updated in the product collection
		assertThat(readAllCatalogFromDatabase()).containsExactly(new CatalogItem(product, 1), new CatalogItem(product2, 1));
	}
	
	private List<CatalogItem> readAllCatalogFromDatabase() {
		return StreamSupport.
			stream(productCollection.find(session).spliterator(), false)
				.map(d -> new CatalogItem(new Product(""+d.get(ID_FIELD_NAME), ""+d.get(NAME_FIELD_NAME), d.getDouble(PRICE_FIELD_NAME)), d.getInteger(STORAGE_FIELD_NAME)))
				.collect(Collectors.toList());
	}
	
	private void addTestItemToDatabase(String id, String name, Double price, int storage) {
		productCollection.insertOne(
				new Document()
					.append(ID_FIELD_NAME, id)
					.append(NAME_FIELD_NAME, name)
					.append(PRICE_FIELD_NAME, price)
					.append(STORAGE_FIELD_NAME, storage)
		);
	}
}
