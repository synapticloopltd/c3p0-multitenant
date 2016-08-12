package synapticloop.c3p0.multitenant;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;

public class WeightedTest extends BaseTest {

	@Test
	public void testDefaultWeightings() {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.WEIGHTED);
		assertEquals(4, multiTenantComboPooledDataSource.getTotalWeightings());
	}

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
		// weightings - doesn't test that the weightings are correct, just that
		// they are in the correct order
		assertTrue(one > two);
		assertTrue(two > three);
		assertTrue(three > four);
		assertTrue(four > 0);
	}
	
	@Test
	public void testZeroWeighting() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, ZERO_WEIGHTINGS);
		assertEquals(0, multiTenantComboPooledDataSource.getTotalWeightings());
		for(int i = 0; i < 10; i++) {
			assertNull(multiTenantComboPooledDataSource.getConnection());
		}
	}
}
