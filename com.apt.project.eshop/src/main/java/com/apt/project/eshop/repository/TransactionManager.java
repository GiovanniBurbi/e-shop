package com.apt.project.eshop.repository;

public interface TransactionManager {
	<T> T doInTransaction(TransactionCode<T> code);	
}
