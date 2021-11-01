package com.apt.project.eshop.repository;

import java.util.List;

@FunctionalInterface
public interface TransactionCodeReturnList<T> {
	List<T> execute(ProductRepository productRepository, CartRepository cartRepository);
}
