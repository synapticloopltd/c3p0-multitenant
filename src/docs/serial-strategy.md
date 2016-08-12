
## SERIAL

This strategy chooses connections based on the number of busy connections, it will try and fill up all of the connection pool for the first available pool of conneciton pools.  Once this pool has been exhausted, it will move on to the next one (and so on for all of the available pools of connection pools).  Once the connections are freed from the earlier pools, it will re-use them.

> In effect this will use up as many connections from the first available pool of conneciton pools

To instantiate a `SERIAL` strategy pool and get a connection:

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


