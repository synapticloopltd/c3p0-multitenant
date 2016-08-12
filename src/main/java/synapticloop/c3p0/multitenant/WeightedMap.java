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

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;

/**
 * This is a weighted map which will return an element, based on its integer
 * weighting from a random selection
 * 
 * @author synapticloop
 *
 * @param <E> The type for the collection 
 */
public class WeightedMap<E> {
	private static final MLogger LOGGER;
	static {
		LOGGER = MLog.getLogger(WeightedMap.class);
	}

	private final NavigableMap<Integer, E> map = new TreeMap<Integer, E>();
	private final Random random;
	private int totalWeightings = 0;

	/**
	 * Instantiate a new weighted map, which will automatically assign a new 
	 * random number generator seeded on the system current time millis.
	 */
	public WeightedMap() {
		this(new Random(System.currentTimeMillis()));
	}

	/**
	 * Instantiate a new weighted map
	 * 
	 * @param random the random number generator to use
	 */
	public WeightedMap(Random random) {
		this.random = random;
	}

	/**
	 * Add a new entry into the weighted map.  If the weight is less than ot 
	 * equal to 0, then it will log an error and ignore adding it to the map.
	 * 
	 * @param weight the weight to add (__MUST__ be greater than 0 to be added)
	 * @param entry the entry the entry to add to the map with the weight
	 */
	public void add(Integer weight, E entry) {
		if (null == weight || weight <= 0) {
			if(LOGGER.isLoggable(MLevel.SEVERE)) {
				LOGGER.log(MLevel.SEVERE, String.format("An entry was attempted to be added to the Map with a weighting of '%d', this was ignored.", weight));
			}
			return;
		}

		totalWeightings += weight;
		map.put(totalWeightings, entry);
	}

	/**
	 * Return the next weighted entry from the map 
	 * 
	 * @return the next weighted entry from the map
	 */
	public E next() {
		if(totalWeightings == 0) {
			return(null);
		}

		int value = random.nextInt(totalWeightings);
		return map.ceilingEntry(value).getValue();
	}

	/**
	 * Get the total weightings for the map
	 * 
	 * @return the total weightings for the map
	 */
	public int getTotalWeightings() { return(this.totalWeightings); }
}
