package com.apt.project.eshop.view.swing;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.view.EShopView;

public class EShopSwingView extends JFrame implements EShopView{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JList<Product> productList;
	private DefaultListModel<Product> productListModel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EShopSwingView frame = new EShopSwingView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public EShopSwingView() {
		setTitle("eShop View");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 829, 401);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblProducts = new JLabel("Products");

		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup().addGap(36).addComponent(scrollPane,
										GroupLayout.PREFERRED_SIZE, 351, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_contentPane.createSequentialGroup().addGap(179).addComponent(lblProducts)))
						.addContainerGap(432, Short.MAX_VALUE)));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
						.addComponent(lblProducts, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
						.addGap(22)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(104, Short.MAX_VALUE)));

		productListModel = new DefaultListModel<>();
		productList = new JList<>(productListModel);
		productList.setName("productList");
		scrollPane.setViewportView(productList);
		contentPane.setLayout(gl_contentPane);
	}

}
