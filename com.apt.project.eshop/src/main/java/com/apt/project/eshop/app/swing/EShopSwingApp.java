package com.apt.project.eshop.app.swing;

import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apt.project.eshop.view.swing.EShopSwingView;

public class EShopSwingApp {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				EShopSwingView eShopView = new EShopSwingView();
				eShopView.setVisible(true);
			} catch (Exception e) {
				Logger.getLogger(EShopSwingApp.class.getName())
				.log(Level.SEVERE, "Exception", e);
			}
		});
	}
}