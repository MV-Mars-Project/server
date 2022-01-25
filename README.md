# Martian Travel Tube System - SERVER

###### This is the server of the Martian Travel Tube System

[Back to documentation repo](https://github.com/MV-Mars-Project/documentation)

## Table of Contents

- [Introduction](#introduction)
- [Getting Started](#getting%20Started)
- [Usage](#Usage)
- [Features](#features)
- [API](#API)
- [Description](#Description)

## Introduction

Together with a team of researchers and developers, we want to build an open web application that will allow users/clients to travel to different
endpoints on Mars and allow businesses to use pods for product transportation.

## Getting Started

### Prerequisites

Make sure you have:

* [Java 11 SDK](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
* [Gradle](https://gradle.org/)

Clone the project!

```shell script
git clone https://git.ti.howest.be/TI/2020-2021/s3/project-ii/projects/groep-15/server.git
```

## Usage

### Starting the server

To start the server simple press 'run' in the gradle tab > Tasks > application > run on the right-hand side (intelliJ) or use the terminal and
execute:

```shell script
gradle run
```

## Features

H2 database is generated when the server starts so no other actions are needed.  
The following scripts will run automatically when deploying the server:

[Setup DB Script](src/main/resources/h2/setupDB.sql)  
[Initialise Endpoints Script](src/main/resources/h2/initEndpointsDB.sql)

## API

We use an openAPI version(3.0.0). We use [this openAPI](https://github.com/MV-Mars-Project/server/blob/master/src/main/resources/openapi-group-15.yaml) to establish communication between the server and our client.

### Using the API

You can use tools like [Postman](https://www.postman.com/) or your browser (only for GET methods).

## Description

![Diagram](https://svgshare.com/i/S00.svg)

### WebServer

When the WebServer starts it generates the H2 database, after which it loads a yaml file located in the resources folder. The yaml file contains the
OpenAPI3 specification of our REST API. This file adds all routes to the router. Then it adds the Logger, the cors handler, security
handlers, SockJSHandler and lastly the error handlers.

The cors handler allows Cross-Origin Resource Sharing (CORS) on each incoming request. We add three security handlers: for users and business only
or combined. Based on the bearer token given by each request, validation occurs. We also add a SockJSHandler which handles
all requests incoming on the eventbus on the `events.` address. The SockJSHandler allows the clients to listen to specific channels on the eventbus, on
which they will receive their notifications. The error handlers add the handlers for all the non 2XX status code responses listed in the
specification. It sets the right status code, message and cause of the error.

For errors thrown by the request which aren't handled or caught by previous handlers, it returns an internal server error. This indicates a failed handling of the incoming request by the server. The WebServer also links the correct bridge functions to the REST API
paths. The WebServer starts a daily timer as well, which resets the amount of used pods by the businesses.

### Bridge

Each REST API path has method connected to it, the `Bridge` takes all the relevant data from the context and validates it and passes it on to
the controller.

The Bridge also contains the methods to validate the AccountTokens.

### Auth Controller

The `Auth Controller` is an Abstract super class of MTTS Controller. It contains methods to authenticate accounts, createAccounts and logout accounts.

### MTTS Controller

The `MTTS Controller` extends Auth Controller. It contains methods which do additional validation, redirects to the right repository and creates
JsonObjects which are the responses defined in the specification.

### Repositories

`Repositories` is a utility class which contains all instantiations of the other repositories with static accessor methods. This way we only keep one
instance of each Repository. For each repository we have an interface and an H2 database implementation of that interface.

#### AccountsRepository

The `AccountsH2Repository` contains methods to retrieve, add or change properties of users and businesses and the necassary SQL
queries for each method.

#### FriendsRepository

The `FriendsH2Repository` contains methods to retrieve, add or remove friends and the necessary SQL queries.

#### TravelsRepository

The `TravelsH2Repository` contains methods to travel to an endpoint, cancel a trip and get the travel history of a user.

#### EndpointsRepository

The `EndpointsH2Repository` contains methods to retrieve and add endpoints.

#### DeliveriesRepository

The `DeliveriesH2Repository` contains methods to retrieve or add deliveries and get delivery information.

#### FavoritesRepository

The `FavoritesH2Repository` contains methods to retrieve, add or remove favorite endpoints.

#### SubscriptionsRepository

The `SubscriptionsH2Repository` contains methods to retrieve business and user subscriptions or info. It also contains methods to set user and business subscriptions and a method to reset the used amount of pods of a
business.

#### ReportsRepository

The `ReportsH2Repository` contains methods to add reports and get the report sections. A report needs a section and description of the report for that
section.

### H2 Database

The `MarsConnection` util class creates the H2 Database. It uses the [Singleton](https://en.wikipedia.org/wiki/Singleton_pattern) pattern so that only
one instance of the class can exist. It has a static `Configure` method which creates the actual H2 database using the parameters and stores those
inside the `MarsConnection` class. Further it has a static `getConnection` method which returns the current connection to the H2 Database.
`Configure` also calls a private method which reads an sql file from the resources/h2 folder which initialize all the tables in fifth normal form, and it has all the constraints to keep the database consistent (ACID). 
After database creation the script **adds the following data to the database**:

* User subscriptions
* Business subs
* Report sections
* Delivery types
* Pod types
* Test account

Then a separate script is ran **adding all the endpoints**.

These scripts are located in the resources/h2 folder.

#### Database tables:

* **Accounts**  
Contains account name, encrypted password, home address and home endpoint id
* **Businesses**  
Contains business name, subscription id and pod usage counter
* **Business_subscriptions**  
Contains the different business subscription data
* **Deliveries**  
Contains delivery information such as type, origin, destination, date and sender data
* **Deliverytypes**    
Contains small or large delivery type
* **Endpoints**  
Contains all endpoint data
* **Favorite_endpoints**  
Contains accountnames coupled with their favorited endpoints
* **Friends**  
Contains usernames and their friendnames
* **Podtypes**  
Contains data about the different pod types
* **Potential_friends**  
Contains non confirmed friends data
* **Reports**  
Contains data from sent in reports including which section te report is about
* **Report_sections**  
Contains data per reporting section
* **Travels**  
Contains all travel data such as origin, destination, username, date, pod types and ETA
* **Users**  
Contains all user data such as name, displayname, shareslocation and subscriptionid
* **User_subscriptions**  
Contains the different user subscription data

***

*Written by Maarten Vercruysse and Daniel Vlaeminck*
