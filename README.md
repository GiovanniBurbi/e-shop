
![Linux Build Actions Status](https://github.com/GiovanniBurbi/e-shop/actions/workflows/maven-linux.yml/badge.svg)
![Mac Build Actions Status](https://github.com/GiovanniBurbi/e-shop/actions/workflows/maven-mac.yml/badge.svg)

<!-- [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GiovanniBurbi_e-shop&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=GiovanniBurbi_e-shop) -->
[![Coverage Status](https://coveralls.io/repos/github/GiovanniBurbi/e-shop/badge.svg)](https://coveralls.io/github/GiovanniBurbi/e-shop)
<!-- [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GiovanniBurbi_e-shop&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=GiovanniBurbi_e-shop)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=GiovanniBurbi_e-shop&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=GiovanniBurbi_e-shop)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=GiovanniBurbi_e-shop&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=GiovanniBurbi_e-shop)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=GiovanniBurbi_e-shop&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=GiovanniBurbi_e-shop)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=GiovanniBurbi_e-shop&metric=bugs)](https://sonarcloud.io/summary/new_code?id=GiovanniBurbi_e-shop)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=GiovanniBurbi_e-shop&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=GiovanniBurbi_e-shop) -->
 # eShop/Shopping Cart App
> Advanced programming techniques Project using BDD, Docker, MongoDB, Swing and CI.

## Prerequisites 
In the development of this project has been used these tools and related versions:
 * Java (>=8)
 * Maven (3.8.2)
 * Docker (20.10.8)
 * Docker compose (1.29.2)
## Brief introduction
The project eShop simulate a shopping cart of a very simple online shop. This app is developed in Java 8 using a MongoDB database with some of its advanced features and Java Swing for the GUI. The goal of this project is to use tools and techniques meant to increase productivity, the quality of the source code and its maintainability. All tools are wired together in the CI server provided by GitHub Actions through workflows files. The build automation process is managed by Maven. For more details on the development of the project and the tools used see the [Final Report](https://github.com/GiovanniBurbi/e-shop/blob/master/FinalReport.pdf).
## Getting started
### Using IDE
* Clone this repository in your IDE, Eclipse has been used for this project.
* To run the Maven build:
  * Run the root directory of the project as Maven build with goals:
    `clean verify`
  * To generate the code coverage report by enable the jacoco profile
    `clean verify -Pjacoco`
  * To generate the mutation testing report by enable the pitest profile
    `clean verify -Ppitest`
* To run the Java application
  * First start the container with MongoDB replica set:
     * Open a terminal and navigate inside the root directory of the project
     * Run the command:
       `docker-compose up`
   * In your IDE open the file "EShopSwingApp" and run it as Java application.
### Using CLI
* Clone this repository.
* To run the Maven build:
  * Navigate inside the root directory of the project
  * Run this Maven command:
    `mvn clean verify`
  * To generate the code coverage report by enable the jacoco profile
    `mvn clean verify -Pjacoco`
  * To generate the mutation testing report by enable the pitest profile
    `mvn clean verify -Ppitest`
* To run the Java application
  * First start the container with MongoDB replica set:
     * Open a terminal and navigate inside the root directory of the project
     * Run the command:
       `docker-compose up`
   * In a new terminal navigate inside the root directory of the project
   * Compile and create the FatJar of the application with:
     `mvn compile package`
   * Start the application with:
     `java -jar target/eshop-0.0.1-SNAPSHOT-jar-with-dependencies.jar`
       
If you are using Windows environment then is recommended to use linux containers. You can see this guide: https://docs.docker.com/desktop/windows/#switch-between-windows-and-linux-containers
