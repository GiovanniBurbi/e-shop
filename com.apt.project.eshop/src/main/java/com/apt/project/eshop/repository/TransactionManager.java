package com.apt.project.eshop.repository;

import java.util.List;

import com.apt.project.eshop.model.Product;

public interface TransactionManager {
	<T> T doInTransaction(TransactionCode<T> code);
	
	List<Product> doInTransactionAndReturnList(TransactionCodeReturnList<Product> code);
}
