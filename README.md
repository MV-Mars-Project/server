# Martian Travel Tube System - SERVER

###### This is the server of the Martian Travel Tube System

## Table of Contents

- [Introduction](#introduction)
- [Getting Started](#Getting Started)
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

H2 database gets generated when the application starts so no other actions are needed.

## API

We use an openAPI version(3.0.0) we use this API to establish communication between the server and our client.

### Using the API

You can use tools like Postman or your browser (only for GET methods). For information about our endpoints you can look them
up [here](https://git.ti.howest.be/TI/2020-2021/s3/project-ii/projects/groep-15/server/-/blob/master/src/main/resources/openapi-group-15.yaml).

## Description

![Diagram](https://svgshare.com/i/S00.svg)

### WebServer

When the WebServer starts it first creates the H2 database, after that it loads a yaml file located in the resources folder. The yaml file contains an
OpenAPI3 specification of our REST API. Based on that file it adds all the routes to the router. Next it adds the Logger, the cors handler, security
handlers, SockJSHandler and as last the error handlers.

The cors handler allows Cross-Origin Resource Sharing (CORS) on each incoming request. We add three security handlers: for users only, for businesses
only and for businesses or users. Based on the bearer token giving by each request it validates the request. We also add a SockJSHandler which handles
all requests incoming on the eventbus on the `events.` address. The SockJSHandler allows the clients to listen to specific channels on the eventbus on
which they will receive their notifications. The error handlers adds the error handlers for all the non 2XX status code responses listed in the
specification. It sets the right status code, message and the cause of the error.

For errors thrown by the request which aren't handled/caught by previous handlers it will return an internal server error. Which shouldn't be
happening but indicates a failed handling of the incoming request by the server. The WebServer also links the correct bridge functions to the REST API
paths. The WebServer also starts a daily timer which resets the amount of used pods by the businesses.

### Bridge

Each REST API path has method connected to it, the bridge pulls out all the relevant data from the context and validates it and then passes it on to
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

The `AccountsH2Repository` contains methods to retrieve users and businesses, to add users and businesses, change properties of both, and the SQL
queries for each method.

#### FriendsRepository

The `FriendsH2Repository` contains methods to retrieve the friends, add friends, remove friends, and the necessary SQL queries.

#### TravelsRepository

The `TravelsH2Repository` contains methods to travel to an endpoint, cancel a travel and get the travel history of a user.

#### EndpointsRepository

The `EndpointsH2Repository` contains methods to retrieve endpoints and add them.

#### DeliveriesRepository

The `DeliveriesH2Repository` contains methods to retrieve deliveries, add deliveries and get delivery information.

#### FavoritesRepository

The `FavoritesH2Repository` contains methods to retrieve favorite endpoints, add favorites and remove favorites.

#### SubscriptionsRepository

The `SubscriptionsH2Repository` contains methods to retrieve business subscriptions, user subscriptions, user subscription info and business
subscription info. It also contains methods to set user subscriptions and business subscriptions, and method to reset the used amount of pods of a
business.

#### ReportsRepository

The `ReportsH2Repository` contains methods to add reports and get the report sections. A report needs section and a description of the report for that
section.

### H2 Database

The `MarsConnection` util class creates the H2 Database. It uses the [Singleton](https://en.wikipedia.org/wiki/Singleton_pattern) pattern so that only
one instance of the class can exist. It has a static `configure` method which creates the actual H2 database using the parameters and stores those
inside the `MarsConnection` class. Further it has a static `getConnection` method which returns the current connection to the H2 Database.
`configure` also calls a private method which reads a sql file from resources/h2 folder which initialize all the tables. The sql file contains all the
sql to set up all the tables we use. It is in fifth normal form, and it has all the constraints to keep the database consistent (ACID). Then it
executes the sql files which populate the tables. Those sql files are also located in resources/h2 folder.