package com.apt.project.eshop.view.swing;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

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
	protected void onTearDown() throws Exception{
		closeable.close();
	}

	@Test @GUITest
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
	}
	
	@Test @GUITest
	public void testShowAllProductsShouldShowsProductsInTheList() {
		Product product = new Product("1", "Laptop", 1300);
		GuiActionRunner.execute(()-> eShopSwingView.showAllProducts(Arrays.asList(product)));
		String[] listContents = window.list("productList").contents();
		assertThat(listContents).containsExactly(product.toString());
	}
	
	@Test @GUITest
	public void testWhenSearchTextBoxIsNotEmptyThenSearchButtonShouldBeEnabled() {
		window.textBox("searchTextBox").enterText("Laptop");
		window.button(JButtonMatcher.withText("Search")).requireEnabled();
	}
	
	@Test @GUITest
	public void testWhenSearchTextBoxIsWhiteSpaceThenSearchButtonShouldBeDisabled() {
		window.textBox("searchTextBox").enterText(" ");
		window.button(JButtonMatcher.withText("Search")).requireDisabled();
	}

	@Test @GUITest
	public void testShowSearchedProductsShouldShowInTheProductListOnlySearchedProducts() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		Product product3 = new Product("3", "Laptop MSI", 1200);
		GuiActionRunner.execute(
			() -> {
				DefaultListModel<Product> listProductsModel = eShopSwingView.getProductListModel();
				listProductsModel.addElement(product1);
				listProductsModel.addElement(product2);
				listProductsModel.addElement(product3);
			}
		);
		
		GuiActionRunner.execute(
			() -> eShopSwingView.showSearchedProducts(Arrays.asList(product1, product3))
		);
		
		String[] listContents = window.list("productList").contents();
		assertThat(listContents).containsExactly(product1.toString(), product3.toString());
	}
	
	@Test
	public void testSearchButtonShouldDelegateToShopControllerSearchedProducts() {
		window.textBox("searchTextBox").enterText("Laptop");
		window.button(JButtonMatcher.withText("Search")).click();
		verify(eShopController).searchProducts("Laptop");
	}
	
	@Test @GUITest
	public void testShowErrorProductNotFoundShouldShowAMessageInTheErrorLabel() {
		String product = "Samsung s21";
		GuiActionRunner.execute(
			() -> eShopSwingView.showErrorProductNotFound(product)
		);
		window.label("errorMessageLabel")
			.requireText("Nessun risultato trovato per: \"" + product + "\"");
	}
	
	@Test @GUITest
	public void testShowErrorProductNotFoundWhenThereIsLeadingWhiteSpaceShouldShowAMessageInTheErrorLabelWithoutWhiteSpace() {
		GuiActionRunner.execute(
			() -> eShopSwingView.showErrorProductNotFound("   samsung")
		);
		window.label("errorMessageLabel").requireText(
				"Nessun risultato trovato per: \"" + "samsung" + "\"");
	}
	
	@Test @GUITest
	public void testResetErrorLabelWhenReleaseKeyInSearchTextBoxShouldResetErrorLabel() {
		String product = "Samsun";
		window.textBox("searchTextBox").enterText(product);
		GuiActionRunner.execute(
			() -> eShopSwingView.getLblErrorLabel()
					.setText("Nessun risultato trovato per: \"" + product + "\"")
		);
		window.textBox("searchTextBox").enterText("g");
		window.label("errorMessageLabel").requireText("");
	}
	
	@Test @GUITest
	public void testShowSearchedProductsShouldEnableClearButton() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("3", "Laptop MSI", 1200);
		GuiActionRunner.execute(
			() -> eShopSwingView.showSearchedProducts(Arrays.asList(product1, product2))
		);
		window.button(JButtonMatcher.withText("Clear")).requireEnabled();
	}
	
	@Test @GUITest
	public void testClearSearchShouldDisableClearButtonAndResetSearchTextBox() {
		window.textBox("searchTextBox").enterText("Laptop");
		GuiActionRunner.execute(
			() -> {
				eShopSwingView.getBtnClear().setEnabled(true);
				eShopSwingView.clearSearch(emptyList());
		});
		window.button(JButtonMatcher.withText("Clear")).requireDisabled();
		window.textBox("searchTextBox").requireText("");
	}
	
	@Test @GUITest
	public void testClearSearchShouldMakeTheProductListShowAllProducts() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Iphone", 1000);
		Product product3 = new Product("3", "Laptop MSI", 1200);
		GuiActionRunner.execute(
			() -> {
				DefaultListModel<Product> listProductsModel = eShopSwingView.getProductListModel();
				listProductsModel.addElement(product1);
				listProductsModel.addElement(product3);
			}
		);
		GuiActionRunner.execute(
			() -> eShopSwingView.clearSearch(asList(product1, product2, product3))
		);
		String[] listContents = window.list("productList").contents();
		assertThat(listContents).containsExactly(product1.toString(), product2.toString(), product3.toString());
	}
	
	@Test @GUITest
	public void testClearSearchWhenThereWasAProductNotFoundErrorShouldResetTheErrorLabel() {
		GuiActionRunner.execute(
			() -> {
				eShopSwingView.getBtnClear().setEnabled(true);
				 eShopSwingView.getLblErrorLabel()
					.setText("Nessun risultato trovato per: \"" + "Samsung" + "\"");
				eShopSwingView.clearSearch(emptyList());
		});
		window.label("errorMessageLabel").requireText("");
	}
	
	@Test @GUITest
	public void testClearButtonShouldDelegateToShopControllerResetSearch() {
		GuiActionRunner.execute(
			() -> {
					eShopSwingView.getBtnClear().setEnabled(true);
		});
		window.button(JButtonMatcher.withText("Clear")).click();
		verify(eShopController).resetSearch();
	}
	
	@Test @GUITest
	public void testAddToCartButtonShouldBeEnabledOnlyWhenAProductInTheProductListIsSelected() {
		GuiActionRunner.execute(
			() -> {
					eShopSwingView.getProductListModel().addElement(new Product("1", "Laptop", 1300));
		});
		window.list("productList").selectItem(0);
		window.button(JButtonMatcher.withText("Add To Cart")).requireEnabled();
		window.list("productList").clearSelection();
		window.button(JButtonMatcher.withText("Add To Cart")).requireDisabled();
	}
	
	@Test @GUITest
	public void testAddToCartViewShouldShowAProductInTheCartList() {
		Product product1 = new Product("1", "Laptop", 1300);
		GuiActionRunner.execute(
			() -> {
				eShopSwingView.addToCartView(asList(product1));
		});
		String[] listContents = window.list("cartList").contents();
		assertThat(listContents).containsExactly(product1.toStringExtended());
	}
	
	@Test @GUITest
	public void testAddToCartViewWhenAddMultipleTimesTheSameProductShouldShowOneIstanceOfTheProductInTheCartListWithTheRightQuantity() {
		Product product1 = new Product("1", "Laptop", 1300, 1);
		Product product2 = new Product("2", "eBook", 300, 1);
		Product product1TwoTimes = new Product("1", "laptop", 1300, 2);
		GuiActionRunner.execute(
			() -> {
				eShopSwingView.addToCartView(asList(product1));
				eShopSwingView.addToCartView(asList(product2));
				eShopSwingView.addToCartView(asList(product1TwoTimes, product2));				
		});
		String[] listContents = window.list("cartList").contents();
		assertThat(listContents).containsExactly(product1TwoTimes.toStringExtended(), product2.toStringExtended());
	}
	
	@Test @GUITest
	public void testClickInTheContentPaneShouldDeselectElementInTheProductList() {
		Product product = new Product("1", "Laptop", 1300, 1);
		GuiActionRunner.execute(
			() -> {
				eShopSwingView.getProductListModel().addElement(product);
		});
		window.list("productList").selectItem(0);
		window.panel("contentPane").click();
		window.list("productList").requireNoSelection();
		window.button(JButtonMatcher.withText("Add To Cart")).requireDisabled();
	}
	
	@Test @GUITest
	public void testClickInTheContentPaneShouldDeselectElementInTheCartList() {
		Product product = new Product("1", "Laptop", 1300, 1);
		GuiActionRunner.execute(
			() -> {
				eShopSwingView.getCartListModel().addElement(product);
		});
		window.list("cartList").selectItem(0);
		window.panel("contentPane").click();
		window.list("cartList").requireNoSelection();
	}
	
	@Test @GUITest
	public void testAddToCartButtonShouldDelegateToEShopControllerNewCartProduct() {
		Product product1 = new Product("1", "Laptop", 1300);
		Product product2 = new Product("2", "Kindle", 200);
		GuiActionRunner.execute(
				() -> {
					DefaultListModel<Product> productListModel = eShopSwingView.getProductListModel();
					productListModel.addElement(product1);
					productListModel.addElement(product2);
				});
		window.list("productList").selectItem(1);
		window.button(JButtonMatcher.withText("Add To Cart")).click();
		verify(eShopController).newCartProduct(product2);
	}
	
	@Test @GUITest
	public void testRemoveFromCartButtonShouldBeEnabledOnlyWhenAProductInTheCartListIsSelected() {
		GuiActionRunner.execute(
			() -> {
					eShopSwingView.getCartListModel().addElement(new Product("1", "Laptop", 1300));
		});
		window.list("cartList").selectItem(0);
		window.button(JButtonMatcher.withText("Remove From Cart")).requireEnabled();
		window.list("cartList").clearSelection();
		window.button(JButtonMatcher.withText("Remove From Cart")).requireDisabled();
	}
}

