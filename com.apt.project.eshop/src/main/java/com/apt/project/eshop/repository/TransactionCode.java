package com.apt.project.eshop.repository;

import java.util.function.BiFunction;

@FunctionalInterface
public interface TransactionCode<T> extends BiFunction<ProductRepository, CartRepository, T> {

}
