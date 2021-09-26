package com.apt.project.eshop.app.swing;

import java.awt.EventQueue;

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
				e.printStackTrace();
			}
		});
	}
}