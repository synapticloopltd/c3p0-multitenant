
## `LOAD_BALANCED`

This load balances the connections between all of the pools of connection pools.  This strategy looks at the busy connections on all of the pools and chooses the one based on the minimum number of busy connections.

> In effect this will use a connection from the least busy pool

To instantiate a `LOAD_BALANCED` strategy pool and get a connection:

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

MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.LOAD_BALANCED);
Connection connection = multiTenantComboPooledDataSource.getConnection();
```


### Property file usage

Should you wish to use property files, the pool of connection pools can be instantiated thusly:

```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.load_balanced.properties");
```

__Note that the property file is loaded from the classpath and can be named anything__

The property file required for this strategy is listed below:



