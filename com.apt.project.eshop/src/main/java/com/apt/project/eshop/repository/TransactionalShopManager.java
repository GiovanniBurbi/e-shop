package com.apt.project.eshop.repository;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.eshop.repository.mongo.CartMongoRepository;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;

// Transactional Mongo Shop Manager
public class TransactionalShopManager implements TransactionManager {

	private static final String ROLLBACK_TRANSACTION = "ROLLBACK TRANSACTION\n";
	private static final String TRANSACTION_ENDED = "Transaction ended\n";
	private static final String SUCCESSFUL_TRANSACTION = "Successful transaction\n";
	MongoClient client;
	private String databaseName;
	private String productCollectionName;
	private String cartCollectionName;

	public TransactionalShopManager(MongoClient client, String databaseName, String productCollectionName,
			String cartCollectionName) {
		this.client = client;
		this.databaseName = databaseName;
		this.productCollectionName = productCollectionName;
		this.cartCollectionName = cartCollectionName;
	}

	@Override
	public <T> T doInTransaction(TransactionCode<T> code) {
		ClientSession session = client.startSession();
		try {
			// create a transaction
			session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
			// create a repository instance in the transaction
			ProductMongoRepository productRepository = new ProductMongoRepository(client, databaseName, productCollectionName, session);
			CartMongoRepository cartRepository = new CartMongoRepository(client, databaseName, cartCollectionName, session);
			// call a lambda passing the repository instance
			T result = code.apply(productRepository, cartRepository);
				
			session.commitTransaction();
			Logger.getLogger(getClass().getName())
				.log(Level.INFO, SUCCESSFUL_TRANSACTION);
			return result;
			
		} catch (RepositoryException e) {
			session.abortTransaction();
			Logger.getLogger(getClass().getName())
				.log(Level.INFO, ROLLBACK_TRANSACTION);
			
		} finally {
			// close the transaction
	        session.close();
	        Logger.getLogger(getClass().getName())
			.log(Level.INFO, TRANSACTION_ENDED);
	    }
		return null;
	}
}