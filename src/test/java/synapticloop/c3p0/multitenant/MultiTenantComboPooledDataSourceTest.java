package synapticloop.c3p0.multitenant;

import static org.junit.Assert.*;

import java.sql.SQLException;

import javax.naming.NamingException;

import org.junit.Test;

public class MultiTenantComboPooledDataSourceTest extends BaseTest {

	@Test
	public void testRequestCountStrategy() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource();
		assertEquals(-1, multiTenantComboPooledDataSource.getRequestCountForTenant("some_unkown_tenant"));
		assertEquals(-1, multiTenantComboPooledDataSource.getRequestPoolCountForName("some_unkown_pool"));
		assertTrue(multiTenantComboPooledDataSource.getStrategy().equals(MultiTenantComboPooledDataSource.Strategy.ROUND_ROBIN));
	}

	@Test
	public void testGetReference() throws NamingException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource();
		assertNotNull(multiTenantComboPooledDataSource.getReference());
	}
}
