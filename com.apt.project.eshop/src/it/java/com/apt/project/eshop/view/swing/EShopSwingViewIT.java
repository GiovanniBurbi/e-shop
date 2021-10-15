package com.apt.project.eshop.view.swing;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.ShopManager;
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
	@Mock
	private ShopManager shopManager;
	
	private AutoCloseable closeable;


	@Override
	protected void onSetUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
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
			eShopController = new EShopController(productRepository, eShopSwingView, shopManager);
			eShopSwingView.setEShopController(eShopController);
			return eShopSwingView;
		});
		window = new FrameFixture(robot(), eShopSwingView);
		window.show(); // shows the frame to test
		
	}
	
	@Override
	protected void onTearDown() throws Exception {
		client.close();
		closeable.close();
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
	public void testSearchButtonSuccess() {
		GuiActionRunner.execute(() -> {
			eShopController.allProducts();
		});
		window.textBox("searchTextBox").enterText("la");
		window.button(JButtonMatcher.withText("Search")).click();
		assertThat(window.list("productList").contents()).containsExactly(
				new Product("1", "Laptop", 1300).toString(),
				new Product("4", "Lavatrice", 300).toString()
		);
	}
	
	@Test @GUITest
	public void testSearchBottonProductNotFoundError() {
		GuiActionRunner.execute(() -> {
			eShopController.allProducts();
		});
		window.textBox("searchTextBox").enterText("Samsung");
		window.button(JButtonMatcher.withText("Search")).click();
		assertThat(window.list("productList").contents()).containsExactly(
				new Product("1", "Laptop", 1300).toString(),
				new Product("2", "Iphone", 1000).toString(),
				new Product("3", "Cuffie", 300).toString(),
				new Product("4", "Lavatrice", 300).toString()
		);
		window.label("errorMessageLabel").requireText(
				"Nessun risultato trovato per: \"Samsung\""		
		);
	}
	
	@Test @GUITest
	public void testClearBotton() {
		window.textBox("searchTextBox").enterText("la");
		GuiActionRunner.execute(() -> {
			eShopController.searchProducts("la");
		});
		window.button(JButtonMatcher.withText("Clear")).click();
		assertThat(window.list("productList").contents()).containsExactly(
				new Product("1", "Laptop", 1300).toString(),
				new Product("2", "Iphone", 1000).toString(),
				new Product("3", "Cuffie", 300).toString(),
				new Product("4", "Lavatrice", 300).toString()
		);
		window.textBox("searchTextBox").requireText("");
	}
	
	@Test @GUITest
	public void testClearButtonWhenTheSearchFailWithAllProductsShownInTheListShouldStayDisable() {
		GuiActionRunner.execute(() -> {
			eShopController.allProducts();
			eShopController.searchProducts("Samsung");
		});
		window.button(JButtonMatcher.withText("Clear")).requireDisabled();
	}
	
	@Test @GUITest
	public void testAddToCartButtonShouldShowTheProductSelectedInTheCartAndTheTotalCostOfTheCart() {
		GuiActionRunner.execute(() -> {
			eShopController.allProducts();
		});
		window.list("productList").selectItem(0);
		window.button(JButtonMatcher.withText("Add To Cart")).click();
		assertThat(window.list("cartList").contents()).containsExactly(new Product("1", "Laptop", 1300).toStringExtended());
		window.label("totalCostLabel").requireText("1300.0$");
	}
	
	@Test @GUITest
	public void testAddToCartButtonWhenTheProductSelectedIsAlreadyInTheCartShouldOnlyIncreaseTheFieldQuantityOfThatProductInTheCart() {
		GuiActionRunner.execute(() -> {
			eShopController.allProducts();
			eShopController.newCartProduct(new Product("1", "Laptop", 1300));
			eShopController.newCartProduct(new Product("2", "Iphone", 1000));
		});
		window.list("productList").selectItem(0);
		window.button(JButtonMatcher.withText("Add To Cart")).click();
		assertThat(window.list("cartList").contents())
			.containsExactly(
					new Product("1", "Laptop", 1300, 2).toStringExtended(),
					new Product("2", "Iphone", 1000).toStringExtended()
		);
		window.label("totalCostLabel").requireText("3600.0$");
	}
	
	@Test @GUITest
	public void testRemoveFromCartButtonShouldRemoveTheProductSelectedInTheCart() {
		GuiActionRunner.execute(() -> {
			eShopController.allProducts();
			eShopController.newCartProduct(new Product("1", "Laptop", 1300));
			eShopController.newCartProduct(new Product("2", "Iphone", 1000));			
		});
		window.list("cartList").selectItem(0);
		window.button(JButtonMatcher.withText("Remove From Cart")).click();
		assertThat(window.list("cartList").contents()).containsExactly(new Product("2", "Iphone", 1000).toStringExtended());
		window.label("totalCostLabel").requireText("1000.0$");
	}
	
	@Test @GUITest
	public void testCheckoutButtonWhenCheckoutSuccessfullShouldClearCartAndResetTotalCostLabelAndShowSuccessCheckoutLabel() {
		GuiActionRunner.execute(() -> {
			eShopController.allProducts();
			eShopController.newCartProduct(new Product("1", "Laptop", 1300));
			eShopController.newCartProduct(new Product("2", "Iphone", 1000));	
		});
		window.button(JButtonMatcher.withText("Checkout")).click();
		assertThat(window.list("cartList").contents()).isEmpty();
		window.label("totalCostLabel").requireText("0.0$");
		window.label("checkoutResultLabel").requireText(
			"<html>Thank you for the purchase!!<br/>"
			+ "<br/>You have spent 2300.0$ for the following products:<br/>"
			+ "-- Laptop, quantity:1<br/>"
			+ "-- Iphone, quantity:1<br/></html>"
		);
	}
}
