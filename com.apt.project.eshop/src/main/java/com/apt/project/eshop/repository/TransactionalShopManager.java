package com.apt.project.eshop.repository;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;

public class TransactionalShopManager implements TransactionManager {

	MongoClient client;
	private String databaseName;
	private String collectionName;
	
	public TransactionalShopManager(MongoClient client, String databaseName, String CollectionName) {
		this.client = client;
		this.databaseName = databaseName;
		this.collectionName = CollectionName;
		}
	
	@Override
	public <T> T doInTransaction(TransactionCode<T> code) {
		ClientSession session = client.startSession();
		// create a transaction
		try {
			session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
			// create a repository instance in the transaction
			ProductMongoRepository productRepository = new ProductMongoRepository(client, databaseName, collectionName);
			// call a lambda passing the repository instance
			code.apply(productRepository);
			
			session.commitTransaction();
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception", e);
			// if something goes wrong the roll-back
			session.abortTransaction();
			System.out.println("####### ROLLBACK TRANSACTION #######");
		} finally {
			// close the transaction
	        session.close();
	        System.out.println("####################################\n");
	    }
		return null;
	}

}
