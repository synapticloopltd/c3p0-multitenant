package synapticloop.c3p0.multitenant.util;

import java.sql.Connection;
import java.sql.SQLException;

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

public class SlowQueryThread implements Runnable {
	private Connection connection;
	private long numMillisSleep = 100;

	public SlowQueryThread(Connection connection) {
		this.connection = connection;
	}

	public SlowQueryThread(Connection connection, long numMillisSleep) {
		this.connection = connection;
		this.numMillisSleep = numMillisSleep;
	}

	@Override
	public void run() {
		try {
			connection.setAutoCommit(false);
			Thread.sleep(numMillisSleep);
			connection.commit();
			connection.setAutoCommit(true);
			connection.close();
		} catch (InterruptedException | SQLException ex) {
			// do nothing
		}
	}

}
