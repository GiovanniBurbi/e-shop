package com.apt.project.eshop.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;

import com.apt.project.eshop.bdd.EShopAppBDD;
import com.apt.project.eshop.model.Product;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class EShopSwingViewSteps {

	private FrameFixture window;

	static final String DB_NAME = "test-db";
	static final String COLLECTION_NAME = "test-collection";

	@After
	public void tearDown() {
		if (window != null)
			window.cleanUp();
	}

	@When("The eShop View is shown")
	public void the_eShop_View_is_shown() {
		// start the Swing application
		application("com.apt.project.eshop.app.swing.EShopSwingApp").withArgs(
				"--mongo-port=" + EShopAppBDD.mongoPort,
				"--db-name=" + DB_NAME,
				"--db-collection=" + COLLECTION_NAME

		).start();
		// get a reference of its JFrame
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "eShop View".equals(frame.getTitle()) && frame.isShowing();
			}

		}).using(BasicRobot.robotWithCurrentAwtHierarchy());
	}

	@Then("The list contains elements with the following values")
	public void the_list_contains_elements_with_the_following_values(List<Map<String, String>> values) {
		values.forEach(
		    v -> assertThat(window.list("productList").contents())
		    	.anySatisfy(e -> assertThat(e)
		    		.contains(new Product(v.get("id"), v.get("name"), Double.parseDouble(v.get("price"))).toString()))
		);
	}
	
	@When("The user enters in the search text field the name {string}")
	public void the_user_enters_in_the_search_text_field_the_name(String nameToSearch) {
		window.textBox("searchTextBox").enterText(nameToSearch);
	}

	@When("The user clicks the {string} button")
	public void the_user_clicks_the_button(String buttonText) {
		window.button(JButtonMatcher.withText(buttonText)).click();
	}

	@Then("The list shows products with {string} in the name")
	public void the_list_shows_products_with_in_the_name(String nameToSearch) {
		assertThat(window.list("productList").contents()).isNotEmpty(); //A CAUSA DI ALLSATISFY CHE FA SCHIFO QUANDO ASSERISCE LISTA VUOTA
		assertThat(window.list("productList").contents())
			.allSatisfy(e -> assertThat(e.toLowerCase()).containsSubsequence(nameToSearch.toLowerCase()));
	}
	
	@Then("An error is shown containing the name searched {string}")
	public void an_error_is_shown_containing_the_name_searched(String nameSearched) {
		assertThat(window.label("errorMessageLabel").text()).contains(nameSearched);
	}
	
	@When("The user search the product {string}")
	public void the_user_search_the_product(String nameToSearch) {
		window.textBox("searchTextBox").enterText(nameToSearch);
		window.button(JButtonMatcher.withText("Search")).click();
	}

	@Then("The search text box is empty")
	public void the_search_text_box_is_empty() {
		window.textBox("searchTextBox").requireText("");
	}
	
	@Given("The user select another product from the product list")
	public void the_user_select_another_product_from_the_product_list() {
		window.list("productList").selectItem(0);
	}

	@When("The user clicks the {string} button {int} times")
	public void the_user_clicks_the_button_times(String buttonText, Integer nClicks) {
		while(nClicks > 0) {
			window.button(JButtonMatcher.withText(buttonText)).click();
			nClicks -= 1;
		}
	}

	@Then("The cart list contains an element with the following values")
	public void the_cart_list_contains_an_element_with_the_following_values(List<Map<String, String>> values) {
		values.forEach(
		    v -> assertThat(window.list("cartList").contents())
		    		.anySatisfy(e -> assertThat(e)
		    			.contains(new Product(v.get("id"), v.get("name"), Double.parseDouble(v.get("price")), Integer.parseInt(v.get("quantity"))).toStringExtended()))
		);
	}
	
	@Given("The cart contains a product")
	public void the_cart_contains_a_product() {
		window.list("productList").selectItem(4);
		window.button(JButtonMatcher.withText("Add To Cart")).click();
	}
	
	@When("The user select that product in the cart")
	public void the_user_select_that_product_in_the_cart() {
		window.list("cartList").selectItem(0);
	}

	@Then("The cart list is empty")
	public void the_cart_list_is_empty() {
		assertThat(window.list("cartList").contents()).isEmpty();
	}
	
	@Then("The view shows the updated total of {string}")
	public void the_view_shows_the_updated_total_of(String cartTotalCost) {
		window.label("totalCostLabel").requireText(cartTotalCost);
	}
	
	@Given("The cart contains {int} item of a product")
	public void the_cart_contains_item_of_a_product(Integer quantity) {
		window.list("productList").selectItem(4);
		while (quantity > 0) {
			window.button(JButtonMatcher.withText("Add To Cart")).click();
			quantity -= 1;
		}
	}

	@Given("The cart contains some products")
	public void the_cart_contains_some_products() {
		window.list("productList").selectItem(0);
		window.button(JButtonMatcher.withText("Add To Cart")).click();
		window.list("productList").selectItem(1);
		window.button(JButtonMatcher.withText("Add To Cart")).click();
		window.button(JButtonMatcher.withText("Add To Cart")).click();
	}

	@Then("The view shows a message about the successful checkout")
	public void the_view_shows_a_message_about_the_successful_checkout() {
		window.label("checkoutResultLabel").requireText(
			"<html>Thank you for the purchase!!<br/>"
			+ "<br/>You have spent 3300.0$ for the following products:<br/>"
			+ "-- Laptop, quantity:1<br/>"
			+ "-- Iphone, quantity:2<br/></html>"
		);
	}
}
