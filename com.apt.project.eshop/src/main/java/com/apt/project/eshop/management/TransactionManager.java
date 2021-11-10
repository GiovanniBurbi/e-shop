package com.apt.project.eshop.management;

public interface TransactionManager {
	<T> T doInTransaction(TransactionCode<T> code);
}
