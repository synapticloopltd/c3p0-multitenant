package synapticloop.c3p0.multitenant;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

public class WeightedTest extends BaseTest {

	@Test
	public void testWeightedStrategy() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, WEIGHTINGS);

		assertEquals(100, multiTenantComboPooledDataSource.getTotalWeightings());
		assertTrue(multiTenantComboPooledDataSource.getStrategy().equals(MultiTenantComboPooledDataSource.Strategy.WEIGHTED));
	}

	@Test
	public void testTooFewStrategy() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, TOO_FEW_WEIGHTINGS);

		assertEquals(96, multiTenantComboPooledDataSource.getTotalWeightings());
	}

	@Test
	public void testTooManyStrategy() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, TOO_MANY_WEIGHTINGS);

		assertEquals(100, multiTenantComboPooledDataSource.getTotalWeightings());
	}

	@Test
	public void testConnectionWeighting() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, WEIGHTINGS);
		for(int i = 0; i < 1000; i++) {
			Connection connection = multiTenantComboPooledDataSource.getConnection();
			connection.close();
		}

		for (String tenant : TENANTS) {
			System.out.println(multiTenantComboPooledDataSource.getRequestCountForTenant(tenant));
		}

	}
}
