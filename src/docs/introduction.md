# Multi-Tenanted c3p0 connection pools

This was originally designed to connect to cockroachdb see [https://www.cockroachlabs.com/](https://www.cockroachlabs.com/) to allow multiple connections to multiple data sources.  However this can be used for any database connection pooling to multiple database.

This extends c3p0 through named connections to allow multiple pools to themselves be pooled.

An example `c3p0.properties` file is listed below:


