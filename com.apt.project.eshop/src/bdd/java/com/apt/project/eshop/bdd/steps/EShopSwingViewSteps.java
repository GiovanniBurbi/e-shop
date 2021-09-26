package com.apt.project.eshop.bdd.steps;

import static org.assertj.swing.launcher.ApplicationLauncher.application;

import javax.swing.JFrame;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class EShopSwingViewSteps {

	private FrameFixture window;

	@When("The eShop View is shown")
	public void the_eShop_View_is_shown() {
		// start the Swing application
		application("com.apt.project.eshop.app.swing.EShopSwingApp").start();
		// get a reference of its JFrame
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "eShop View".equals(frame.getTitle()) && frame.isShowing();
			}

		}).using(BasicRobot.robotWithCurrentAwtHierarchy());		
	}

	@Then("The product list contains an element with id {string}, name {string} and price {double}")
	public void the_product_list_contains_an_element_with_id_name_and_price(String id, String name, Double price) {
	    // Write code here that turns the phrase above into concrete actions
	    throw new io.cucumber.java.PendingException();
	}
	
}
