package com.apt.project.eshop.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static com.mongodb.client.model.Projections.excludeId;

import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.apt.project.eshop.bdd.EShopAppBDD;
import com.mongodb.MongoClient;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class DatabaseSteps {

	private static final String MONGO_HOST = "localhost";
	static final String DB_NAME = "test-db";
	static final String COLLECTION_NAME = "test-product-collection";
	private MongoClient mongoClient;

	@Before
	public void setUp() {
		mongoClient = new MongoClient(MONGO_HOST, EShopAppBDD.mongoPort);
		// to start with an empty database
		mongoClient.getDatabase(DB_NAME).drop();
	}

	@After
	public void tearDown() {
		mongoClient.close();
	}

	@Given("The database contains products with the following values")
	public void the_database_contains_products_with_the_following_values(List<Map<String, String>> values) {
		values.forEach(v -> mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME).insertOne(new Document()
				.append("id", v.get("id"))
				.append("name", v.get("name"))
				.append("price", Double.parseDouble(v.get("price")))
				.append("storage", Integer.parseInt(v.get("storage")))
		));
	}

	@Then("The database storage of the purchased products is updated")
	public void the_database_storage_of_the_purchased_products_is_updated() {
		assertThat(mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME).find().projection(excludeId())).containsExactly(
				catalogItemDocument("1", "Laptop", 1300.0, 1),
				catalogItemDocument("2", "Iphone", 1000.0, 0),
				catalogItemDocument("3", "Laptop MSI", 1250.0, 1),
				catalogItemDocument("4", "Macbook", 1400.0, 1),
				catalogItemDocument("5", "SmartTV", 400.0, 1),
				catalogItemDocument("6", "Playstation 5", 500.0, 1),
				catalogItemDocument("7", "Xbox", 500.0, 1)
		);
	}

	@Then("The database storage of the products has not changed")
	public void the_database_storage_of_the_products_has_not_changed() {
		assertThat(mongoClient.getDatabase(DB_NAME).getCollection(COLLECTION_NAME).find().projection(excludeId())).containsExactly(
				catalogItemDocument("1", "Laptop", 1300.0, 2),
				catalogItemDocument("2", "Iphone", 1000.0, 2),
				catalogItemDocument("3", "Laptop MSI", 1250.0, 1),
				catalogItemDocument("4", "Macbook", 1400.0, 1),
				catalogItemDocument("5", "SmartTV", 400.0, 1),
				catalogItemDocument("6", "Playstation 5", 500.0, 1),
				catalogItemDocument("7", "Xbox", 500.0, 1)
		);
	}
	
	private Document catalogItemDocument(String id, String name, Double price, Integer storage) {
		return new Document().append("id", id)
				.append("name", name)
				.append("price", price)
				.append("storage", storage);		
	}
}