
## `NAMED`

This strategy allows you to pool the pool of connection pools by name.  As an example, you may have `read` only databases that your web application mainly uses, whilst you may have `write` databases that is used by the back end system.

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

String[] NAMES = { "read", "read", "read", "write" };

MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, NAMES);

# to return a connection from the 'read' pool
Connection readConnection = multiTenantComboPooledDataSource.getConnection("read");

# to return a connection from the 'write' pool
Connection writeConnection = multiTenantComboPooledDataSource.getConnection("write");
```

If you mistakenly call `getConnection()` without passing in the named pool, you will get a `ROUND_ROBIN` connection.

### Property file usage

Should you wish to use property files, the pool of connection pools can be instantiated thusly:

```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.named.properties");
```

__Note that the property file is loaded from the classpath and can be named anything__

The property file required for this strategy is listed below:


