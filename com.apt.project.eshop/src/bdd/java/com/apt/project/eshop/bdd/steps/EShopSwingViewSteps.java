package com.apt.project.eshop.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;

import com.apt.project.eshop.bdd.EShopAppBDD;
import com.apt.project.eshop.model.Product;

import io.cucumber.java.After;
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

	@Then("The list contains an element with the following values")
	public void the_list_contains_an_element_with_the_following_values(List<Map<String, String>> values) {
		values.forEach(
		    v -> assertThat(window.list("productList").contents())
		    	.anySatisfy(e -> assertThat(e)
		    		.contains(new Product(v.get("id"), v.get("name"), Double.parseDouble(v.get("price"))).toString()))
		);
	}
}
