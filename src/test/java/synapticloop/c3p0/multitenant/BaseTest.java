package synapticloop.c3p0.multitenant;

import java.util.ArrayList;
import java.util.List;

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

public class BaseTest {
	protected MultiTenantComboPooledDataSource multiTenantComboPooledDataSource;
	protected static final String NAME_READ = "read";
	protected static final String NAME_WRITE = "write";

	protected static final List<Integer> WEIGHTINGS = new ArrayList<Integer>();
	protected static final List<Integer> ZERO_WEIGHTINGS = new ArrayList<Integer>();
	protected static final List<Integer> TOO_MANY_WEIGHTINGS = new ArrayList<Integer>();
	protected static final List<Integer> TOO_FEW_WEIGHTINGS = new ArrayList<Integer>();
	protected static final List<String> TENANTS = new ArrayList<String>();
	protected static final String[] NAMES = { NAME_READ, NAME_READ, NAME_READ, NAME_WRITE };
	protected static final String[] TOO_FEW_NAMES = { NAME_READ, NAME_READ, NAME_WRITE };
	protected static final String[] TOO_MANY_NAMES = { NAME_READ, NAME_READ, NAME_READ, NAME_WRITE, NAME_WRITE };

	static {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		TENANTS.add("one");
		TENANTS.add("two");
		TENANTS.add("three");
		TENANTS.add("four");

		WEIGHTINGS.add(60);
		WEIGHTINGS.add(25);
		WEIGHTINGS.add(10);
		WEIGHTINGS.add(5);

		ZERO_WEIGHTINGS.add(0);
		ZERO_WEIGHTINGS.add(-5);
		ZERO_WEIGHTINGS.add(-1);
		ZERO_WEIGHTINGS.add(null);

		TOO_MANY_WEIGHTINGS.add(60);
		TOO_MANY_WEIGHTINGS.add(25);
		TOO_MANY_WEIGHTINGS.add(10);
		TOO_MANY_WEIGHTINGS.add(5);
		TOO_MANY_WEIGHTINGS.add(5);

		TOO_FEW_WEIGHTINGS.add(60);
		TOO_FEW_WEIGHTINGS.add(25);
		TOO_FEW_WEIGHTINGS.add(10);
	}

}
