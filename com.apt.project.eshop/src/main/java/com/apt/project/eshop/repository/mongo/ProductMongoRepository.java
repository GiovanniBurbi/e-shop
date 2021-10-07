package com.apt.project.eshop.repository.mongo;

import static java.util.Collections.emptyList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.ProductRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class ProductMongoRepository implements ProductRepository {

	private MongoCollection<Product> productCollection;
	private MongoDatabase database;
	private MongoCollection<Product> cartCollection;

	public ProductMongoRepository(MongoClient client, String databaseName, String collectionName) {
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		database = client.getDatabase(databaseName);
		productCollection = database.getCollection(collectionName, Product.class).withCodecRegistry(pojoCodecRegistry);
		cartCollection = database.getCollection("cart", Product.class).withCodecRegistry(pojoCodecRegistry);
	}
	
	@Override
	public List<Product> findAll() {
		return StreamSupport.stream(productCollection.find().spliterator(), false).collect(Collectors.toList());
	}

	@Override
	public void loadCatalog(List<Product> products) {
		database.drop();
		productCollection.insertMany(products);
	}

	@Override
	public List<Product> findByName(String nameSearch) {
		return StreamSupport.stream(
				productCollection
					.find(Filters.regex("name",Pattern.compile(nameSearch, Pattern.CASE_INSENSITIVE))).spliterator(), false)
					.collect(Collectors.toList()
		);
	}

	@Override
	public void addToCart(Product product) {
		cartCollection.insertOne(new Product(product.getId(), product.getName(), product.getPrice()));
	}

	@Override
	public List<Product> allCart() {
		// da implementare
		return emptyList();
	}
}
