
## WEIGHTED

This strategy allows you to weight certain connections for more use, than others, you are required to add in the weightings, or they will be equally weighted - in effect giving all of the pools of connection pools equal chance to be selected for usage.

> In effect this will randomly choose a connection from the pool weighted with the passed in weightings

To instantiate a `WEIGHTED` strategy pool and get a connection:

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

List<Integer> WEIGHTINGS = new ArrayList<Integer>();
WEIGHTINGS.add(60);
WEIGHTINGS.add(25);
WEIGHTINGS.add(10);
WEIGHTINGS.add(5);

MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, WEIGHTINGS);
Connection connection = multiTenantComboPooledDataSource.getConnection();

```

In the above example, on average, **60%** will come from the first connection pool, **25%** from the second, **10%** from the third and **5%** from the fourth.  

If you instantiate a `WEIGHTED` strategy thusly:

```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.WEIGHTED);
```

Then all of the connection pools will be given a default weighting of 1.


### Property file usage

Should you wish to use property files, the pool of connection pools can be instantiated thusly:

```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.weighted.properties");
```

__Note that the property file is loaded from the classpath and can be named anything__

The property file required for this strategy is listed below:


