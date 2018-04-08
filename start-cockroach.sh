#!/bin/bash

cockroach start --store=cockroachdb/node1 --port=26257 --http-port=8080 --host=localhost --insecure --background
cockroach start --store=cockroachdb/node2 --port=26258 --http-port=8081 --host=localhost --insecure --join=localhost:26257 --background
cockroach start --store=cockroachdb/node3 --port=26259 --http-port=8082 --host=localhost --insecure --join=localhost:26257 --background
cockroach start --store=cockroachdb/node4 --port=26260 --http-port=8083 --host=localhost --insecure --join=localhost:26257 --background

cockroach sql --insecure --execute="create database if not exists multitenant" --execute="create user if not exists multitenant" --execute="grant all on database multitenant to multitenant"

