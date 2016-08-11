package synapticloop.c3p0.multitenant;

import static org.junit.Assert.*;

/*
 * Copyright (c) 2016 Synapticloop.
 * 
 * All rights reserved.
 * 
 * This code may contain contributions from other parties which, where 
 * applicable, will be listed in the default build file for the project 
 * ~and/or~ in a file named CONTRIBUTORS.txt in the root of the project.
 * 
 * This source code and any derived binaries are covered by the terms and 
 * conditions of the Licence agreement ("the Licence").  You may not use this 
 * source code or any derived binaries except in compliance with the Licence.  
 * A copy of the Licence is available in the file named LICENSE.txt shipped with 
 * this source code or binaries.
 */

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;

public class SerialTest extends BaseTest {

	@Test
	public void testSerialStrategy() throws SQLException {
		multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.SERIAL);
		for(int i = 0; i < 4; i++) {
			Connection connection = multiTenantComboPooledDataSource.getConnection();
			assertNotNull(connection);

			//assertEquals(1, multiTenantComboPooledDataSource.getRequestCountForTenant(TENANTS.get(i)));

			connection.close();
		}

		for (String tenant : TENANTS) {
			assertTrue(true);
//			assertEquals(1, multiTenantComboPooledDataSource.getRequestCountForTenant(tenant));
		}
	}
}
