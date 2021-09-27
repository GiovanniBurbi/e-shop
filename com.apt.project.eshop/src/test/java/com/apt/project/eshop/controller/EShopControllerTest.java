package com.apt.project.eshop.controller;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.apt.project.eshop.model.Product;
import com.apt.project.eshop.repository.ProductRepository;
import com.apt.project.eshop.view.EShopView;


public class EShopControllerTest {

	@Mock
	private ProductRepository productRepository;
	@Mock
	private EShopView eShopView;
	@InjectMocks
	private EShopController eShopController;
	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testAllProducts() {
		List<Product> products = asList(new Product("1", "laptop", 1300));
		given(productRepository.findAll()).willReturn(products);
		eShopController.allProducts();
		then(eShopView).should().showAllProducts(products);
	}

}
