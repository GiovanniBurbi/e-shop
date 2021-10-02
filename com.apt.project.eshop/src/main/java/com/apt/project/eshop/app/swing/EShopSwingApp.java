package com.apt.project.eshop.app.swing;

import java.awt.EventQueue;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.apt.project.eshop.view.swing.EShopSwingView;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 *  Start MongoDB with Docker with
 *
 * <pre>
 * docker run -p 27017:27017 --rm mongo:4.4.3
 * </pre>
 * 
 * Launch the application.
 */
@Command(mixinStandardHelpOptions = true)
public class EShopSwingApp implements Callable<Void> {
	
	@Option(names = { "--mongo-host" }, description = "MongoDB host address")
	private String mongoHost = "localhost";

	@Option(names = { "--mongo-port" }, description = "MongoDB host port")
	private int mongoPort = 27017;

	@Option(names = { "--db-name" }, description = "Database name")
	private String databaseName = "eShop";

	@Option(names = { "--db-collection" }, description = "Collection name")
	private String collectionName = "products";
	
	public static void main(String[] args) {
		new CommandLine(new EShopSwingApp()).execute(args);
	}
	
	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(() -> {
			try {
				MongoClient client = new MongoClient(new ServerAddress(mongoHost, mongoPort));
				ProductMongoRepository productRepository = new ProductMongoRepository(client, databaseName,	collectionName);
				EShopSwingView eShopView = new EShopSwingView();
				EShopController eShopController = new EShopController(productRepository, eShopView);
				eShopView.setEShopController(eShopController);
				eShopView.setVisible(true);
				eShopController.allProducts();
			} catch (Exception e) {
				Logger.getLogger(getClass().getName())
				.log(Level.SEVERE, "Exception", e);
			}
		});
		return null;
	}
}