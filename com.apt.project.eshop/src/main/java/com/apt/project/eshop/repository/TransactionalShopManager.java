package com.apt.project.eshop.repository;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.eshop.repository.mongo.CartMongoRepository;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;

// Transactional Mongo Shop Manager
public class TransactionalShopManager implements TransactionManager {

	MongoClient client;
	private String databaseName;
	private String collectionName;
	private String cartCollectionName;
	
	public TransactionalShopManager(MongoClient client, String databaseName, String collectionName, String cartCollectionName) {
		this.client = client;
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		this.cartCollectionName = cartCollectionName;
		}
	
	@Override
	public <T> T doInTransaction(TransactionCode<T> code) {
		ClientSession session = client.startSession();
		try {
			// create a transaction
			session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
			// create a repository instance in the transaction
			ProductMongoRepository productRepository = new ProductMongoRepository(client, databaseName, collectionName, session);
			CartMongoRepository cartRepository = new CartMongoRepository(client, databaseName, cartCollectionName, session);
			// call a lambda passing the repository instance
			code.apply(productRepository);
				
			session.commitTransaction();
			Logger.getLogger(getClass().getName())
				.log(Level.INFO, "Successful transaction\n");
			
		} catch (MongoException e) {
			session.abortTransaction();
			Logger.getLogger(getClass().getName())
				.log(Level.INFO, "ROLLBACK TRANSACTION\n");
			
		} finally {
			// close the transaction
	        session.close();
	        Logger.getLogger(getClass().getName())
			.log(Level.INFO, "Transaction ended\n");
	    }
		return null;
	}

}