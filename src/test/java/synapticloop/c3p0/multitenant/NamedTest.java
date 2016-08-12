package synapticloop.c3p0.multitenant;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

public class NamedTest extends BaseTest {

	@Test
	public void testNamedStrategy() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, NAMES);

		assertTrue(multiTenantComboPooledDataSource.getStrategy().equals(MultiTenantComboPooledDataSource.Strategy.NAMED));
	}

	@Test
	public void testTooFewStrategy() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, TOO_FEW_NAMES);
		Connection connection = multiTenantComboPooledDataSource.getConnection(NAME_READ);
		connection.close();
		connection = multiTenantComboPooledDataSource.getConnection(NAME_WRITE);
		connection.close();
		connection = multiTenantComboPooledDataSource.getConnection(MultiTenantComboPooledDataSource.KEY_DEFAULT_WEIGHTED_NAME_MAP);
		connection.close();

		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(NAME_READ));
		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(NAME_WRITE));
		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(MultiTenantComboPooledDataSource.KEY_DEFAULT_WEIGHTED_NAME_MAP));
	}

	@Test
	public void testTooManyStrategy() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, TOO_MANY_NAMES);
		Connection connection = multiTenantComboPooledDataSource.getConnection(NAME_READ);
		connection.close();
		connection = multiTenantComboPooledDataSource.getConnection(NAME_WRITE);
		connection.close();

		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(NAME_READ));
		assertEquals(1, multiTenantComboPooledDataSource.getRequestPoolCountForName(NAME_WRITE));
	}

	@Test(expected = SQLException.class)
	public void testGetIncorrectNamedPool() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, NAMES);
		multiTenantComboPooledDataSource.getConnection("this is an incorrect pool name");
	}

	@Test
	public void testGetNonNamedPool() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, NAMES);

		// this will revert to round robin and get the first one
		Connection connection = multiTenantComboPooledDataSource.getConnection();
		connection.close();

		assertEquals(1, multiTenantComboPooledDataSource.getRequestCountForTenant("one"));
	}
}
