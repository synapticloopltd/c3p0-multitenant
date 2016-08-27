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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
 * pools that need to connect to multiple databases that all hold the same 
 * data will work.
 * 
 * @author synapticloop
 *
 */
public class MultiTenantComboPooledDataSource implements Serializable, Referenceable {
	private static final long serialVersionUID = -8817106257353513000L;
	private static final MLogger LOGGER;
	static {
		LOGGER = MLog.getLogger(MultiTenantComboPooledDataSource.class);
	}

	private static final int NUM_TRIES_LATENCY_DEFAULT = 50;
	private static final Strategy DEFAULT_STRATEGY = Strategy.ROUND_ROBIN;
	private static final JavaBeanReferenceMaker REFERENCE_MAKER = new com.mchange.v2.naming.JavaBeanReferenceMaker();

	private static final String C3P0_MULTITENANT_PROPERTIES = "/c3p0.multitenant.properties";

	private static final String PROPERTY_STRATEGY = "strategy";
	private static final String PROPERTY_TENANTS = "tenants";
	private static final String PROPERTY_WEIGHTINGS = "weightings";
	private static final String PROPERTY_NAMES = "names";
	private static final String PROPERTY_NUM_TRIES_LATENCY= "num_tries_latency";

	public static final String KEY_DEFAULT_WEIGHTED_NAME_MAP = "";

	public enum Strategy {
		ROUND_ROBIN, // just go through the connection pools, disregarding load
		LOAD_BALANCED, // get the lowest number of connections and use this pool
		SERIAL, // use up all of the first connections, going to the next one when full
		LEAST_LATENCY_SERIAL, // get the least latency connection in a serial fashion
		WEIGHTED, // weight the connections, from the passed in values
		NAMED, // get a connection from one of the named pools, there may be more than one pool per name, they will be weighted between them
	}

	private Strategy strategy = DEFAULT_STRATEGY;
	private List<String> tenants;
	private List<Integer> weightings = new ArrayList<Integer>();
	private String[] names;
	private int numTriesLatency = NUM_TRIES_LATENCY_DEFAULT;

	private List<ComboPooledDataSource> comboPooledDataSources = new ArrayList<ComboPooledDataSource>();
	private Map<String, ComboPooledDataSource> comboPooledDataSourceMap = new HashMap<String, ComboPooledDataSource>();

	private int comboPooledDataSourcesSize;
	private int comboPooledDataSourcesCurrent = 0;

	private Map<String, MutableInt> connectionRequestHitCountMap = new HashMap<String, MutableInt>();
	private Map<String, MutableInt> connectionRequestPoolHitCountMap = new HashMap<String, MutableInt>();

	private WeightedMap<ComboPooledDataSource> weightedMap = new WeightedMap<ComboPooledDataSource>();
	private Map<String, WeightedMap<ComboPooledDataSource>> namedWeightMap = new HashMap<String, WeightedMap<ComboPooledDataSource>>();


	/**
	 * A simple class to allow incrementing connection request hit counts
	 * 
	 * @author synapticloop
	 *
	 */
	private class MutableInt {
		private int value = 0;

		public void increment() { ++value; }

		public int getValue() { return value; }
	}

	/**
	 * A simple class to hold the name and the latency for a connection pool
	 * 
	 * @author synapticloop
	 *
	 */
	private class NamedLatencyPool {
		private String name;
		private Float latency;

		public NamedLatencyPool(String name, Float latency) {
			this.name = name;
			this.latency = latency;
		}

		public String getName() { return(this.name); }
		public Float getLatency() {return(this.latency); }
	}
	/**
	 * Create a multi-tenant combo pooled data source, reading the strategy and
	 * the tenants from the passed in property file location.
	 * 
	 * @param propertyFileLocation The property file location to load
	 */
	public MultiTenantComboPooledDataSource(String propertyFileLocation) {
		// try and load the c3p0.multitenant.properties
		Properties properties = new Properties();
		try {
			properties.load(MultiTenantComboPooledDataSource.class.getResourceAsStream(propertyFileLocation));
			// at this point we are looking for tenants and the strategy
			this.strategy = Strategy.valueOf(properties.getProperty(PROPERTY_STRATEGY, DEFAULT_STRATEGY.toString()));

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
					propertyWeightings = KEY_DEFAULT_WEIGHTED_NAME_MAP;
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
					propertyNames = KEY_DEFAULT_WEIGHTED_NAME_MAP;
				}
				this.names = propertyNames.split(",");
				break;
			case LEAST_LATENCY_SERIAL:
				// fall-through ignore
				String propertyNumTriesLatency = properties.getProperty(PROPERTY_NUM_TRIES_LATENCY, "50");
				try {
					this.numTriesLatency = Integer.parseInt(propertyNumTriesLatency);
					if(numTriesLatency <= 0) {
						if(LOGGER.isLoggable(MLevel.WARNING)) {
							LOGGER.log(MLevel.WARNING, String.format("A strategy of '%s' was requested, yet parse the property '%s', was set <= 0, using the default value of: %d", Strategy.LEAST_LATENCY_SERIAL.toString(), PROPERTY_NUM_TRIES_LATENCY, this.numTriesLatency, NUM_TRIES_LATENCY_DEFAULT));
						}
						this.numTriesLatency = NUM_TRIES_LATENCY_DEFAULT;
					}
				} catch(NumberFormatException ex) {
					if(LOGGER.isLoggable(MLevel.WARNING)) {
						LOGGER.log(MLevel.WARNING, String.format("A strategy of '%s' was requested, yet I could not parse the property '%s', using the default value of: %d", Strategy.LEAST_LATENCY_SERIAL.toString(), PROPERTY_NUM_TRIES_LATENCY, this.numTriesLatency));
					}
				}
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
				LOGGER.log(MLevel.SEVERE, String.format("Could not find the '%s' property file", propertyFileLocation), ex);
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

	/**
	 * Create a multi tenant combo pooled data source with a 'named' strategy.  
	 * The list size of the names should be the same as the number of tenants
	 * 
	 * @param tenants The list of tenant names 
	 * @param names the names of the tenants
	 */
	public MultiTenantComboPooledDataSource(List<String> tenants, String[] names) {
		this.tenants = tenants;
		this.names = names;

		// This must always be the weighted strategy
		this.strategy = Strategy.NAMED;

		initialiseMultiTenantPools();
	}

	/**
	 * Initialise the multi-tenant pools, depending on what the strategy and the 
	 * tenants are and any other remaining properties.
	 */
	private void initialiseMultiTenantPools() {
		boolean isDebugEnabled = LOGGER.isLoggable(MLevel.DEBUG);

		if(isDebugEnabled) {
			LOGGER.log(MLevel.DEBUG, String.format("Attempting multi-tenant connection pool with strategy '%s'", this.strategy.toString()));
		}

		Set<String> removableTenants = new HashSet<String>();
		for (String tenant : tenants) {
			if(isDebugEnabled) {
				LOGGER.log(MLevel.DEBUG, String.format("Creating multi-tenant connection pool for tenant '%s'", tenant));
			}

			ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource(tenant);
			if(null == comboPooledDataSource.getJdbcUrl()) {
				// this means that we cannot find the named tenant
				removableTenants.add(tenant);
				LOGGER.log(MLevel.SEVERE, String.format("Could not find named configuration for '%s', this will __NOT__ be added to the pools.", tenant));
			} else {
				comboPooledDataSourceMap.put(tenant, comboPooledDataSource);
	
				if(isDebugEnabled) {
					LOGGER.log(MLevel.DEBUG, String.format("Created multi-tenant connection pool for tenant '%s'", tenant));
				}
			}
		}

		if(removableTenants.size() != 0) {
			List<String> newTenants = new ArrayList<String>();
			for (String tenant : tenants) {
				if(!removableTenants.contains(tenant)) {
					newTenants.add(tenant);
				} else {
					LOGGER.log(MLevel.SEVERE, String.format("Removed tenant '%s' from list of tenants", tenant));
				}
			}
			tenants = newTenants;
		}

		switch (this.strategy) {
		case WEIGHTED:
			for(int i = 0; i < tenants.size(); i++) {
				String tenant = tenants.get(i);
				try {
					weightedMap.add(weightings.get(i), comboPooledDataSourceMap.get(tenant));
				} catch(IndexOutOfBoundsException ex) {
					// we have too few weightings - log it and carry on
					if(LOGGER.isLoggable(MLevel.SEVERE)) {
						LOGGER.severe(String.format("Too few weightings for tenant '%s', setting it to 1", tenants.get(i)));
					}
					weightedMap.add(1, comboPooledDataSourceMap.get(tenant));
				}
			}

			if(weightings.size() > tenants.size()) {
				if(LOGGER.isLoggable(MLevel.SEVERE)) {
					LOGGER.severe(String.format("I received '%d' weightings for the '%d' tenants, ignoring extra weightings...", weightings.size(), tenants.size()));
				}
			}
			break;
		case NAMED:
			for(int i = 0; i < tenants.size(); i++) {

				String tenant = tenants.get(i);

				String name = "";
				try {
					name = names[i];
				} catch(ArrayIndexOutOfBoundsException ex) {
					if(LOGGER.isLoggable(MLevel.SEVERE)) {
						LOGGER.severe(String.format("Too few names for tenant '%s', adding it to the default pool '%s'", tenant, KEY_DEFAULT_WEIGHTED_NAME_MAP));
					}
				}

				WeightedMap<ComboPooledDataSource> weightedMap = namedWeightMap.get(name);
				if(null == weightedMap) {
					weightedMap = new WeightedMap<ComboPooledDataSource>();
					if(LOGGER.isLoggable(MLevel.INFO)) {
						LOGGER.info(String.format("Created new weighted map for tenant pool '%s'", name));
					}
				}
				weightedMap.add(1, comboPooledDataSourceMap.get(tenants.get(i)));

				if(LOGGER.isLoggable(MLevel.INFO)) {
					LOGGER.info(String.format("Inserted new entry into weighted map for tenant pool '%s', for tenant '%s'", name, tenant));
				}

				namedWeightMap.put(name, weightedMap);
				if(!connectionRequestPoolHitCountMap.containsKey(name)) {
					connectionRequestPoolHitCountMap.put(name, new MutableInt());
				}
			}

			if(names.length > tenants.size()) {
				if(LOGGER.isLoggable(MLevel.SEVERE)) {
					LOGGER.severe(String.format("I received '%d' names for the '%d' tenants, ignoring extra names...", names.length, tenants.size()));
				}
			}
			break;
		case LEAST_LATENCY_SERIAL:
			// re-order the connection pools by the latency
			reorderConnectionPoolsByLatency();
			break;
		case SERIAL:
			// do nothing - fall through
		case LOAD_BALANCED:
			// do nothing - fall through
		case ROUND_ROBIN:
			break;
		default:
			// no extra processing for other strategies
			if(LOGGER.isLoggable(MLevel.WARNING)) {
				LOGGER.log(MLevel.WARNING, String.format("Could not find the strategy '%s', defaulting to '%s',", this.strategy.toString(), DEFAULT_STRATEGY.toString()));
			}
			break;
		}

		comboPooledDataSourcesSize = comboPooledDataSources.size();
		if(LOGGER.isLoggable(MLevel.INFO)) {
			LOGGER.log(MLevel.INFO, String.format("Created %s using strategy of '%s'", MultiTenantComboPooledDataSource.class.getSimpleName(), this.strategy));
		}
	}

	/**
	 * Go through each of the connection pools and determine their latency, then
	 * order the connection pool by latency
	 */
	private void reorderConnectionPoolsByLatency() {
		List<NamedLatencyPool> namedLatencyPools = new ArrayList<NamedLatencyPool>();

		for (ComboPooledDataSource comboPooledDataSource : comboPooledDataSources) {
			String dataSourceName = comboPooledDataSource.getDataSourceName();

			long startTime = System.currentTimeMillis();
			boolean isInError = false;

			for(int i = 0; i < numTriesLatency; i++) {
				// try and get some connections and determine the latency
				try {
					comboPooledDataSource.getConnection().close();
				} catch (SQLException ex) {
					if(LOGGER.isLoggable(MLevel.SEVERE)) {
						LOGGER.log(MLevel.SEVERE, String.format("Using strategy of '%s', could not get a connection to data source '%s', setting latency to: %d", this.strategy.toString(), dataSourceName, Float.MAX_VALUE));
					}

					isInError = true;
					break;
				}
			}

			if(isInError) {
				namedLatencyPools.add(new NamedLatencyPool(dataSourceName, Float.MAX_VALUE));
			} else {
				float averageLatency = (System.currentTimeMillis() - startTime)/numTriesLatency;
				namedLatencyPools.add(new NamedLatencyPool(dataSourceName, averageLatency));
				if(LOGGER.isLoggable(MLevel.INFO)) {
					LOGGER.log(MLevel.INFO, String.format("Data source '%s' has an average latency of: %.12f from %d tries", dataSourceName, averageLatency, numTriesLatency));
				}
			}
		}

		// now we can go through and order them
		Collections.sort(namedLatencyPools, new Comparator<NamedLatencyPool>() {

			@Override
			public int compare(NamedLatencyPool o1, NamedLatencyPool o2) {
				return(o1.getLatency().compareTo(o2.getLatency()));
			}
		});

		// now that it is sorted, go through and create a new comboPoolList for 
		// SERIAL strategy usage
		List<ComboPooledDataSource> orderedComboPooledDataSources = new ArrayList<ComboPooledDataSource>();

		for (NamedLatencyPool namedLatencyPool : namedLatencyPools) {
			ComboPooledDataSource comboPooledDataSource = comboPooledDataSourceMap.get(namedLatencyPool.getName());
			orderedComboPooledDataSources.add(comboPooledDataSource);
			if(LOGGER.isLoggable(MLevel.INFO)) {
				LOGGER.log(MLevel.INFO, String.format("Adding data source '%s'.", comboPooledDataSource.getDataSourceName()));
			}
		}

		comboPooledDataSources = orderedComboPooledDataSources;
	}

	/**
	 * Get a connection from one of the pools this will depend on the pool that 
	 * was chosen when instantiation occurred.  If this was instantiation was a 
	 * 'NAMED' strategy, then this will log a SEVERE error and return a round 
	 * robin connection.
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
		case LEAST_LATENCY_SERIAL:
			return(getSerialConnection());
		case WEIGHTED:
			return(getWeightedConnection());
		case NAMED:
			if(LOGGER.isLoggable(MLevel.SEVERE)) {
				LOGGER.log(MLevel.SEVERE, String.format("Called getConnection() where the strategy is '%s', reverting to strategy '%s'", Strategy.NAMED, DEFAULT_STRATEGY));
			}

			return(getRoundRobinConnection());
		default:
			throw new SQLException(String.format("Could not determine the strategy for connections, was looking for '%s'", strategy.toString()));
		}
	}

	/**
	 * Get a connection from a named pool, this method is only valid where the
	 * strategy is NAMED.  All other strategies will return null
	 * 
	 * @param poolName the name of the pool to get a connection from
	 * 
	 * @return the connection form the pool, or null if the strategy is not NAMED
	 * 
	 * @throws SQLException if there was an error getting a connection
	 */
	public Connection getConnection(String poolName) throws SQLException {
		if(LOGGER.isLoggable(MLevel.DEBUG)) {
			LOGGER.log(MLevel.DEBUG, String.format("'%s' connection requested with pool name '%s'", this.strategy.toString(), poolName));
		}

		switch(this.strategy) {
		case ROUND_ROBIN:
		case LOAD_BALANCED:
		case SERIAL:
		case LEAST_LATENCY_SERIAL:
		case WEIGHTED:
			if(LOGGER.isLoggable(MLevel.SEVERE)) {
				LOGGER.log(MLevel.SEVERE, String.format("Cannot get a named connection of '%s' as the '%s' strategy does not support it.", this.strategy.toString(), Strategy.NAMED.toString()));
			}
			return(null);
		case NAMED:
			if(namedWeightMap.containsKey(poolName)) {
				ComboPooledDataSource comboPooledDataSource = namedWeightMap.get(poolName).next();
				incrementRequestPoolHitCountMap(poolName);
				incrementRequestHitCountMap(comboPooledDataSource);
				return(comboPooledDataSource.getConnection());
			} else {
				throw new SQLException(String.format("Could not find the named pool for name '%s'", poolName));
			}
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
	 * Get a serial connection, this will go through the connection pools and
	 * assign a connection up to the maxPoolSize for that pool.  If the pool
	 * is full, it will move on to the next connection pool.
	 * 
	 * If all connection pools are busy, it will return a connection from the
	 * first pool, regardless of whether it is busy or not.
	 * 
	 * @return the connection 
	 * 
	 * @throws SQLException if there was an error with the connection
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

	/**
	 * Get a load balanced connection 
	 * 
	 * @return the connection from the load balancer
	 * 
	 * @throws SQLException If there was an error getting the connection
	 */
	private synchronized Connection getLoadBalancedConnection() throws SQLException {
		int maxBusyConnections = 0;
		int poolIndex = 0;
		int readyPoolIndex = 0;
		String dataSourceName = null;

		try {
			for (ComboPooledDataSource comboPooledDataSource : comboPooledDataSources) {
				// TODO - do we really want to fail on this one... - may need to split out try/catch
				int numBusyConnections = comboPooledDataSource.getNumBusyConnections();
				if(numBusyConnections == 0) {
					readyPoolIndex = poolIndex;
					break;
				} else {

					if(numBusyConnections > maxBusyConnections) {
						// here we don't want to choose it
						maxBusyConnections = numBusyConnections;
					} else if(numBusyConnections != maxBusyConnections) {
						// here the num busy isn't the same, so we assign
						readyPoolIndex = poolIndex;
					}

					poolIndex++;

				}
			}

			ComboPooledDataSource comboPooledDataSource = comboPooledDataSources.get(readyPoolIndex);
			dataSourceName = comboPooledDataSource.getDataSourceName();
			incrementRequestHitCountMap(comboPooledDataSource);
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
		if(null != comboPooledDataSource) {
			incrementRequestHitCountMap(comboPooledDataSource);
			return(comboPooledDataSource.getConnection());
		} else {
			return(null);
		}
	}

	/**
	 * Increment the number of hits for the request to the combo pool
	 * 
	 * @param comboPooledDataSource the combo pool to increment the hit for
	 */
	private void incrementRequestHitCountMap(ComboPooledDataSource comboPooledDataSource) {
		if(null == comboPooledDataSource) {
			return;
		}
		connectionRequestHitCountMap.get(comboPooledDataSource.getDataSourceName()).increment();
	}

	/**
	 * Increment the number of hits for the request to the named pool
	 * 
	 * @param The name to update
	 */
	private void incrementRequestPoolHitCountMap(String name) {
		connectionRequestPoolHitCountMap.get(name).increment();
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

	public int getRequestPoolCountForName(String name) {
		if(connectionRequestPoolHitCountMap.containsKey(name)) {
			return(connectionRequestPoolHitCountMap.get(name).getValue());
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

	/**
	 * Get the total number of weightings for the weighted map.  If the strategy 
	 * is not set to WEIGHTED, then this will return 0;
	 * 
	 * @return the total weightings for the weighted map
	 */
	public int getTotalWeightings() { return(weightedMap.getTotalWeightings()); }
}
