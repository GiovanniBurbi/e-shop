package com.apt.project.eshop.app.swing;

import static java.util.Arrays.asList;

import java.awt.EventQueue;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.management.ShopManager;
import com.apt.project.eshop.management.mongo.TransactionalShopManager;
import com.apt.project.eshop.model.CatalogItem;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.view.swing.EShopSwingView;
import com.mongodb.MongoClient;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Start MongoDB with replica set using Docker with
 *
 * <pre>
 * 
 * sudo docker run --name mongoRs -p 27017:27017 --rm mongo:4.4.3 --replSet rs0
 * 
 * 
 * +++ Open a new terminal and run +++
 * 
 * sudo docker exec -it mongoRs mongo --eval 'rs.initiate()'
 * 
 * </pre>
 * 
 * Launch the application.
 * 
 * @author Giovanni Burbi
 * 
 */
@Command(mixinStandardHelpOptions = true)
public class EShopSwingApp implements Callable<Void> {

	private static final String MONGO_HOST = "localhost";

	private static final int MONGO_PORT = 27017;

	@Option(names = { "--db-name" }, description = "Database name")
	private String databaseName = "eShop";

	@Option(names = { "--db-product-collection" }, description = "Product collection name")
	private String productCollectionName = "products";

	@Option(names = { "--db-cart-collection" }, description = "Cart collection name")
	private String cartCollectionName = "cart";

	public static void main(String[] args) {
		new CommandLine(new EShopSwingApp()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(() -> {
			try {
				MongoClient client = new MongoClient(MONGO_HOST, MONGO_PORT);
				EShopSwingView eShopView = new EShopSwingView();
				TransactionalShopManager transactionManager = new TransactionalShopManager(client, databaseName,
						productCollectionName, cartCollectionName);
				ShopManager shopManager = new ShopManager(transactionManager);
				EShopController eShopController = new EShopController(eShopView, shopManager);
				shopManager.setShopController(eShopController);
				eShopView.setEShopController(eShopController);
				// Load Catalog if product list is empty
				if (shopManager.allProducts().isEmpty()) {
					shopManager.loadCatalog(asList(
						new CatalogItem(new Product("1", "Laptop", 1300.0), 3),
						new CatalogItem(new Product("2", "Iphone", 1000.0), 3),
						new CatalogItem(new Product("3", "Laptop MSI", 1250.0), 3),
						new CatalogItem(new Product("4", "Macbook", 1400.0), 3),
						new CatalogItem(new Product("5", "SmartTv", 400.0), 3),
						new CatalogItem(new Product("6", "Playstation 5", 500.0), 3),
						new CatalogItem(new Product("7", "Xbox", 500.0), 3)
					));
				}
				eShopView.setVisible(true);
				eShopController.allProducts();
				eShopController.showCart();
				eShopController.showCartCost();
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception", e);
			}
		});
		return null;
	}
}