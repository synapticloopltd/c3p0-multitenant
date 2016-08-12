package synapticloop.c3p0.multitenant;

import static org.junit.Assert.*;

import java.sql.SQLException;

import javax.naming.NamingException;

import org.junit.Test;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;

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

	@Test
	public void testWrongStrategyForNamedPool() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, WEIGHTINGS);

		assertNull(multiTenantComboPooledDataSource.getConnection("something-which-doesn't-exist"));

		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.ROUND_ROBIN);
		assertNull(multiTenantComboPooledDataSource.getConnection("something-which-doesn't-exist"));

		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.LOAD_BALANCED);
		assertNull(multiTenantComboPooledDataSource.getConnection("something-which-doesn't-exist"));

		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.SERIAL);
		assertNull(multiTenantComboPooledDataSource.getConnection("something-which-doesn't-exist"));
	}
	
	@Test
	public void testLoadBalanacedPropertiesTest() {
		
	}
}
