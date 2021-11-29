package com.apt.project.eshop.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CartItemTest {

	@Test
	public void testToStringMethod() {
		CartItem item = new CartItem(new Product("1", "Laptop", 1300.0), 2);
		assertThat(item.toString()).hasToString(
			"CartItem [Product [id=1, name=Laptop, price=1300.0], quantity=2]"
		);
	}

}
