
# Additional Testing Notes

In order for the test to run, you __MUST__ have a cockroach DB setup:

```
cockroach start --store=cockroachdb/node1 --port=26257 --http-port=8080 --background
cockroach start --store=cockroachdb/node2 --port=26258 --http-port=8081 --join=localhost:26257 --background
cockroach start --store=cockroachdb/node3 --port=26259 --http-port=8082 --join=localhost:26257 --background
cockroach start --store=cockroachdb/node4 --port=26260 --http-port=8083 --join=localhost:26257 --background
```

This will start 4 nodes all clustered together.

Run:

```
cockroach sql
```

Then run

```
create database multitenant;
grant all on multitenant.* to multitenant;
```

This will allow the user `multitenant` to connect to the database.

