package com.apt.project.eshop.model;

import java.util.Objects;

public class CatalogItem {
	
	private Product product;
	private int storage;
	
	public CatalogItem(Product product, int storage) {
		this.product = product;
		this.storage = storage;
	}

	public Product getProduct() {
		return product;
	}

	public int getStorage() {
		return storage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(product, storage);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CatalogItem other = (CatalogItem) obj;
		return Objects.equals(product, other.product) && storage == other.storage;
	}
}
