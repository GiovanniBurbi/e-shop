Feature: eShop View
  Specifications of the behavior of the eShop View
  
   Scenario: The initial state of the view
  	Given The database contains products with the following values
  	  | id | name | price |
  	  | "1" | "Laptop" | 1300.0 |
  	  | "2" | "Iphone" | 1000.0 |
  	  | "3" | "Bose" | 250.0 | 
    When The eShop View is shown
    Then The list contains elements with the following values
      | id | name | price |
      | "1" | "Laptop" | 1300.0 |
  	  | "2" | "Iphone" | 1000.0 |
  	  | "3" | "Bose" | 250.0 |

  Scenario: Search a product
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
  	And The user enters in the search text field the name "la" 
  	And The user clicks the "Search" button
  	Then The list shows products with "la" in the name
  	
  	
  	