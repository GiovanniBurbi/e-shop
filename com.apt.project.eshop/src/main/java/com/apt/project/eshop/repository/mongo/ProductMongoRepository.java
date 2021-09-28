package com.apt.project.eshop.repository.mongo;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.ProductRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class ProductMongoRepository implements ProductRepository {

	private MongoCollection<Product> productCollection;

	public ProductMongoRepository(MongoClient client, String databaseName, String collectionName) {
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		productCollection = client.getDatabase(databaseName).getCollection(collectionName, Product.class).withCodecRegistry(pojoCodecRegistry);
	}
	
	@Override
	public List<Product> findAll() {
		return StreamSupport.stream(productCollection.find().spliterator(), false).collect(Collectors.toList());
	}

	@Override
	public void loadCatalog(Product product) {
		
	}
}
