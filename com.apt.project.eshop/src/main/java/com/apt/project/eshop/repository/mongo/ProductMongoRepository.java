package com.apt.project.eshop.repository.mongo;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.repository.RepositoryException;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class ProductMongoRepository implements ProductRepository {

	private MongoCollection<Product> productCollection;
	private MongoDatabase database;
	private ClientSession session;
	
	public ProductMongoRepository(MongoClient client, String databaseName, String collectionName, ClientSession session) {
		this.session = session;
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		database = client.getDatabase(databaseName);
		productCollection = database.getCollection(collectionName, Product.class).withCodecRegistry(pojoCodecRegistry);
	}
	
	@Override
	public List<Product> findAll() {
		return StreamSupport.stream(productCollection.find(session).spliterator(), false).collect(Collectors.toList());
	}

	@Override
	public void loadCatalog(List<Product> products) {
		database.drop();
		productCollection.insertMany(session, products);
	}

	@Override
	public List<Product> findByName(String nameSearch) {
		return StreamSupport.stream(
				productCollection
					.find(session, Filters.regex("name",Pattern.compile(nameSearch, Pattern.CASE_INSENSITIVE))).spliterator(), false)
					.collect(Collectors.toList()
		);
	}

	@Override
	public void removeFromStorage(Product product) throws RepositoryException {
		Bson filterNameProduct = Filters.eq("name", product.getName());
		int quantityToReduce = product.getQuantity();
		Product productInStorage = productCollection.find(session, filterNameProduct).first();
		int quantityInStorage = productInStorage.getQuantity();
		if (quantityInStorage < quantityToReduce)
			throw new RepositoryException("Insufficient stock", productInStorage);
		Bson update = Updates.inc("quantity", - quantityToReduce);
		productCollection.findOneAndUpdate(session, filterNameProduct,update);
	}

	@Override
	public boolean catalogIsEmpty() {
		return (productCollection.find(session).first() == null);
	}
}
