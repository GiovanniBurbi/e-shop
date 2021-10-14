package com.apt.project.eshop.repository.mongo;

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
import com.mongodb.client.model.Updates;

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
		Product existingCartProduct = cartCollection.find(Filters.eq("name", product.getName())).first();
		if (existingCartProduct != null)
			cartCollection.updateOne(Filters.eq("name", product.getName()), Updates.inc("quantity", 1));
		else 
			cartCollection.insertOne(new Product(product.getId(), product.getName(), product.getPrice()));
	}

	@Override
	public List<Product> allCart() {
		return StreamSupport.stream(cartCollection.find().spliterator(), false).collect(Collectors.toList());
	}

	@Override
	public void removeFromCart(Product product) {
		cartCollection.findOneAndDelete(Filters.eq("name", product.getName()));
	}

	@Override
	public void removeFromStorage(Product product) {
		// TODO Auto-generated method stub
		
	}
}
