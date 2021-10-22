package com.apt.project.eshop.bdd;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
* Start MongoDB with Docker with
*
* <pre>
* docker run -p 27017:27017 --rm mongo:4.4.3
* </pre>
* 
* @author Giovanni Burbi
* 
*/
@RunWith(Cucumber.class)
@CucumberOptions(features = "src/bdd/resources", monochrome = true)
public class EShopAppBDD {
	
	public static int mongoPort = Integer.parseInt(System.getProperty("mongo.mapped.port", "27017"));
	
	@BeforeClass
	public static void setUpOnce() {
		FailOnThreadViolationRepaintManager.install();
	}
	
}
