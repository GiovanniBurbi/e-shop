package com.apt.project.eshop.repository;

import java.util.List;

import com.apt.project.eshop.model.Product;

public interface ProductRepository {

	List<Product> findAll();

}
