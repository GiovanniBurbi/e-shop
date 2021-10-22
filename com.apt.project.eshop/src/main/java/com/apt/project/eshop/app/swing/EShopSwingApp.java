package com.apt.project.eshop.app.swing;

import java.awt.EventQueue;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.repository.ShopManager;
import com.apt.project.eshop.repository.TransactionalShopManager;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.apt.project.eshop.view.swing.EShopSwingView;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 *  Start MongoDB with replica set using Docker with
 *
 * <pre>
 * 
 * sudo docker run --name mongoRs -p 27017:27017 --rm mongo:4.4.3 --replSet rs0
 * 
 * 
 * +++ Open a new terminal and run +++
 * 
 * sudo docker exec -it mongo1 mongo
 * 
 * +++ finally +++
 * rs.initiate()
 * 
 * +++ To verify all is working you can run +++
 * rs.conf()
 * rs.status()
 * 
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
				TransactionalShopManager transactionManager = new TransactionalShopManager(client, databaseName, collectionName);
				ShopManager shopManager = new ShopManager(transactionManager);
				EShopController eShopController = new EShopController(productRepository, eShopView, shopManager);
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