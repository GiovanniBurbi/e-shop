package com.apt.project.eshop.app.swing;

import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.mongo.ProductMongoRepository;
import com.apt.project.eshop.view.swing.EShopSwingView;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class EShopSwingApp {

	/**
	 *  Start MongoDB with Docker with
	 *
	 * <pre>
	 * docker run -p 27017:27017 --rm mongo:4.4.3
	 * </pre>
	 * 
	 * Launch the application.
	 * 
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				MongoClient client = new MongoClient(new ServerAddress("localhost", 27017));
				ProductMongoRepository productRepository = new ProductMongoRepository(client, "eShop", "products");
				productRepository.loadCatalog(new Product("1", "Laptop", 1300));
				EShopSwingView eShopView = new EShopSwingView();
				EShopController eShopController = new EShopController(productRepository, eShopView);
				eShopView.setVisible(true);
				eShopController.allProducts();
			} catch (Exception e) {
				Logger.getLogger(EShopSwingApp.class.getName())
				.log(Level.SEVERE, "Exception", e);
			}
		});
	}
}