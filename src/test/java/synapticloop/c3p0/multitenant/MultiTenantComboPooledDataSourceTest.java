package synapticloop.c3p0.multitenant;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.Test;

public class MultiTenantComboPooledDataSourceTest extends BaseTest {

	@Test
	public void testRequestCountStrategy() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource();
		assertEquals(-1, multiTenantComboPooledDataSource.getRequestCountForTenant("some_unkown_tenant"));
		assertEquals(-1, multiTenantComboPooledDataSource.getRequestPoolCountForName("some_unkown_pool"));
		assertTrue(multiTenantComboPooledDataSource.getStrategy().equals(MultiTenantComboPooledDataSource.Strategy.ROUND_ROBIN));
	}

//	@Test
//	public void testTooFewStrategy() throws SQLException {
//		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, TOO_FEW_NAMES);
//		Connection connection = multiTenantComboPooledDataSource.getConnection(NAME_READ);
//		connection.close();
//		connection = multiTenantComboPooledDataSource.getConnection(NAME_WRITE);
//		connection.close();
//		connection = multiTenantComboPooledDataSource.getConnection(MultiTenantComboPooledDataSource.KEY_DEFAULT_WEIGHTED_NAME_MAP);
//		connection.close();
//
//		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(NAME_READ));
//		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(NAME_WRITE));
//		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(MultiTenantComboPooledDataSource.KEY_DEFAULT_WEIGHTED_NAME_MAP));
//	}
//
//	@Test
//	public void testTooManyStrategy() throws SQLException {
//		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, TOO_MANY_NAMES);
//		Connection connection = multiTenantComboPooledDataSource.getConnection(NAME_READ);
//		connection.close();
//		connection = multiTenantComboPooledDataSource.getConnection(NAME_WRITE);
//		connection.close();
//
//		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(NAME_READ));
//		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(NAME_WRITE));
//	}

}
