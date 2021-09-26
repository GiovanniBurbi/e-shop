package com.apt.project.eshop.repository.mongo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.ProductRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class ProductMongoRepository implements ProductRepository {

	private MongoCollection<Document> productCollection;

	public ProductMongoRepository(MongoClient client, String databaseName, String collectionName) {
		productCollection = client.getDatabase(databaseName).getCollection(collectionName);
	}
	
	@Override
	public List<Product> findAll() {
		return StreamSupport.stream(productCollection.find().spliterator(), false).map(this::fromDocumentToProduct).collect(Collectors.toList());
	}

	private Product fromDocumentToProduct(Document d) {
		return new Product(""+d.get("id"), ""+d.get("name"), d.getDouble("price"));
	}

}
