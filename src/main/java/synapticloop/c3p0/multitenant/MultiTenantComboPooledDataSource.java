package synapticloop.c3p0.multitenant;

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

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import com.mchange.v2.naming.JavaBeanReferenceMaker;

/**
 * This is a multi tenant connection pool for connections to a variety of sources
 * 
 * @author synapticloop
 *
 */
public class MultiTenantComboPooledDataSource implements Serializable, Referenceable {
	private static final long serialVersionUID = -8817106257353513000L;

	private static final JavaBeanReferenceMaker REFERENCE_MAKER = new com.mchange.v2.naming.JavaBeanReferenceMaker();
	private static final MLogger LOGGER;

	private static final String C3P0_MULTITENANT_PROPERTIES = "/c3p0.multitenant.properties";

	private static final String PROPERTY_STRATEGY = "strategy";
	private static final String PROPERTY_TENANTS = "tenants";

	static {
		LOGGER = MLog.getLogger(MultiTenantComboPooledDataSource.class);
	}

	public enum Strategy {
		ROUND_ROBIN, // just go through the connection pools, disregarding load
		LOAD_BALANCED, // get the lowest number of connections and use this pool
		SERIAL // use up all of the first connections, going to the next one when full
	}

	private Strategy strategy = Strategy.ROUND_ROBIN;
	private List<String> tenants;
	private List<ComboPooledDataSource> comboPooledDataSources = new ArrayList<ComboPooledDataSource>();

	private int comboPooledDataSourcesSize;
	private int comboPooledDataSourcesCurrent = 0;

	private Map<String, MutableInt> connectionRequestHitCountMap = new HashMap<String, MutableInt>();

	class MutableInt {
		private int value = 0;

		public void increment() { 
			++value;
		}

		public int getValue() {
			return value;
		}
	}

	public MultiTenantComboPooledDataSource() {
		// try and load the c3p0.multitenant.properties
		Properties properties = new Properties();
		try {
			properties.load(MultiTenantComboPooledDataSource.class.getResourceAsStream(C3P0_MULTITENANT_PROPERTIES));
			// at this point we are looking for tenants and the strategy
			Strategy.valueOf(properties.getProperty(PROPERTY_STRATEGY, Strategy.ROUND_ROBIN.toString()));

			String propertyTenant = properties.getProperty(PROPERTY_TENANTS, null);
			if(null == propertyTenant) {
				if(LOGGER.isLoggable(MLevel.SEVERE)) {
					LOGGER.log(MLevel.SEVERE, String.format("Could not determine tenants for connections, required property '%s' missing.", PROPERTY_TENANTS));
				}
				return;
			}

			String[] propertyTenants = propertyTenant.split(",");
			if(0 == propertyTenants.length) {
				if(LOGGER.isLoggable(MLevel.SEVERE)) {
					LOGGER.log(MLevel.SEVERE, String.format("Empty list of tenants."));
				}
				return;
			}

			this.tenants = Arrays.asList(propertyTenants);
		} catch (IOException ex) {
			if(LOGGER.isLoggable(MLevel.SEVERE)) {
				LOGGER.log(MLevel.SEVERE, "Could not load c3p0.multitenant.properties file", ex);
			}
		}

		// finally we are ready to initialise the tenants
		initialiseMultiTenantPools();
	}

	public MultiTenantComboPooledDataSource(List<String> tenants, Strategy strategy) {
		this.tenants = tenants;
		this.strategy = strategy;

		initialiseMultiTenantPools();
	}

	private void initialiseMultiTenantPools() {
		boolean isDebugEnabled = LOGGER.isLoggable(MLevel.DEBUG);

		for (String tenant : tenants) {
			if(isDebugEnabled) {
				LOGGER.log(MLevel.DEBUG, String.format("Creating connection pool for tenant '%s'", tenant));
			}

			ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource(tenant);

			if(isDebugEnabled) {
				LOGGER.log(MLevel.DEBUG, String.format("Created connection pool for tenant '%s'", tenant));
			}

			// now set up all of the data structures
			comboPooledDataSources.add(comboPooledDataSource);
			connectionRequestHitCountMap.put(tenant, new MutableInt());
		}

		comboPooledDataSourcesSize = comboPooledDataSources.size();
	}

	public Connection getConnection() throws SQLException {
		if(LOGGER.isLoggable(MLevel.DEBUG)) {
			LOGGER.log(MLevel.DEBUG, String.format("'%s' connection requested", this.strategy.toString()));
		}

		switch(this.strategy) {
		case ROUND_ROBIN:
			return(getRoundRobinConnection());
		case LOAD_BALANCED:
			return(getLoadBalancedConnection());
		case SERIAL:
			return(getSerialConnection());
		default:
			throw new SQLException(String.format("Could not determine the strategy for connections, was looking for '%s'", strategy.toString()));
		}
	}

	private Connection getRoundRobinConnection() throws SQLException {
		ComboPooledDataSource comboPooledDataSource = comboPooledDataSources.get(comboPooledDataSourcesCurrent);
		Connection connection = comboPooledDataSource.getConnection();
		if(comboPooledDataSourcesCurrent == comboPooledDataSourcesSize - 1) {
			comboPooledDataSourcesCurrent = 0;
		} else {
			comboPooledDataSourcesCurrent++;
		}

		incrementRequestHitCountMap(comboPooledDataSource);

		return(connection);
	}

	private synchronized Connection getSerialConnection() throws SQLException {
		for (ComboPooledDataSource comboPooledDataSource : comboPooledDataSources) {
			if(comboPooledDataSource.getNumBusyConnections() < comboPooledDataSource.getMaxPoolSize()) {
				incrementRequestHitCountMap(comboPooledDataSource);
				return(comboPooledDataSource.getConnection());
			}
		}

		// at this point we are out of connections - just return the first one
		ComboPooledDataSource comboPooledDataSource = comboPooledDataSources.get(0);
		incrementRequestHitCountMap(comboPooledDataSource);

		return(comboPooledDataSource.getConnection());
	}

	private synchronized Connection getLoadBalancedConnection() throws SQLException {
		int maxBusyConnections = 0;
		int poolIndex = 0;
		int readyPoolIndex = 0;
		for (ComboPooledDataSource comboPooledDataSource : comboPooledDataSources) {
			int numBusyConnections = comboPooledDataSource.getNumBusyConnections();
			if(numBusyConnections > maxBusyConnections) {
				maxBusyConnections = numBusyConnections;
			} else {
				readyPoolIndex = poolIndex;
			}
			poolIndex++;
		}

		return(comboPooledDataSources.get(readyPoolIndex).getConnection());
	}

	private void incrementRequestHitCountMap(ComboPooledDataSource comboPooledDataSource) {
		connectionRequestHitCountMap.get(comboPooledDataSource.getDataSourceName()).increment();
	}

	/**
	 * Return the request count for a specific tenant
	 * 
	 * @param tenant the name of the tenant
	 * 
	 * @return the number of connection requests for this tenant
	 */
	public int getRequestCountForTenant(String tenant) {
		if(connectionRequestHitCountMap.containsKey(tenant)) {
			return(connectionRequestHitCountMap.get(tenant).getValue());
		}

		return(-1);
	}

	public Reference getReference() throws NamingException {
		return REFERENCE_MAKER.createReference(this);
	}

}
