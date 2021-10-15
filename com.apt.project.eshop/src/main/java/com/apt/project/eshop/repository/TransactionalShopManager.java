package com.apt.project.eshop.repository;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;

// Transactional Mongo Shop Manager
public class TransactionalShopManager implements TransactionManager {

	MongoClient client;
	private String databaseName;
	private String collectionName;
	
	public TransactionalShopManager(MongoClient client, String databaseName, String collectionName) {
		this.client = client;
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		}
	
	@Override
	public <T> T doInTransaction(TransactionCode<T> code) {
		ClientSession session = client.startSession();
		// create a transaction
		session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
		// create a repository instance in the transaction
		ProductMongoRepository productRepository = new ProductMongoRepository(client, databaseName, collectionName);
		// call a lambda passing the repository instance
		code.apply(productRepository);
			
		session.commitTransaction();

		// close the transaction
        session.close();
        Logger.getLogger(getClass().getName())
		.log(Level.INFO, "####################################\n");

		return null;
	}

}
