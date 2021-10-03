package com.apt.project.eshop.view.swing;

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
	public void testResetErrorLabelWhenReleaseKeyInSearchTextBoxShouldResetErrorLabel() {
		String product = "Samsun";
		window.textBox("searchTextBox").enterText(product);
		GuiActionRunner.execute(
				() -> eShopSwingView.getLblErrorLabel()
						.setText("Nessun risultato trovato per: \"" + product + "\"")
		);
		window.textBox("searchTextBox").enterText("g");
		assertThat(eShopSwingView.getLblErrorLabel().getText()).isEmpty();
		}
}

