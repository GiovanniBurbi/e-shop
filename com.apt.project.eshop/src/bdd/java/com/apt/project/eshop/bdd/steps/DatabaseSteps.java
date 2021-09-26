package com.apt.project.eshop.bdd.steps;

import org.bson.Document;

import com.apt.project.eshop.bdd.EShopAppBDD;
import com.mongodb.MongoClient;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

public class DatabaseSteps {

	static final String DB_NAME = "test-db";
	static final String COLLECTION_NAME = "test-collection";
	private MongoClient mongoClient;

	@Before
	public void setUp() {
		mongoClient = new MongoClient("localhost", EShopAppBDD.mongoPort);
		// to start with an empty database
		mongoClient.getDatabase(DB_NAME).drop();
	}

	@After
	public void tearDown() {
		mongoClient.close();
	}

	@Given("The database contains a product with id {string}, name {string} and price {double}")
	public void the_database_contains_a_product_with_id_name_and_price(String id, String name, Double price) {
		mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME)
				.insertOne(new Document().append("id", id).append("name", name).append("price", price));
	}
	
}