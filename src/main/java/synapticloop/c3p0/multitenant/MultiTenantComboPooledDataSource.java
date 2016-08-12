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
 * This is a multi tenant connection pool for connections to a variety of 
 * sources, this was developed primarily for cockroachDB (see
 * https://www.cockroachlabs.com/ for more details) although any multi tenanted
 * polls that need to connect to multiple databases that all hold the same 
 * data will work.
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
	private static final String PROPERTY_WEIGHTINGS = "weightings";
	private static final String PROPERTY_NAMES = "names";

	static {
		LOGGER = MLog.getLogger(MultiTenantComboPooledDataSource.class);
	}

	public enum Strategy {
		ROUND_ROBIN, // just go through the connection pools, disregarding load
		LOAD_BALANCED, // get the lowest number of connections and use this pool
		SERIAL, // use up all of the first connections, going to the next one when full
		WEIGHTED, // weight the connections, from the passed in values
		NAMED // get a connection from one of the named pools, there may be more than one pool per name, they will be weighted between them
	}

	private Strategy strategy = Strategy.ROUND_ROBIN;
	private List<String> tenants;
	private List<ComboPooledDataSource> comboPooledDataSources = new ArrayList<ComboPooledDataSource>();
	private List<Integer> weightings;

	private int comboPooledDataSourcesSize;
	private int comboPooledDataSourcesCurrent = 0;

	private Map<String, MutableInt> connectionRequestHitCountMap = new HashMap<String, MutableInt>();
	private WeightedMap<ComboPooledDataSource> weightedMap = new WeightedMap<ComboPooledDataSource>();
	private Map<String, ComboPooledDataSource> comboPooledDataSourceMap = new HashMap<String, ComboPooledDataSource>();
	private Map<String, WeightedMap<ComboPooledDataSource>> namedWeightMap = new HashMap<String, WeightedMap<ComboPooledDataSource>>();

	private String[] names;

	class MutableInt {
		private int value = 0;

		public void increment() { 
			++value;
		}

		public int getValue() {
			return value;
		}
	}

	public MultiTenantComboPooledDataSource(String propertyFileLocation) {
		// try and load the c3p0.multitenant.properties
		Properties properties = new Properties();
		try {
			properties.load(MultiTenantComboPooledDataSource.class.getResourceAsStream(propertyFileLocation));
			// at this point we are looking for tenants and the strategy
			this.strategy = Strategy.valueOf(properties.getProperty(PROPERTY_STRATEGY, Strategy.ROUND_ROBIN.toString()));

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

			// now get the weightings
			switch (this.strategy) {
			case WEIGHTED:
				String propertyWeightings = properties.getProperty(PROPERTY_WEIGHTINGS, null);
				if(null == propertyWeightings) {
					if(LOGGER.isLoggable(MLevel.SEVERE)) {
						LOGGER.log(MLevel.SEVERE, String.format("A strategy of '%s' was requested, yet no property '%s' was supplied, all weightings will be set to 1", Strategy.WEIGHTED.toString(), PROPERTY_WEIGHTINGS));
					}
					propertyWeightings = "";
				}

				String[] splits = propertyWeightings.split(",");
				weightings = new ArrayList<Integer>();
				for (String split : splits) {
					try {
						weightings.add(new Integer(split.trim()));
					} catch(NumberFormatException ex) {
						if(LOGGER.isLoggable(MLevel.SEVERE)) {
							LOGGER.log(MLevel.SEVERE, String.format("Could not convert the weighting of '%s' to an integer, setting the weighting to 1", split));
						}
						weightings.add(1);
					}
				}
				break;
			case NAMED:
				String propertyNames = properties.getProperty(PROPERTY_NAMES, null);
				if(null == propertyNames) {
					if(LOGGER.isLoggable(MLevel.SEVERE)) {
						LOGGER.log(MLevel.SEVERE, String.format("A strategy of '%s' was requested, yet no property '%s' was supplied, all names will be set to the same empty string", Strategy.NAMED.toString(), PROPERTY_NAMES));
					}
					propertyNames = "";
				}
				this.names = propertyNames.split(propertyNames);
				break;
			case SERIAL:
				// fall-through ignore
			case LOAD_BALANCED:
				// fall-through ignore
			case ROUND_ROBIN:
				// fall-through ignore
			default:
				break;
			}

		} catch (IOException ex) {
			if(LOGGER.isLoggable(MLevel.SEVERE)) {
				LOGGER.log(MLevel.SEVERE, String.format("Could not find the '%s' file", propertyFileLocation), ex);
			}
		}

		// finally we are ready to initialise the tenants
		initialiseMultiTenantPools();

	}

	/**
	 * Initialise the multi tenant combo pool using the default file to be found
	 * on the classpath - default "/c3p0.multitenant.properties"
	 */
	public MultiTenantComboPooledDataSource() {
		this(C3P0_MULTITENANT_PROPERTIES);
	}

	/**
	 * Create a multi tenant combo pooled data source, with a list of named 
	 * configurations for c3p0 
	 * 
	 * @param tenants The list of named configurations to use
	 * @param strategy The strategy to use
	 */
	public MultiTenantComboPooledDataSource(List<String> tenants, Strategy strategy) {
		this.tenants = tenants;
		this.strategy = strategy;

		initialiseMultiTenantPools();
	}

	/**
	 * Create a multi tenant combo pooled data source with a 'weighted' strategy 
	 * for each of the list of named configurations for c3p0.  The list size of 
	 * the weightings should be the same as the size of the named  tenants, if 
	 * not, the weighting will be set to 1.
	 * 
	 * The weightings do not have to add up to any particular number, this will 
	 * be used when randomly assigning a connection, based on the weighting
	 * 
	 * @param tenants The list of named configurations to use
	 * @param weightings The list of weightings for each of the named 
	 *     configurations to use
	 */
	public MultiTenantComboPooledDataSource(List<String> tenants, List<Integer> weightings) {
		this.tenants = tenants;
		this.weightings = weightings;

		// This must always be the weighted strategy
		this.strategy = Strategy.WEIGHTED;

		initialiseMultiTenantPools();
	}

	public MultiTenantComboPooledDataSource(List<String> tenants, String[] names) {
		this.tenants = tenants;
		this.names = names;

		// This must always be the weighted strategy
		this.strategy = Strategy.WEIGHTED;

		initialiseMultiTenantPools();
	}

	private void initialiseMultiTenantPools() {
		boolean isDebugEnabled = LOGGER.isLoggable(MLevel.DEBUG);

		for (String tenant : tenants) {
			if(isDebugEnabled) {
				LOGGER.log(MLevel.DEBUG, String.format("Creating connection pool for tenant '%s'", tenant));
			}

			ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource(tenant);
			comboPooledDataSourceMap.put(tenant, comboPooledDataSource);

			if(isDebugEnabled) {
				LOGGER.log(MLevel.DEBUG, String.format("Created connection pool for tenant '%s'", tenant));
			}

			// now set up all of the data structures
			comboPooledDataSources.add(comboPooledDataSource);
			connectionRequestHitCountMap.put(tenant, new MutableInt());
		}

		switch (this.strategy) {
		case WEIGHTED:
			for(int i = 0; i < tenants.size(); i++) {
				try {
					weightedMap.add(weightings.get(i), comboPooledDataSourceMap.get(tenants.get(i)));
				} catch(IndexOutOfBoundsException ex) {
					// we have too few weightings - log it and carry on
					if(LOGGER.isLoggable(MLevel.SEVERE)) {
						LOGGER.severe(String.format("Could not determine the weighting for pool '%s', setting it to 1", tenants.get(i)));
					}
					weightedMap.add(1, comboPooledDataSourceMap.get(tenants.get(i)));
				}
			}

			if(weightings.size() > tenants.size()) {
				if(LOGGER.isLoggable(MLevel.SEVERE)) {
					LOGGER.severe(String.format("I received '%d' weightings for the '%d' tenants, ignoring extra weightings...", weightings.size(), tenants.size()));
				}
			}
			break;
		case NAMED:
			break;
		default:
			// no extra processing for other strategies
			break;
		}

		comboPooledDataSourcesSize = comboPooledDataSources.size();
	}

	/**
	 * Get a connection from one of the pools this will depend on the pool that 
	 * was chosen when instantiation occurred
	 * 
	 * @return the connection to one of the tenants
	 * 
	 * @throws SQLException if there was an error getting a connection
	 */
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
		case WEIGHTED:
			return(getWeightedConnection());
		default:
			throw new SQLException(String.format("Could not determine the strategy for connections, was looking for '%s'", strategy.toString()));
		}
	}

	/**
	 * Get a round robin connection to the underlying database pools.  The round
	 * 
	 * @return the round robin connection
	 * 
	 * @throws SQLException If there was an error connecting to the database
	 */
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

	/**
	 * Get a serial connection
	 * @return
	 * @throws SQLException
	 */
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
		String dataSourceName = null;

		try {
			for (ComboPooledDataSource comboPooledDataSource : comboPooledDataSources) {
				// TODO - we don'e really want to fail on this one... - need to split out try/catch
				int numBusyConnections = comboPooledDataSource.getNumBusyConnections();
				if(numBusyConnections > maxBusyConnections) {
					maxBusyConnections = numBusyConnections;
				} else {
					readyPoolIndex = poolIndex;
				}
				poolIndex++;
			}

			ComboPooledDataSource comboPooledDataSource = comboPooledDataSources.get(readyPoolIndex);
			dataSourceName = comboPooledDataSource.getDataSourceName();
			return(comboPooledDataSource.getConnection());
		} catch (SQLException ex) {
			throw new SQLException(String.format("Could not get a connection to data source name '%s'", dataSourceName), ex);
		}
	}

	/**
	 * Get the weighted connection
	 * 
	 * @return the connection for the weighted parameter
	 * 
	 * @throws SQLException if there was wn error getting the connection
	 */
	private Connection getWeightedConnection() throws SQLException {
		ComboPooledDataSource comboPooledDataSource = weightedMap.next();
		incrementRequestHitCountMap(comboPooledDataSource);
		return(comboPooledDataSource.getConnection());
	}

	/**
	 * Increment the number of hits for the request to the combo pool
	 * 
	 * @param comboPooledDataSource the combo pool to increment the hit for
	 */
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

	/**
	 * Return the strategy that is in use for the combo pool
	 * 
	 * @return the strategy for the combo pool
	 */
	public Strategy getStrategy() { return(this.strategy); }
	public int getTotalWeightings() { return(weightedMap.getTotalWeightings()); }
}
