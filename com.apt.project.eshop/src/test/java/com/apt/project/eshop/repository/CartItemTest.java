package com.apt.project.eshop.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.apt.project.eshop.model.Product;

public class CartItemTest {

	@Test
	public void testToStringMethod() {
		CartItem item = new CartItem(new Product("1", "Laptop", 1300.0), 2);
		assertThat(item.toString()).isEqualTo(
			"CartItem [Product [id=1, name=Laptop, price=1300.0], quantity=2]"
		);
	}

}
