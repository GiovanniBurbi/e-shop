package com.apt.project.eshop.repository;

import java.util.function.Function;

@FunctionalInterface
public interface TransactionCode<T> extends Function<ProductRepository, T> {

}
