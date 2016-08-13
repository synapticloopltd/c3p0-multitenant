__NOTE__ the above lines with the `named-configs`:

```
c3p0.named-configs.one.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant
c3p0.named-configs.two.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant
c3p0.named-configs.three.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant
c3p0.named-configs.four.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant
```

The named configs are

 - `one`
 - `two`
 - `three`
 - `four`

Once the named configurations are set up, you will then need a `c3p0.multitenant.properties` 
file to determine the strategy that will be used.  A complete example is listed 
below, with all of the options that are available.
