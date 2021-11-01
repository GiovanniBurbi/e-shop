package com.apt.project.eshop.repository;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.eshop.model.Product;
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
			code.apply(productRepository, cartRepository);
				
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

	@Override
	public List<Product> doInTransactionAndReturnList(TransactionCodeReturnList<Product> code) {
		ClientSession session = client.startSession();
		// create a transaction
		session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
		// create a repository instance in the transaction
		ProductMongoRepository productRepository = new ProductMongoRepository(client, databaseName,
				productCollectionName, session);
		CartMongoRepository cartRepository = new CartMongoRepository(client, databaseName, cartCollectionName, session);
		// call a lambda passing the repository instance
		List<Product> products = code.execute(productRepository, cartRepository);

		session.commitTransaction();
		Logger.getLogger(getClass().getName()).log(Level.INFO, "Successful transaction\n");
		// close the transaction
		session.close();
		Logger.getLogger(getClass().getName()).log(Level.INFO, "Transaction ended\n");
		return products;
	}

	@Override
	public double doInTransactionAndReturnValue(TransactionCodeReturnValue<Double> code) {
		ClientSession session = client.startSession();
		// create a transaction
		session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
		// create a repository instance in the transaction
		ProductMongoRepository productRepository = new ProductMongoRepository(client, databaseName,
				productCollectionName, session);
		CartMongoRepository cartRepository = new CartMongoRepository(client, databaseName, cartCollectionName, session);
		// call a lambda passing the repository instance
		double value = code.execute(productRepository, cartRepository);

		session.commitTransaction();
		Logger.getLogger(getClass().getName()).log(Level.INFO, "Successful transaction\n");
		// close the transaction
		session.close();
		Logger.getLogger(getClass().getName()).log(Level.INFO, "Transaction ended\n");
		return value;
	}

}