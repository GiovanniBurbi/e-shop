Feature: eShop View
  Specifications of the behavior of the eShop View
  
  Scenario: The initial state of the view
    Given The database contains a product with id "1", name "Laptop" and price 1300.0
    When The eShop View is shown
    Then The product list contains an element with id "1", name "Laptop" and price 1300.0
