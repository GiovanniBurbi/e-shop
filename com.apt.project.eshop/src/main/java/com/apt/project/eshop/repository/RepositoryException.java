package com.apt.project.eshop.repository;

import com.apt.project.eshop.model.Product;

public class RepositoryException extends Exception {

	
	private static final long serialVersionUID = 1L;
	private static final String REPOSITORY_EXCEPTION_MESSAGE = "Repository exception! ";
	private Product product;
	
	public RepositoryException(Exception e) {
        super(REPOSITORY_EXCEPTION_MESSAGE, e);
    }

	public RepositoryException(String message, Product product) {
        super(REPOSITORY_EXCEPTION_MESSAGE + message + ", " + product.getName() + " left in stock: " + product.getQuantity());
		this.product = product;
    }

	public Product getProduct() {
		return product;
	}
}