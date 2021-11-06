package com.apt.project.eshop.repository;

@FunctionalInterface
public interface TransactionCode<T> {
	T apply(ProductRepository productRepository, CartRepository cartRepository) throws RepositoryException;
}
