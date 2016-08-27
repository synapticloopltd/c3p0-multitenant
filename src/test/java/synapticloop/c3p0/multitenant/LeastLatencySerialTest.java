package synapticloop.c3p0.multitenant;

import java.sql.SQLException;

import org.junit.Test;

public class LeastLatencySerialTest extends BaseTest {

	@Test
	public void testSerialStrategy() throws SQLException {
		new MultiTenantComboPooledDataSource("/c3p0.multitenant.least-latency-serial.properties");

		// at this point - not sure how to test this accurately...
		// debugging output shows (which inndicates correct ordering:
		//		[INFO] Data source 'one' has an average latency of: 2.000000000000 from 50 tries
		//		[INFO] Initializing c3p0 pool... com.mchange.v2.c3p0.ComboPooledDataSource [ acquireIncrement -> 1, acquireRetryAttempts -> 1, acquireRetryDelay -> 1000, autoCommitOnClose -> false, automaticTestTable -> null, breakAfterAcquireFailure -> false, checkoutTimeout -> 0, connectionCustomizerClassName -> null, connectionTesterClassName -> com.mchange.v2.c3p0.impl.DefaultConnectionTester, contextClassLoaderSource -> caller, dataSourceName -> two, debugUnreturnedConnectionStackTraces -> true, description -> null, driverClass -> null, extensions -> {}, factoryClassLocation -> null, forceIgnoreUnresolvedTransactions -> false, forceSynchronousCheckins -> false, forceUseNamedDriverClass -> false, identityToken -> 2rvy2c9i1l4mv6r1fmin1b|4fbe5475, idleConnectionTestPeriod -> 1, initialPoolSize -> 2, jdbcUrl -> jdbc:postgresql://localhost:26257/multitenant, maxAdministrativeTaskTime -> 0, maxConnectionAge -> 0, maxIdleTime -> 0, maxIdleTimeExcessConnections -> 0, maxPoolSize -> 2, maxStatements -> 0, maxStatementsPerConnection -> 0, minPoolSize -> 2, numHelperThreads -> 3, preferredTestQuery -> select 1, privilegeSpawnedThreads -> false, properties -> {user=******, password=******}, propertyCycle -> 0, statementCacheNumDeferredCloseThreads -> 0, testConnectionOnCheckin -> false, testConnectionOnCheckout -> true, unreturnedConnectionTimeout -> 0, userOverrides -> {}, usesTraditionalReflectiveProxies -> false ]
		//		[FINE] Created new pool for auth, username (masked): 'mu******'.
		//		[FINE] awaitAvailable(): [unknown]
		//		[INFO] Data source 'two' has an average latency of: 0.000000000000 from 50 tries
		//		[INFO] Initializing c3p0 pool... com.mchange.v2.c3p0.ComboPooledDataSource [ acquireIncrement -> 1, acquireRetryAttempts -> 1, acquireRetryDelay -> 1000, autoCommitOnClose -> false, automaticTestTable -> null, breakAfterAcquireFailure -> false, checkoutTimeout -> 0, connectionCustomizerClassName -> null, connectionTesterClassName -> com.mchange.v2.c3p0.impl.DefaultConnectionTester, contextClassLoaderSource -> caller, dataSourceName -> three, debugUnreturnedConnectionStackTraces -> true, description -> null, driverClass -> null, extensions -> {}, factoryClassLocation -> null, forceIgnoreUnresolvedTransactions -> false, forceSynchronousCheckins -> false, forceUseNamedDriverClass -> false, identityToken -> 2rvy2c9i1l4mv6r1fmin1b|28cfdeb5, idleConnectionTestPeriod -> 1, initialPoolSize -> 2, jdbcUrl -> jdbc:postgresql://localhost:26257/multitenant, maxAdministrativeTaskTime -> 0, maxConnectionAge -> 0, maxIdleTime -> 0, maxIdleTimeExcessConnections -> 0, maxPoolSize -> 2, maxStatements -> 0, maxStatementsPerConnection -> 0, minPoolSize -> 2, numHelperThreads -> 3, preferredTestQuery -> select 1, privilegeSpawnedThreads -> false, properties -> {user=******, password=******}, propertyCycle -> 0, statementCacheNumDeferredCloseThreads -> 0, testConnectionOnCheckin -> false, testConnectionOnCheckout -> true, unreturnedConnectionTimeout -> 0, userOverrides -> {}, usesTraditionalReflectiveProxies -> false ]
		//		[FINE] Created new pool for auth, username (masked): 'mu******'.
		//		[FINE] awaitAvailable(): [unknown]
		//		[INFO] Data source 'three' has an average latency of: 0.000000000000 from 50 tries
		//		[INFO] Initializing c3p0 pool... com.mchange.v2.c3p0.ComboPooledDataSource [ acquireIncrement -> 1, acquireRetryAttempts -> 1, acquireRetryDelay -> 1000, autoCommitOnClose -> false, automaticTestTable -> null, breakAfterAcquireFailure -> false, checkoutTimeout -> 0, connectionCustomizerClassName -> null, connectionTesterClassName -> com.mchange.v2.c3p0.impl.DefaultConnectionTester, contextClassLoaderSource -> caller, dataSourceName -> four, debugUnreturnedConnectionStackTraces -> true, description -> null, driverClass -> null, extensions -> {}, factoryClassLocation -> null, forceIgnoreUnresolvedTransactions -> false, forceSynchronousCheckins -> false, forceUseNamedDriverClass -> false, identityToken -> 2rvy2c9i1l4mv6r1fmin1b|6b01565b, idleConnectionTestPeriod -> 1, initialPoolSize -> 2, jdbcUrl -> jdbc:postgresql://localhost:26257/multitenant, maxAdministrativeTaskTime -> 0, maxConnectionAge -> 0, maxIdleTime -> 0, maxIdleTimeExcessConnections -> 0, maxPoolSize -> 2, maxStatements -> 0, maxStatementsPerConnection -> 0, minPoolSize -> 2, numHelperThreads -> 3, preferredTestQuery -> select 1, privilegeSpawnedThreads -> false, properties -> {user=******, password=******}, propertyCycle -> 0, statementCacheNumDeferredCloseThreads -> 0, testConnectionOnCheckin -> false, testConnectionOnCheckout -> true, unreturnedConnectionTimeout -> 0, userOverrides -> {}, usesTraditionalReflectiveProxies -> false ]
		//		[FINE] Created new pool for auth, username (masked): 'mu******'.
		//		[FINE] awaitAvailable(): [unknown]
		//		[INFO] Data source 'four' has an average latency of: 0.000000000000 from 50 tries
		//		[INFO] Adding data source 'two'.
		//		[INFO] Adding data source 'three'.
		//		[INFO] Adding data source 'four'.
		//		[INFO] Adding data source 'one'.
	}

}
