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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.RepositoryException;
import com.apt.project.eshop.repository.ShopManager;
import com.apt.project.eshop.repository.TransactionManager;
import com.apt.project.eshop.repository.TransactionalShopManager;
import com.apt.project.eshop.repository.mongo.CartMongoRepository;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

@RunWith(GUITestRunner.class)
public class EShopSwingViewIT extends AssertJSwingJUnitTestCase {
	
	private static final String PRODUCTS_COLLECTION_NAME = "products";
	private static final String ESHOP_DB_NAME = "eShop";
	private static final String CART_COLLECTION_NAME = "cart";

	@SuppressWarnings("rawtypes")
	@ClassRule
    public static GenericContainer mongo = new GenericContainer("mongo:4.4.3")
            .withExposedPorts(27017)
            .withCommand("--replSet rs0");
	
	private MongoClient client;
	private ProductMongoRepository productRepository;
	private EShopSwingView eShopSwingView;
	private EShopController eShopController;
	private FrameFixture window;
	private List<Product> catalog;
	private ShopManager shopManager;
	private TransactionManager transactionManager;
	private CartMongoRepository cartRepository;
	
	@BeforeClass
	public static void MongoConfiguration() {
		// configure replica set in MongoDB with TestContainers
		try {
			mongo.execInContainer("/bin/bash", "-c",
					"mongo --eval 'printjson(rs.initiate())' " + "--quiet");
			mongo.execInContainer("/bin/bash", "-c",
					"until mongo --eval \"printjson(rs.isMaster())\" | grep ismaster | grep true > /dev/null 2>&1;"
							+ "do sleep 1;done");
		} catch (Exception e) {
			throw new IllegalStateException("Failed to initiate rs.", e);
		}
	}

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
		cartRepository = new CartMongoRepository(client, ESHOP_DB_NAME, CART_COLLECTION_NAME);
		// make sure to start with the initial configuration
		productRepository.loadCatalog(catalog);
		transactionManager = new TransactionalShopManager(client, ESHOP_DB_NAME, PRODUCTS_COLLECTION_NAME);
		shopManager = new ShopManager(transactionManager);
		GuiActionRunner.execute(() -> {
			eShopSwingView = new EShopSwingView();
			eShopController = new EShopController(productRepository, cartRepository, eShopSwingView, shopManager);
			eShopSwingView.setEShopController(eShopController);
			return eShopSwingView;
		});
		shopManager.setShopController(eShopController);
		window = new FrameFixture(robot(), eShopSwingView);
		window.show(); // shows the frame to test
		
	}
	
	@Override
	protected void onTearDown() throws Exception {
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
	
	@Test @GUITest
	public void testCheckoutButtonWhenCheckoutFailureShouldOnlyShowFailureCheckoutLabel() throws RepositoryException {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		GuiActionRunner.execute(() -> {
			eShopController.allProducts();
			eShopController.newCartProduct(product1);
			eShopController.newCartProduct(product2);	
			eShopController.newCartProduct(product2);	
		});
		window.button(JButtonMatcher.withText("Checkout")).click();
		assertThat(window.list("cartList").contents()).containsExactly(new Product("1", "Laptop", 1300).toStringExtended(), new Product("2", "Iphone", 1000, 2).toStringExtended());
		window.label("totalCostLabel").requireText("3300.0$");
		window.label("checkoutResultLabel").requireText(
			"<html>Error!<br/>"
			+ "<br/>Not enough stock for the following product:<br/>"
			+ "-- Iphone, remaining stock:1<br/>"
			+ "<br/>Remove some products and try again</html>"
		);
	}
	
	@Test @GUITest
	public void testShowAllCart() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		cartRepository.addToCart(product1);
		cartRepository.addToCart(product2);
		GuiActionRunner.execute(() -> {
			eShopController.showCart();	
		});
		assertThat(window.list("cartList").contents()).containsExactly(
				product1.toStringExtended(),
				product2.toStringExtended()
		);
	}
	
	@Test @GUITest
	public void testShowTotalCost() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		cartRepository.addToCart(product1);
		cartRepository.addToCart(product2);
		cartRepository.addToCart(product2);
		GuiActionRunner.execute(() -> {
			eShopController.showCartCost();	
		});
		window.label("totalCostLabel").requireText("3300.0$");
	}
}
