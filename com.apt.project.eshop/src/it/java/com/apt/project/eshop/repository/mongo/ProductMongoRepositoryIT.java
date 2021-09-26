package com.apt.project.eshop.repository.mongo;

import static org.junit.Assert.fail;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

public class ProductMongoRepositoryIT {
	
	private static final String PRODUCTS_COLLECTION_NAME = "products";
	private static final String ESHOP_DB_NAME = "eShop";
	
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongo = new GenericContainer("mongo:4.4.3").withExposedPorts(27017);

	@Test
	public void test() {
		fail("Not yet implemented");
	}
}
