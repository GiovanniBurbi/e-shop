package com.apt.project.eshop.bdd.steps;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.List;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.apt.project.eshop.bdd.EShopAppBDD;
import com.apt.project.eshop.model.Product;
import com.mongodb.MongoClient;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

public class DatabaseSteps {

	static final String DB_NAME = "test-db";
	static final String COLLECTION_NAME = "test-collection";
	private MongoClient mongoClient;
	private CodecRegistry pojoCodecRegistry;

	@Before
	public void setUp() {
		mongoClient = new MongoClient("localhost", EShopAppBDD.mongoPort);
		// to start with an empty database
		mongoClient.getDatabase(DB_NAME).drop();
		pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
				fromProviders(PojoCodecProvider.builder().automatic(true).build()));
	}

	@After
	public void tearDown() {
		mongoClient.close();
	}
	
	@Given("The database contains products with the following values")
	public void the_database_contains_products_with_the_following_values(List<List<String>> values) {
		values.forEach(
			v -> mongoClient.getDatabase(DB_NAME)
				.getCollection(COLLECTION_NAME, Product.class)
				.withCodecRegistry(pojoCodecRegistry)
				.insertOne(new Product(v.get(0), v.get(1), Double.parseDouble(v.get(2))))
		);
	}	
}