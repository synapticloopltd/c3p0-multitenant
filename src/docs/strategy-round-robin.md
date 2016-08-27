
## `ROUND_ROBIN`

This is the default strategy for the multi tenanted pool and simply round robins the connection requests through all of the pools of connection pools

To instantiate a `ROUND_ROBIN` strategy pool and get a connection:

```
import java.sql.Connection;
import java.util.List;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource;
import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;


List<String> TENANTS = new ArrayList<String>();

TENANTS.add("one");
TENANTS.add("two");
TENANTS.add("three");
TENANTS.add("four");

MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.ROUND_ROBIN);
Connection connection = multiTenantComboPooledDataSource.getConnection();
```

### Property file usage

Should you wish to use property files, the pool of connection pools can be instantiated thusly:

```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.round_robin.properties");
```

__Note that the property file is loaded from the classpath and can be named anything__

The property file required for this strategy is listed below:


