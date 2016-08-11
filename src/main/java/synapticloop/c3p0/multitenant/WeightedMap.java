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

/**
 * This is a weighted map which will return an element, based on its integer
 * weighting from a random selection
 * 
 * @author synapticloop
 *
 * @param <E> The type for the collection 
 */
public class WeightedMap<E> {
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
	 * Add a new entry into the weighted map
	 * 
	 * @param weight the weight
	 * @param entry the entry
	 */
	public void add(Integer weight, E entry) {
		if (weight <= 0) return;
		totalWeightings += weight;
		map.put(totalWeightings, entry);
	}

	/**
	 * Return the next weighted entry from the map 
	 * 
	 * @return the next weighted entry from the map
	 */
	public E next() {
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
