package com.apt.project.eshop.management;

import com.apt.project.eshop.repository.CartRepository;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.repository.RepositoryException;

@FunctionalInterface
public interface TransactionCode<T> {
	T apply(ProductRepository productRepository, CartRepository cartRepository) throws RepositoryException;
}
