package com.apt.project.eshop.view.swing;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.model.CartItem;
import com.apt.project.eshop.model.CatalogItem;
import com.apt.project.eshop.model.Product;

@RunWith(GUITestRunner.class)
public class EShopSwingViewTest extends AssertJSwingJUnitTestCase {

	@Mock
	private EShopController eShopController;
	private EShopSwingView eShopSwingView;
	private FrameFixture window;
	private AutoCloseable closeable;

	@Override
	protected void onSetUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			eShopSwingView = new EShopSwingView(); // FOR CALLING AND TESTING METHODS OF SHOPVIEW
			eShopSwingView.setEShopController(eShopController);
			return eShopSwingView;
		});
		window = new FrameFixture(robot(), eShopSwingView); // FOR INTERACTING WITH THE GUI COMPONENTS
		window.show();
	}

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}

	@Test
	@GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("Products"));
		window.list("productList");
		window.textBox("searchTextBox").requireEnabled();
		window.button(JButtonMatcher.withText("Search")).requireDisabled();
		window.label("errorMessageLabel").requireText("");
		window.button(JButtonMatcher.withText("Clear")).requireDisabled();
		window.button(JButtonMatcher.withText("Add To Cart")).requireDisabled();
		window.label(JLabelMatcher.withText("Cart"));
		window.list("cartList");
		window.button(JButtonMatcher.withText("Remove From Cart")).requireDisabled();
		window.label(JLabelMatcher.withText("Total: "));
		window.label("totalCostLabel").requireText("0.0$");
		window.button(JButtonMatcher.withText("Checkout")).requireDisabled();
		window.label("checkoutResultLabel").requireText("");
	}

	@Test
	@GUITest
	public void testShowAllProductsShouldShowsProductsInTheList() {
		CatalogItem product = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		GuiActionRunner.execute(() -> eShopSwingView.showAllProducts(Arrays.asList(product)));
		String[] listContents = window.list("productList").contents();
		assertThat(listContents).containsExactly(product.getProduct().toString());
	}

	@Test
	@GUITest
	public void testWhenSearchTextBoxIsNotEmptyThenSearchButtonShouldBeEnabled() {
		window.textBox("searchTextBox").enterText("Laptop");
		window.button(JButtonMatcher.withText("Search")).requireEnabled();
	}

	@Test
	@GUITest
	public void testWhenSearchTextBoxIsWhiteSpaceThenSearchButtonShouldBeDisabled() {
		window.textBox("searchTextBox").enterText(" ");
		window.button(JButtonMatcher.withText("Search")).requireDisabled();
	}

	@Test
	@GUITest
	public void testShowSearchedProductsShouldShowInTheProductListOnlySearchedProducts() {
		CatalogItem item1 = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		CatalogItem item2= new CatalogItem(new Product("2", "Iphone", 1000), 1);
		CatalogItem item3= new CatalogItem(new Product("3", "Laptop MSI", 1200), 1);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Product> listProductsModel = eShopSwingView.getProductListModel();
			listProductsModel.addElement(item1.getProduct());
			listProductsModel.addElement(item2.getProduct());
			listProductsModel.addElement(item3.getProduct());
		});

		GuiActionRunner.execute(() -> eShopSwingView.showSearchedProducts(Arrays.asList(item1, item3)));

		String[] listContents = window.list("productList").contents();
		assertThat(listContents).containsExactly(item1.getProduct().toString(), item3.getProduct().toString());
	}

	@Test
	public void testSearchButtonShouldDelegateToEShopControllerSearchedProducts() {
		window.textBox("searchTextBox").enterText("Laptop");
		window.button(JButtonMatcher.withText("Search")).click();
		verify(eShopController).searchProducts("Laptop");
	}

	@Test
	@GUITest
	public void testShowErrorProductNotFoundShouldShowAMessageInTheErrorLabel() {
		String product = "Samsung s21";
		GuiActionRunner.execute(() -> eShopSwingView.showErrorProductNotFound(product));
		window.label("errorMessageLabel").foreground().requireEqualTo(Color.RED);
		window.label("errorMessageLabel").requireText("Nessun risultato trovato per: \"" + product + "\"");
	}

	@Test
	@GUITest
	public void testShowErrorProductNotFoundWhenThereIsLeadingWhiteSpaceShouldShowAMessageInTheErrorLabelWithoutWhiteSpace() {
		GuiActionRunner.execute(() -> eShopSwingView.showErrorProductNotFound("   samsung"));
		window.label("errorMessageLabel").requireText("Nessun risultato trovato per: \"" + "samsung" + "\"");
	}

	@Test
	@GUITest
	public void testResetErrorLabelWhenReleaseKeyInSearchTextBoxShouldResetErrorLabel() {
		String product = "Samsun";
		window.textBox("searchTextBox").enterText(product);
		GuiActionRunner.execute(
				() -> eShopSwingView.getLblErrorLabel().setText("Nessun risultato trovato per: \"" + product + "\""));
		window.textBox("searchTextBox").enterText("g");
		window.label("errorMessageLabel").requireText("");
	}

	@Test
	@GUITest
	public void testShowSearchedProductsShouldEnableClearButton() {
		CatalogItem item1 = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		CatalogItem item2 = new CatalogItem(new Product("3", "Laptop MSI", 1200), 1);
		GuiActionRunner.execute(() -> eShopSwingView.showSearchedProducts(Arrays.asList(item1, item2)));
		window.button(JButtonMatcher.withText("Clear")).requireEnabled();
	}

	@Test
	@GUITest
	public void testClearSearchShouldDisableClearButtonAndResetSearchTextBox() {
		window.textBox("searchTextBox").enterText("Laptop");
		GuiActionRunner.execute(() -> {
			eShopSwingView.getBtnClear().setEnabled(true);
			eShopSwingView.clearSearch(emptyList());
		});
		window.button(JButtonMatcher.withText("Clear")).requireDisabled();
		window.textBox("searchTextBox").requireText("");
	}

	@Test
	@GUITest
	public void testClearSearchShouldMakeTheProductListShowAllProducts() {
		CatalogItem item1 = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		CatalogItem item2= new CatalogItem(new Product("2", "Iphone", 1000), 1);
		CatalogItem item3= new CatalogItem(new Product("3", "Laptop MSI", 1200), 1);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Product> listProductsModel = eShopSwingView.getProductListModel();
			listProductsModel.addElement(item1.getProduct());
			listProductsModel.addElement(item3.getProduct());
		});
		GuiActionRunner.execute(() -> eShopSwingView.clearSearch(asList(item1, item2, item3)));
		String[] listContents = window.list("productList").contents();
		assertThat(listContents).containsExactly(item1.getProduct().toString(), item2.getProduct().toString(), item3.getProduct().toString());
	}

	@Test
	@GUITest
	public void testClearSearchWhenThereWasAProductNotFoundErrorShouldResetTheErrorLabel() {
		GuiActionRunner.execute(() -> {
			eShopSwingView.getBtnClear().setEnabled(true);
			eShopSwingView.getLblErrorLabel().setText("Nessun risultato trovato per: \"" + "Samsung" + "\"");
			eShopSwingView.clearSearch(emptyList());
		});
		window.label("errorMessageLabel").requireText("");
	}

	@Test
	public void testClearButtonShouldDelegateToEShopControllerResetSearch() {
		GuiActionRunner.execute(() -> {
			eShopSwingView.getBtnClear().setEnabled(true);
		});
		window.button(JButtonMatcher.withText("Clear")).click();
		verify(eShopController).resetSearch();
	}

	@Test
	@GUITest
	public void testAddToCartButtonShouldBeEnabledOnlyWhenAProductInTheProductListIsSelected() {
		GuiActionRunner.execute(() -> {
			eShopSwingView.getProductListModel().addElement(new Product("1", "Laptop", 1300));
		});
		window.list("productList").selectItem(0);
		window.button(JButtonMatcher.withText("Add To Cart")).requireEnabled();
		window.list("productList").clearSelection();
		window.button(JButtonMatcher.withText("Add To Cart")).requireDisabled();
	}

	@Test
	@GUITest
	public void testAddToCartViewShouldShowAProductInTheCartList() {
		CartItem item1 = new CartItem(new Product("1", "Laptop", 1300), 1);
		GuiActionRunner.execute(() -> {
			eShopSwingView.addToCartView(asList(item1));
		});
		String[] listContents = window.list("cartList").contents();
		assertThat(listContents).containsExactly(item1.toString());
	}

	@Test
	@GUITest
	public void testAddToCartViewWhenAddMultipleTimesTheSameProductShouldShowOneIstanceOfTheProductInTheCartListWithTheRightQuantity() {
		CartItem item1 = new CartItem(new Product("1", "Laptop", 1300), 1);
		CartItem item2= new CartItem(new Product("2", "Iphone", 1000), 1);
		CartItem item1TwoTimes = new CartItem(new Product("1", "Laptop", 1300), 2);		
		GuiActionRunner.execute(() -> {
			eShopSwingView.addToCartView(asList(item1));
			eShopSwingView.addToCartView(asList(item2));
			eShopSwingView.addToCartView(asList(item1TwoTimes, item2));
		});
		String[] listContents = window.list("cartList").contents();
		assertThat(listContents).containsExactly(item1TwoTimes.toString(), item2.toString());
	}

	@Test
	public void testAddToCartButtonShouldDelegateToEShopControllerNewCartProduct() {
		CatalogItem item1 = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		CatalogItem item2= new CatalogItem(new Product("2", "Iphone", 1000), 1);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Product> productListModel = eShopSwingView.getProductListModel();
			productListModel.addElement(item1.getProduct());
			productListModel.addElement(item2.getProduct());
		});
		window.list("productList").selectItem(1);
		window.button(JButtonMatcher.withText("Add To Cart")).click();
		verify(eShopController).newCartProduct(item2.getProduct());
	}

	@Test
	@GUITest
	public void testRemoveFromCartButtonShouldBeEnabledOnlyWhenAProductInTheCartListIsSelected() {
		GuiActionRunner.execute(() -> {
			eShopSwingView.getCartListModel().addElement(new CartItem(new Product("1", "Laptop", 1300), 1));
		});
		window.list("cartList").selectItem(0);
		window.button(JButtonMatcher.withText("Remove From Cart")).requireEnabled();
		window.list("cartList").clearSelection();
		window.button(JButtonMatcher.withText("Remove From Cart")).requireDisabled();
	}

	@Test
	@GUITest
	public void testRemoveFromCartViewShouldRemoveTheSelectedProductFromTheCartList() {
		CartItem item1 = new CartItem(new Product("1", "Laptop", 1300), 1);
		CartItem item2= new CartItem(new Product("2", "Iphone", 1000), 1);
		GuiActionRunner.execute(() -> {
			DefaultListModel<CartItem> cartListModel = eShopSwingView.getCartListModel();
			cartListModel.addElement(item1);
			cartListModel.addElement(item2);
			eShopSwingView.removeFromCartView(item1);
		});
		String[] listContents = window.list("cartList").contents();
		assertThat(listContents).containsExactly(item2.toString());
	}

	@Test
	public void testRemoveFromCartButtonShouldDelegateToEShopControllerRemoveCartProduct() {
		CartItem item1 = new CartItem(new Product("1", "Laptop", 1300), 1);
		CartItem item2= new CartItem(new Product("2", "Iphone", 1000), 1);
		GuiActionRunner.execute(() -> {
			DefaultListModel<CartItem> cartListModel = eShopSwingView.getCartListModel();
			cartListModel.addElement(item1);
			cartListModel.addElement(item2);
		});
		window.list("cartList").selectItem(1);
		window.button(JButtonMatcher.withText("Remove From Cart")).click();
		verify(eShopController).removeCartProduct(item2);
	}

	@Test
	@GUITest
	public void testTheUIWhenTheUserSelectSomethingFromTheProductListAndThenSomethingInTheCartListShoudDeselectTheElementFromTheProductList() {
		CatalogItem item1 = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		CartItem item2= new CartItem(new Product("2", "Iphone", 1000), 1);
		GuiActionRunner.execute(() -> {
			eShopSwingView.getProductListModel().addElement(item1.getProduct());
			eShopSwingView.getCartListModel().addElement(item2);
		});
		window.list("productList").selectItem(0);
		window.list("cartList").selectItem(0);
		window.list("productList").requireNoSelection();
		window.list("cartList").requireSelection(0);
	}

	@Test
	@GUITest
	public void testTheUIWhenTheUserSelectSomethingFromTheCartListAndThenSomethingInTheProductListShoudDeselectTheElementFromTheCartList() {
		CatalogItem item1 = new CatalogItem(new Product("1", "Laptop", 1300), 1);
		CartItem item2= new CartItem(new Product("2", "Iphone", 1000), 1);
		GuiActionRunner.execute(() -> {
			eShopSwingView.getProductListModel().addElement(item1.getProduct());
			eShopSwingView.getCartListModel().addElement(item2);
		});
		window.list("cartList").selectItem(0);
		window.list("productList").selectItem(0);
		window.list("cartList").requireNoSelection();
		window.list("productList").requireSelection(0);
	}

	@Test
	@GUITest
	public void testCheckoutButtonShouldBeEnabledOnlyWhenAtLeastOneProductIsInsideTheCartList() {
		GuiActionRunner.execute(() -> {
			eShopSwingView.getCartListModel().addElement(new CartItem(new Product("1", "Laptop", 1300), 1));
		});
		window.button(JButtonMatcher.withText("Checkout")).requireEnabled();
		GuiActionRunner.execute(() -> {
			eShopSwingView.getCartListModel().removeAllElements();
		});
		window.button(JButtonMatcher.withText("Checkout")).requireDisabled();
	}

	@Test
	public void testCheckoutButtonShouldDelegateToEShopControllerCheckoutCart() {
		GuiActionRunner.execute(() -> {
			eShopSwingView.getBtnCheckout().setEnabled(true);
		});
		window.button(JButtonMatcher.withText("Checkout")).click();
		verify(eShopController).checkoutCart();
	}

	@Test
	@GUITest
	public void testClearCartShouldRemoveAllElementsFromTheCartList() {
		GuiActionRunner.execute(() -> {
			eShopSwingView.getCartListModel().addElement(new CartItem(new Product("1", "Laptop", 1300), 1));
			eShopSwingView.clearCart();
		});
		String[] listContents = window.list("cartList").contents();
		assertThat(listContents).isEmpty();
	}

	@Test
	@GUITest
	public void testShowSuccessLabelShouldShowAMessageForTheSuccessfulCheckout() {
		CartItem item1 = new CartItem(new Product("1", "Laptop", 1300), 1);
		CartItem item2= new CartItem(new Product("2", "eBook", 300), 2);
		GuiActionRunner.execute(() -> {
			eShopSwingView.getCartListModel().addElement(item1);
			eShopSwingView.getCartListModel().addElement(item2);
			eShopSwingView.getTotalCostlabel().setText("1900.0$");
			eShopSwingView.showSuccessLabel();
		});
		window.label("checkoutResultLabel")
				.requireText("<html>Thank you for the purchase!!<br/>"
						+ "<br/>You have spent 1900.0$ for the following products:<br/>" + "-- Laptop, quantity:1<br/>"
						+ "-- eBook, quantity:2<br/></html>");
		window.label("checkoutResultLabel").foreground().requireEqualTo(Color.BLACK);
	}

	@Test
	@GUITest
	public void testClearCheckoutResultWhenTheUserClicksAddToCartButton() {
		GuiActionRunner.execute(() -> {
			eShopSwingView.getBtnAddToCart().setEnabled(true);
			eShopSwingView.getLblCheckoutLabel().setText("some text");
		});
		window.button(JButtonMatcher.withText("Add To Cart")).click();
		window.label("checkoutResultLabel").requireText("");
	}

	@Test
	@GUITest
	public void testClearCheckoutResultWhenTheUserClicksRemoveFromCartButton() {
		GuiActionRunner.execute(() -> {
			eShopSwingView.getBtnRemoveFromCart().setEnabled(true);
			eShopSwingView.getLblCheckoutLabel().setText("some text");
		});
		window.button(JButtonMatcher.withText("Remove From Cart")).click();
		window.label("checkoutResultLabel").requireText("");
	}

	@Test
	@GUITest
	public void testClearCheckoutResultWhenTheUserClicksSearchButton() {
		GuiActionRunner.execute(() -> {
			eShopSwingView.getBtnSearch().setEnabled(true);
			eShopSwingView.getLblCheckoutLabel().setText("some text");
		});
		window.button(JButtonMatcher.withText("Search")).click();
		window.label("checkoutResultLabel").requireText("");
	}

	@Test
	@GUITest
	public void testClearCheckoutResultWhenTheUserClicksClearButton() {
		GuiActionRunner.execute(() -> {
			eShopSwingView.getBtnClear().setEnabled(true);
			eShopSwingView.getLblCheckoutLabel().setText("some text");
		});
		window.button(JButtonMatcher.withText("Clear")).click();
		window.label("checkoutResultLabel").requireText("");
	}

	@Test
	@GUITest
	public void testshowFailureLabelShouldShowAMessageForTheCheckoutFailure() {
		CartItem item1 = new CartItem(new Product("1", "Laptop", 1300), 1);
		CartItem item2= new CartItem(new Product("2", "eBook", 300), 5);
		CatalogItem item2Catalog= new CatalogItem(new Product("2", "eBook", 300), 2);
		GuiActionRunner.execute(() -> {
			eShopSwingView.getCartListModel().addElement(item1);
			eShopSwingView.getCartListModel().addElement(item2);
			eShopSwingView.showFailureLabel(item2Catalog);
		});
		window.label("checkoutResultLabel")
				.requireText("<html>Error!<br/>" + "<br/>Not enough stock for the following product:<br/>"
						+ "-- eBook, remaining stock:2<br/>" + "<br/>Remove some products and try again</html>");
		window.label("checkoutResultLabel").foreground().requireEqualTo(Color.RED);
	}

	@Test
	@GUITest
	public void testShowAllCartShouldShowsProductsInTheCart() {
		CartItem item = new CartItem(new Product("1", "Laptop", 1300), 1);
		GuiActionRunner.execute(() -> eShopSwingView.showAllCart(Arrays.asList(item)));
		String[] listContents = window.list("cartList").contents();
		assertThat(listContents).containsExactly(item.toString());
	}

	@Test
	@GUITest
	public void testShowTotalCostShouldShowsTheCartPriceInTheLabel() {
		CartItem item1= new CartItem(new Product("1", "Laptop", 1300), 1);
		CartItem item2 = new CartItem(new Product("2", "Iphone", 1000), 2);
		GuiActionRunner.execute(() -> eShopSwingView.showTotalCost(
				(item1.getProduct().getPrice() * item1.getQuantity()) + (item2.getProduct().getPrice() * item2.getQuantity())));
		window.label("totalCostLabel").requireText("3300.0$");
	}
}
