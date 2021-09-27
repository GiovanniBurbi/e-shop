package com.apt.project.eshop.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.apt.project.eshop.model.Product;

@RunWith(GUITestRunner.class)
public class EShopSwingViewTest extends AssertJSwingJUnitTestCase {
	
	private EShopSwingView eShopSwingView;
	private FrameFixture window;

	@Override
	protected void onSetUp() throws Exception {
		GuiActionRunner.execute(() -> {
			eShopSwingView = new EShopSwingView(); // FOR CALLING AND TESTING METHODS OF SHOPVIEW
			return eShopSwingView;
		});
		window = new FrameFixture(robot(), eShopSwingView); // FOR INTERACTING WITH THE GUI COMPONENTS
		window.show();

	}

	@Test
	@GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("Products"));
		window.list("productList");
	}
	
	@Test 
	public void testShowAllProductsShouldShowsProductsInTheList() {
		Product product = new Product("1", "Laptop", 1300);
		GuiActionRunner.execute(()-> eShopSwingView.showAllProducts(Arrays.asList(product)));
		String[] listContents = window.list("productList").contents();
		assertThat(listContents).containsExactly(product.toString());
	}

}

