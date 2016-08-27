package synapticloop.c3p0.multitenant;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.NamingException;

import org.junit.Test;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;
import synapticloop.c3p0.multitenant.util.SlowQueryThread;

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
	public void testLoadBalanacedPropertiesTest() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.load_balanced.properties");
		ExecutorService executor = Executors.newCachedThreadPool();
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.LOAD_BALANCED);
		for(int i = 0; i < 1000; i++) {
			Connection connection = multiTenantComboPooledDataSource.getConnection();
			assertNotNull(connection);

			executor.submit(new SlowQueryThread(connection, 10));
		}

		assertTrue(multiTenantComboPooledDataSource.getRequestCountForTenant(TENANTS.get(0)) < 300);
		assertTrue(multiTenantComboPooledDataSource.getRequestCountForTenant(TENANTS.get(1)) < 300);
		assertTrue(multiTenantComboPooledDataSource.getRequestCountForTenant(TENANTS.get(2)) < 300);
		assertTrue(multiTenantComboPooledDataSource.getRequestCountForTenant(TENANTS.get(3)) < 300);
	}

	@Test
	public void testSerialPropertiesTest() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.serial.properties");
		ExecutorService executor = Executors.newCachedThreadPool();
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.SERIAL);
		for(int i = 0; i < 9; i++) {
			Connection connection = multiTenantComboPooledDataSource.getConnection();
			assertNotNull(connection);

			executor.submit(new SlowQueryThread(connection));
		}

		assertEquals(3, multiTenantComboPooledDataSource.getRequestCountForTenant(TENANTS.get(0)));
		assertEquals(2, multiTenantComboPooledDataSource.getRequestCountForTenant(TENANTS.get(1)));
		assertEquals(2, multiTenantComboPooledDataSource.getRequestCountForTenant(TENANTS.get(2)));
		assertEquals(2, multiTenantComboPooledDataSource.getRequestCountForTenant(TENANTS.get(3)));
	}

	@Test
	public void testNamedPropertiesTest() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.named.properties");
		Connection connection = multiTenantComboPooledDataSource.getConnection(NAME_READ);
		connection.close();
		connection = multiTenantComboPooledDataSource.getConnection(NAME_WRITE);
		connection.close();

		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(NAME_READ));
		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(NAME_WRITE));
	}

	@Test
	public void testRoundRobinPropertiesTest() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.round_robin.properties");
		for(int i = 0; i < 4; i++) {
			Connection connection = multiTenantComboPooledDataSource.getConnection();
			assertNotNull(connection);

			assertEquals(multiTenantComboPooledDataSource.getRequestCountForTenant(TENANTS.get(i)), 1);

			connection.close();
		}

		for (String tenant : TENANTS) {
			assertEquals(multiTenantComboPooledDataSource.getRequestCountForTenant(tenant), 1);
		}
	}

	@Test
	public void testWeightedPropertiesTest() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.weighted.properties");
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
	public void testMissingTenant() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.round_robin-bad-tenants.properties");

	}
}
