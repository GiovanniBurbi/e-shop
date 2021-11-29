package com.apt.project.eshop.repository;

import com.apt.project.eshop.model.CatalogItem;

public class RepositoryException extends Exception {

	private static final long serialVersionUID = 1L;
	private static final String REPOSITORY_EXCEPTION_MESSAGE = "Repository exception! ";
	private final transient CatalogItem item;

	public RepositoryException(String message, CatalogItem item) {
		super(REPOSITORY_EXCEPTION_MESSAGE + message + ", " + item.getProduct().getName() + " left in stock: "
				+ item.getStorage());
		this.item = item;
	}

	public CatalogItem getItem() {
		return item;
	}
}