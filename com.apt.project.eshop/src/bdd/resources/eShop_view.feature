Feature: eShop View
  Specifications of the behavior of the eShop View
  
  Background: 
  	Given The database contains products with the following values
  	  | id | name | price |
  	  | "1" | "Laptop" | 1300.0 |
  	  | "2" | "Iphone" | 1000.0 |
  	  | "3" | "Laptop MSI" | 1250.0 |
  	  | "4" | "Macbook" | 1400.0 |
  	  | "5" | "SmartTv UHD" | 400.0 |
  	  | "6" | "Dyson phon" | 350.0 |
  	  | "7" | "Playstation 5" | 500.0 |
  	When The eShop View is shown
  	
  Scenario: The initial state of the view
    Then The list contains elements with the following values
      | id | name | price |
  	  | "1" | "Laptop" | 1300.0 |
  	  | "2" | "Iphone" | 1000.0 |
  	  | "3" | "Laptop MSI" | 1250.0 |
  	  | "4" | "Macbook" | 1400.0 |
  	  | "5" | "SmartTv UHD" | 400.0 |
  	  | "6" | "Dyson phon" | 350.0 |
  	  | "7" | "Playstation 5" | 500.0 |

	Scenario: Search a product
		Given The user enters in the search text field the name "la" 
  	When The user clicks the "Search" button
  	Then The list shows products with "la" in the name
	
  Scenario: Search a not existing product
  	Given The user enters in the search text field the name "samsung s21"
  	When The user clicks the "Search" button
  	Then An error is shown containing the name searched "samsung s21"
   		
  Scenario: Clear the search
  	Given The user search the product "laptop"
  	When The user clicks the "Clear" button
  	Then The list contains elements with the following values
  	  | id | name | price |
  	  | "1" | "Laptop" | 1300.0 |
  	  | "2" | "Iphone" | 1000.0 |
  	  | "3" | "Laptop MSI" | 1250.0 |
  	  | "4" | "Macbook" | 1400.0 |
  	  | "5" | "SmartTv UHD" | 400.0 |
  	  | "6" | "Dyson phon" | 350.0 |
  	  | "7" | "Playstation 5" | 500.0 |
  	And The search text box is empty
  	
  	Scenario: Add Product to Cart
  	Given The user select a product from the product list
  	When The user clicks the "Add To Cart" button 2 times
  	Then The cart list contains an element with the following values
      | id | name | price | quantity |
      | "1" | "Laptop" | 1300.0 | 2 |
  	
  	