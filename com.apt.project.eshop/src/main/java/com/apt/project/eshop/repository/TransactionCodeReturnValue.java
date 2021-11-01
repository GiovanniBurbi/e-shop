package com.apt.project.eshop.repository;

@FunctionalInterface
public interface TransactionCodeReturnValue<T> {
	T execute(ProductRepository productRepository, CartRepository cartRepository);
}
