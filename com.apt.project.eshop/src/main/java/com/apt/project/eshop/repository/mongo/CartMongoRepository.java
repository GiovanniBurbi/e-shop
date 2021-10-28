package com.apt.project.eshop.repository.mongo;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.CartRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class CartMongoRepository implements CartRepository{
		
	private MongoCollection<Product> cartCollection;

	List<Product> products;
	
	private MongoDatabase database;

	private ClientSession session;

	public CartMongoRepository(MongoClient client, String databaseName, String collectionName) {
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		database = client.getDatabase(databaseName);
		cartCollection = database.getCollection(collectionName, Product.class).withCodecRegistry(pojoCodecRegistry);
	}

	public CartMongoRepository(MongoClient client, String databaseName, String collectionName,
			ClientSession session) {
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		database = client.getDatabase(databaseName);
		cartCollection = database.getCollection(collectionName, Product.class).withCodecRegistry(pojoCodecRegistry);
		this.session = session;
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
		cartCollection.findOneAndDelete(session, Filters.eq("name", product.getName()));
	}

	@Override
	public double cartTotalCost() {
		List<Product> cartProducts = StreamSupport.stream(cartCollection.find().spliterator(), false).collect(Collectors.toList());
		double total = 0;
		for (Product product : cartProducts) {
			total += product.getPrice() * product.getQuantity();
		}
		return total;
	}	
}