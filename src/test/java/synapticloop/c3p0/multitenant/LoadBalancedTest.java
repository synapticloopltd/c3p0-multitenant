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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;
import synapticloop.c3p0.multitenant.util.SlowQueryThread;

public class LoadBalancedTest extends BaseTest {

	@Test
	public void testLoadBalancedStrategy() throws SQLException {
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

}
