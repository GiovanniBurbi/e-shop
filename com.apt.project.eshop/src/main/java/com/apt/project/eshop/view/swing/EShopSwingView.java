package com.apt.project.eshop.view.swing;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.view.EShopView;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Color;

public class EShopSwingView extends JFrame implements EShopView{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JList<Product> productList;
	private DefaultListModel<Product> productListModel;
	private JTextField searchTextBox;
	private JButton btnSearch;
	private transient EShopController eShopController;
	private JLabel lblErrorLabel;
	private JButton btnClear;


	public DefaultListModel<Product> getProductListModel() {
		return productListModel;
	}
	
	public void setEShopController(EShopController eShopController) {
		this.eShopController = eShopController;
	}

	public JLabel getLblErrorLabel() {
		return lblErrorLabel;
	}
	
	public JButton getBtnClear() {
		return btnClear;
	}
	
	/**
	 * Create the frame.
	 */
	public EShopSwingView() {
		setTitle("eShop View");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 829, 401);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblProducts = new JLabel("Products");

		JScrollPane scrollPane = new JScrollPane();
		
		searchTextBox = new JTextField();
		searchTextBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnSearch.setEnabled(!searchTextBox.getText().trim().isEmpty());
				if(!getLblErrorLabel().getText().isEmpty())
					resetErrorLabel();
			}
		});
		searchTextBox.setName("searchTextBox");
		searchTextBox.setColumns(10);
		
		btnSearch = new JButton("Search");
		btnSearch.addActionListener(
			e -> eShopController.searchProducts(searchTextBox.getText())
		);
		btnSearch.setEnabled(false);
		
		lblErrorLabel = new JLabel("");
		getLblErrorLabel().setName("errorMessageLabel");
		getLblErrorLabel().setForeground(Color.RED);
		
		btnClear = new JButton("Clear");
		getBtnClear().setEnabled(false);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(37)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
								.addComponent(lblErrorLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(searchTextBox)))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(126)
							.addComponent(btnSearch)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getBtnClear(), GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(183)
							.addComponent(lblProducts)))
					.addContainerGap(431, Short.MAX_VALUE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(24)
					.addComponent(lblProducts, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(searchTextBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addComponent(getBtnClear()))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lblErrorLabel, GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
					.addGap(6))
		);

		productListModel = new DefaultListModel<>();
		productList = new JList<>(getProductListModel());
		productList.setName("productList");
		scrollPane.setViewportView(productList);
		contentPane.setLayout(gl_contentPane);
	}

	@Override
	public void showAllProducts(List<Product> products) {
		products.stream().forEach(getProductListModel()::addElement);
	}

	@Override
	public void showSearchedProducts(List<Product> searchedProducts) {
		productListModel.clear();
		searchedProducts.stream().forEach(productListModel::addElement);
		getBtnClear().setEnabled(true);
	}

	@Override
	public void showErrorProductNotFound(String product) {
		getLblErrorLabel()
			.setText("Nessun risultato trovato per: \"" + product + "\"");
	}
	
	public void resetErrorLabel() {
		getLblErrorLabel().setText("");
	}

	@Override
	public void clearSearch(List<Product> products) {
		getBtnClear().setEnabled(false);
		searchTextBox.setText("");
		productListModel.clear();
		showAllProducts(products);
	}
}
