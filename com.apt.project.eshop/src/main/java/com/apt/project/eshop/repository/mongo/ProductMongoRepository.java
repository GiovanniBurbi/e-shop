package com.apt.project.eshop.repository.mongo;

import java.util.Collections;
import java.util.List;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.ProductRepository;
import com.mongodb.MongoClient;

public class ProductMongoRepository implements ProductRepository {

	public ProductMongoRepository(MongoClient client, String databaseName, String collectionName) {}
	
	@Override
	public List<Product> findAll() {
		return Collections.emptyList();
	}

}
