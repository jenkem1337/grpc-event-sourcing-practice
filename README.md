# Event Sourcing Project

## Purpose Of Project
The purpose of this project is to learn the event sourcing architecture and its implementation, and to reinforce this knowledge with a simple shopping cart example

## Architecture Of Project
  * Postgres is used for Event Store
  * Etcd is used for leader election to enable horizontal scalability for the snapshot service
  * gRPC is used for client-server communication
  ### High Level Architecture
  ![HighLevelArch](https://github.com/jenkem1337/grpc-event-sourcing-practice/blob/master/src/main/resources/Ads%C4%B1z-2025-03-01-1347.png)
  
  ### EventStore ERD
  ![ERD](https://github.com/jenkem1337/grpc-event-sourcing-practice/blob/master/src/main/resources/event-store-erd.png)

## gRPC Service Definitions
  ### ShoppingCartService
  | Method Name | Explanation |
  | ------------|-------------|
  | createCart  | Creating new cart with user id|
  | addAnItem   | Add new cart item if not exist in cart. Otherwise only increased cart item quantity|
  | removeAnItem| Remove a cart item from cart|
  | removeItem  | Remove single cart item|
  | complete    | Complete shopping |
  ### ShoppingCartSnapshotService
  | Method Name | Explanation |
  | ------------|-------------|
  | getLatestAggregateStateForSnapshot | Getting snapshot needed aggregate state|
  | saveSnaphot| Save needed snapshots|

## Installation
* Clone Repository
```bash
  git clone https://github.com/jenkem1337/grpc-event-sourcing-practice.git
```
* Start
```bash
docker compose up --build
```
