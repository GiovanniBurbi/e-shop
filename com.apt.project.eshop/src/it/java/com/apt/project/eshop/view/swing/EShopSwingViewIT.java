package com.apt.project.eshop.view.swing;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

@RunWith(GUITestRunner.class)
public class EShopSwingViewIT extends AssertJSwingJUnitTestCase {
	
	private static final String PRODUCTS_COLLECTION_NAME = "products";
	private static final String ESHOP_DB_NAME = "eShop";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongo = new GenericContainer("mongo:4.4.3").withExposedPorts(27017);
	
	private MongoClient client;
	private ProductMongoRepository productRepository;
	private EShopSwingView eShopSwingView;
	private EShopController eShopController;
	private FrameFixture window;
	private List<Product> catalog;


	@Override
	protected void onSetUp() throws Exception {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
		catalog = asList(
				new Product("1", "Laptop", 1300),
				new Product("2", "Iphone", 1000),
				new Product("3", "Cuffie", 300),
				new Product("4", "Lavatrice", 300)
			);
		productRepository = new ProductMongoRepository(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME);
		// make sure to start with the initial configuration
		productRepository.loadCatalog(catalog);
		GuiActionRunner.execute(() -> {
			eShopSwingView = new EShopSwingView();
			eShopController = new EShopController(productRepository, eShopSwingView);
			return eShopSwingView;
		});
		window = new FrameFixture(robot(), eShopSwingView);
		window.show(); // shows the frame to test
		
	}
	
	@Override
	protected void onTearDown() {
		client.close();
	}

	@Test @GUITest
	public void testAllProducts() {
		GuiActionRunner.execute(() -> eShopController.allProducts());
		// verify that the view's list is populated
		assertThat(window.list("productList").contents()).containsExactly(
				new Product("1", "Laptop", 1300).toString(),
				new Product("2", "Iphone", 1000).toString(),
				new Product("3", "Cuffie", 300).toString(),
				new Product("4", "Lavatrice", 300).toString()
		);
	}
	
	@Test @GUITest
	public void testSearchProducts() {
		GuiActionRunner.execute(() -> {
			eShopController.allProducts();
			eShopController.searchProducts("la");		
		});
		assertThat(window.list("productList").contents()).containsExactly(
				new Product("1", "Laptop", 1300).toString(),
				new Product("4", "Lavatrice", 300).toString()
		);
	}
	
	@Test @GUITest
	public void testProductNotFoundError() {
		GuiActionRunner.execute(() -> {
			eShopController.allProducts();
			eShopController.searchProducts("samsung");		
		});
		assertThat(window.list("productList").contents()).containsExactly(
				new Product("1", "Laptop", 1300).toString(),
				new Product("2", "Iphone", 1000).toString(),
				new Product("3", "Cuffie", 300).toString(),
				new Product("4", "Lavatrice", 300).toString()
		);
		window.label("errorMessageLabel").requireText(
				"Nessun risultato trovato per: \"samsung\""		
		);
	}
}
