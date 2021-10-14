package com.apt.project.eshop.view.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.apt.project.eshop.controller.EShopController;
import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.view.EShopView;

public class EShopSwingView extends JFrame implements EShopView {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JList<Product> productList;
	private DefaultListModel<Product> productListModel;
	private JTextField searchTextBox;
	private JButton btnSearch;
	private transient EShopController eShopController;
	private JLabel lblErrorLabel;
	private JButton btnClear;
	private JList<Product> cartList;
	private DefaultListModel<Product> cartListModel;
	private JButton btnRemoveFromCart;
	private JLabel totalCostLabel;

	private JButton btnCheckout;

	public JLabel getTotalCostlabel() {
		return totalCostLabel;
	}

	public DefaultListModel<Product> getCartListModel() {
		return cartListModel;
	}

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
		setBounds(100, 100, 829, 449);
		contentPane = new JPanel();
		contentPane.setName("contentPane");
		contentPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				productList.clearSelection();
				cartList.clearSelection();
			}
		});
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblProducts = new JLabel("Products");

		JScrollPane scrollPane = new JScrollPane();

		searchTextBox = new JTextField();
		searchTextBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnSearch.setEnabled(!searchTextBox.getText().trim().isEmpty());
				if (!getLblErrorLabel().getText().isEmpty())
					resetErrorLabel();
			}
		});
		searchTextBox.setName("searchTextBox");
		searchTextBox.setColumns(10);

		btnSearch = new JButton("Search");
		btnSearch.addActionListener(e -> eShopController.searchProducts(searchTextBox.getText()));
		btnSearch.setEnabled(false);

		lblErrorLabel = new JLabel("");
		getLblErrorLabel().setName("errorMessageLabel");
		getLblErrorLabel().setForeground(Color.RED);

		btnClear = new JButton("Clear");
		btnClear.addActionListener(e -> eShopController.resetSearch());
		getBtnClear().setEnabled(false);

		JButton btnAddToCart = new JButton("Add To Cart");
		btnAddToCart.addActionListener(e -> eShopController.newCartProduct(productList.getSelectedValue()));
		btnAddToCart.setEnabled(false);

		JScrollPane scrollPane_1 = new JScrollPane();

		JLabel lblCart = new JLabel("Cart");

		btnRemoveFromCart = new JButton("Remove From Cart");
		btnRemoveFromCart.addActionListener(e -> eShopController.removeCartProduct(cartList.getSelectedValue()));
		btnRemoveFromCart.setEnabled(false);

		JLabel lblTotal = new JLabel("Total: ");

		totalCostLabel = new JLabel("0.0$");
		getTotalCostlabel().setName("totalCostLabel");
		getTotalCostlabel().setFont(new Font("Dialog", Font.PLAIN, 12));

		btnCheckout = new JButton("Checkout");
		btnCheckout.setName("");
		btnCheckout.setEnabled(false);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
				.createSequentialGroup()
				.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup().addGap(37)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
										.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
										.addComponent(searchTextBox).addComponent(lblErrorLabel,
												GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addGroup(gl_contentPane.createSequentialGroup().addGap(126).addComponent(btnSearch)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnClear, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createSequentialGroup().addGap(155).addComponent(btnAddToCart)))
				.addPreferredGap(ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
				.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(gl_contentPane
						.createSequentialGroup()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 341, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_contentPane.createSequentialGroup().addGap(12)
										.addComponent(btnRemoveFromCart).addGap(35)
										.addComponent(lblTotal, GroupLayout.PREFERRED_SIZE, 46,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(totalCostLabel,
												GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap()).addGroup(
								gl_contentPane.createSequentialGroup()
										.addComponent(btnCheckout, GroupLayout.PREFERRED_SIZE, 134,
												GroupLayout.PREFERRED_SIZE)
										.addGap(47))))
				.addGroup(gl_contentPane.createSequentialGroup().addGap(183).addComponent(lblProducts)
						.addPreferredGap(ComponentPlacement.RELATED, 369, Short.MAX_VALUE).addComponent(lblCart)
						.addGap(179)));
		gl_contentPane
				.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup().addGap(24)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblProducts, GroupLayout.PREFERRED_SIZE, 34,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lblCart))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_contentPane.createSequentialGroup()
												.addComponent(searchTextBox, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
														.addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 25,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(btnClear))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 191,
														GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
												.addComponent(btnAddToCart).addGap(18).addComponent(lblErrorLabel,
														GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
										.addGroup(gl_contentPane.createSequentialGroup()
												.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 175,
														GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
														.addComponent(btnRemoveFromCart)
														.addComponent(lblTotal, GroupLayout.DEFAULT_SIZE, 32,
																Short.MAX_VALUE)
														.addComponent(totalCostLabel))
												.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnCheckout)
												.addGap(99)))
								.addContainerGap()));

		cartListModel = new DefaultListModel<>();
		cartListModel.addListDataListener(new CartListDataListener());
		cartList = new JList<>(getCartListModel());
		cartList.addListSelectionListener(e -> {
			btnRemoveFromCart.setEnabled(cartList.getSelectedIndex() != -1);
			if (e.getValueIsAdjusting()) {
				productList.clearSelection();
			}
		});
		cartList.setCellRenderer(new CartTextRenderer());
		cartList.setName("cartList");
		cartList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane_1.setViewportView(cartList);

		productListModel = new DefaultListModel<>();
		productList = new JList<>(getProductListModel());
		productList.addListSelectionListener(e -> {
			btnAddToCart.setEnabled(productList.getSelectedIndex() != -1);
			if (e.getValueIsAdjusting()) {
				cartList.clearSelection();
			}
		});
		productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
		getLblErrorLabel().setText("Nessun risultato trovato per: \"" + product.trim() + "\"");
	}

	public void resetErrorLabel() {
		getLblErrorLabel().setText("");
	}

	@Override
	public void clearSearch(List<Product> products) {
		getBtnClear().setEnabled(false);
		searchTextBox.setText("");
		resetErrorLabel();
		productListModel.clear();
		showAllProducts(products);
	}

	@Override
	public void addToCartView(List<Product> products) {
		getCartListModel().clear();
		products.stream().forEach(getCartListModel()::addElement);
	}

	@Override
	public void removeFromCartView(Product product) {
		cartListModel.removeElement(product);
	}

	@Override
	public void updateTotal(double price) {
		String actualTotalString = getTotalCostlabel().getText();
		double actualTotal = Double.parseDouble(actualTotalString.substring(0, actualTotalString.lastIndexOf("$")));
		totalCostLabel.setText(String.valueOf(actualTotal + price) + "$");

	}

	class CartTextRenderer extends JLabel implements ListCellRenderer<Product> {

		private static final long serialVersionUID = 1L;

		public CartTextRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Product> list, Product product, int index,
				boolean isSelected, boolean cellHasFocus) {
			String nameProduct = product.toStringExtended();
			setText(nameProduct);
			if (isSelected) {
				setForeground(list.getSelectionForeground());
				setBackground(list.getSelectionBackground());
			} else {
				setForeground(list.getForeground());
				setBackground(list.getBackground());
			}
			return this;
		}
	}

	class CartListDataListener implements ListDataListener {
		public void contentsChanged(ListDataEvent e) {	
			// no needed for now
		}

		public void intervalAdded(ListDataEvent e) {
			if(!(btnCheckout.isEnabled()))
				btnCheckout.setEnabled(true);
		}

		public void intervalRemoved(ListDataEvent e) {
			if (cartListModel.isEmpty())
				btnCheckout.setEnabled(false);
		}
	}

}
