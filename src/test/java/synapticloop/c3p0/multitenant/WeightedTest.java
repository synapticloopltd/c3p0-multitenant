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
		for(int i = 0; i < 100; i++) {
			Connection connection = multiTenantComboPooledDataSource.getConnection();
			connection.close();
		}

		for (String tenant : TENANTS) {
			System.out.println(multiTenantComboPooledDataSource.getRequestCountForTenant(tenant));
		}

		int one = multiTenantComboPooledDataSource.getRequestCountForTenant("one");
		int two = multiTenantComboPooledDataSource.getRequestCountForTenant("two");
		int three = multiTenantComboPooledDataSource.getRequestCountForTenant("three");
		int four = multiTenantComboPooledDataSource.getRequestCountForTenant("four");

		// allowing variance - there will be more variance with the smaller 
		// weightings
		assertTrue(one > 50 && one < 70);
		assertTrue(two > 15 && two < 35);
		assertTrue(three > 5 && three < 15);
		assertTrue(four > 2 && four < 8);

	}
}
