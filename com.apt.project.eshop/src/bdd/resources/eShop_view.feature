Feature: eShop View
  Specifications of the behavior of the eShop View
  
   Scenario: The initial state of the view
  	Given The database contains products with the following values
  	  | "1" | "Laptop" | 1300.0 |
  	  | "2" | "Iphone" | 1000.0 |
  	  | "3" | "Bose" | 250.0 | 
    When The eShop View is shown
    Then The list contains an element with the following values
      | "1" | "Laptop" | 1300.0 |
  	  | "2" | "Iphone" | 1000.0 |
  	  | "3" | "Bose" | 250.0 |
