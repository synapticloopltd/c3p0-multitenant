 <a name="#documentr_top"></a>

> **This project requires JVM version of at least 1.7**


[![Download](https://api.bintray.com/packages/synapticloop/maven/c3p0-multitenant/images/download.svg)](https://bintray.com/synapticloop/maven/c3p0-multitenant/_latestVersion) [![GitHub Release](https://img.shields.io/github/release/synapticloopltd/c3p0-multitenant.svg)](https://github.com/synapticloopltd/c3p0-multitenant/releases) 



<a name="documentr_heading_0"></a>

# c3p0-multitenant <sup><sup>[top](#)</sup></sup>



> Multi tenanted c3p0 pool






<a name="documentr_heading_1"></a>

# Table of Contents <sup><sup>[top](#)</sup></sup>



 - [c3p0-multitenant](#documentr_heading_0)
 - [Table of Contents](#documentr_heading_1)
 - [Strategies](#documentr_heading_2)
   - [ROUND_ROBIN](#documentr_heading_3)
   - [LOAD_BALANCED](#documentr_heading_5)
   - [SERIAL](#documentr_heading_7)
   - [LEAST_LATENCY_SERIAL](#documentr_heading_9)
   - [WEIGHTED](#documentr_heading_11)
   - [NAMED](#documentr_heading_13)
 - [Building the Package](#documentr_heading_15)
   - [*NIX/Mac OS X](#documentr_heading_16)
   - [Windows](#documentr_heading_17)
 - [Running the Tests](#documentr_heading_18)
   - [*NIX/Mac OS X](#documentr_heading_19)
   - [Windows](#documentr_heading_20)
 - [Additional Testing Notes](#documentr_heading_21)
 - [Artefact Publishing - Github](#documentr_heading_22)
 - [Artefact Publishing - Bintray](#documentr_heading_23)
   - [maven setup](#documentr_heading_24)
   - [gradle setup](#documentr_heading_25)
   - [Dependencies - Gradle](#documentr_heading_26)
   - [Dependencies - Maven](#documentr_heading_27)
   - [Dependencies - Downloads](#documentr_heading_28)
 - [License](#documentr_heading_34)


# Multi-Tenanted c3p0 connection pools

This was originally designed to connect to cockroachdb see [https://www.cockroachlabs.com/](https://www.cockroachlabs.com/) to allow multiple connections to multiple data sources.  However this can be used for any database connection pooling to multiple database.

This extends c3p0 through named connections to allow multiple pools to themselves be pooled.

An example `c3p0.properties` file is listed below:






```
#
# These are default properties which all of the named configurations inherit
#
c3p0.user=multitenant
c3p0.password=

c3p0.maxPoolSize=2
c3p0.maxStatements=0
c3p0.maxStatementsPerConnection=0
c3p0.minPoolSize=2
c3p0.acquireIncrement=1
c3p0.initialPoolSize=2
c3p0.acquireRetryAttempts=1
c3p0.idleConnectionTestPeriod=1
c3p0.debugUnreturnedConnectionStackTraces=true
c3p0.testConnectionOnCheckout=true
c3p0.preferredTestQuery=select 1

#
# Here we list the named configurations for all of the connections pools that we 
# want to set up, these will all inherit the above configuration items, although
# they can be over-ridden if required: e.g.:
#
#    c3p0.named-configs.one.user=another_user
#
c3p0.named-configs.one.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant
c3p0.named-configs.two.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant
c3p0.named-configs.three.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant
c3p0.named-configs.four.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant

# Logging properties
com.mchange.v2.log.MLog=fallback
com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL=FINE
```



__NOTE__ the above lines with the `named-configs`:



```
c3p0.named-configs.one.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant
c3p0.named-configs.two.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant
c3p0.named-configs.three.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant
c3p0.named-configs.four.jdbcUrl=jdbc:postgresql://localhost:26257/multitenant
```



The named configs are

 - `one`
 - `two`
 - `three`
 - `four`

Once the named configurations are set up, you will then need a `c3p0.multitenant.properties` 
file to determine the strategy that will be used.  A complete example is listed 
below, with all of the options that are available.




```

# strategy can be one of
#
#  ROUND_ROBIN
#  LOAD_BALANCED
#  SERIAL
#  LEAST_LATENCY_SERIAL
#  WEIGHTED
#  NAMED
#
# by default, if nothing is set, the strategy will be ROUND_ROBIN
# strategy=NAMED

#
# This is a list of tenants -i.e. named configurations for the c3p0 configuration
#
tenants=one,two,three,four

#
# NOTE: This property is only used if the strategy listed above is WEIGHTED
#
# The weightings that will be applied to the tenants, doesn't need to add up to
# 100, can add up to anything, they are relative to the total.  These must be in
# the same order as the tenants listed above, i.e.:
#
#   one   => 60 / 100 weighting
#   two   => 25 / 100 weighting
#   three => 10 / 100 weighting
#   four  =>  5 / 100 weighting
# 
# If there are too few weightings, the missing weights will be set to 1, if 
# there are too many weightings, the extra weightings will be ignored.
#

weightings=60,25,10,5

#
# NOTE: This property is only used if the strategy listed above is NAMED
#
# The names that will be applied to the tenants.  Each of the names allow the 
# tenants to be grouped into a sub-pool, to utilise the pool, a call to 
# getConnection(String poolName), NOT just getConnection().  If a call to 
# getConnection() is used, then it will return a random connection.  Within each
# sub-pool, the connection that is retrieved is randonmly assigned.
#
# These __MUST__ be in the same order as the tenants listed above, i.e.:
#
#   one   => read
#   two   => read
#   three => read
#   four  => write
# 
# If there are too few names, the missing names will not be added to any pool 
# and will not be accessible.  If there are too many names, the extra names will
# be ignored.
#
names=read,read,read,write

#
# NOTE: This property is only used if the strategy listed above is LEAST_LATENCY_SERIAL
#
# This is the number of tries for each connection to determine the latency, if
# not set, then it will default to 50
#
num_tries_latency=50



```






<a name="documentr_heading_2"></a>

# Strategies <sup><sup>[top](#)</sup></sup>

Built in to the multi tenant connection pools are various strategies for how connections 
are provided.

 - `ROUND_ROBIN` - choose a connection from the next pool
 - `LOAD_BALANCED` - choose a connection from the least busy pool
 - `SERIAL` - exhaust connections from the first available pool
 - `LEAST_LATENCY_SERIAL` - exhaust connections from the least latency pool
 - `WEIGHTED` - randomly choose a connection from a weighted selection
 - `NAMED` - randomly choose a connection from a pool of pools

The strategies are detailed below in more detail:


<a name="documentr_heading_3"></a>

## `ROUND_ROBIN` <sup><sup>[top](#)</sup></sup>

This is the default strategy for the multi tenanted pool and simply round robins the connection requests through all of the pools of connection pools

To instantiate a `ROUND_ROBIN` strategy pool and get a connection:



```
import java.sql.Connection;
import java.util.List;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource;
import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;


List<String> TENANTS = new ArrayList<String>();

TENANTS.add("one");
TENANTS.add("two");
TENANTS.add("three");
TENANTS.add("four");

MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.ROUND_ROBIN);
Connection connection = multiTenantComboPooledDataSource.getConnection();
```



### Property file usage

Should you wish to use property files, the pool of connection pools can be instantiated thusly:



```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.round_robin.properties");
```



__Note that the property file is loaded from the classpath and can be named anything__

The property file required for this strategy is listed below:






```

# strategy can be one of
#
#  ROUND_ROBIN
#  LOAD_BALANCED
#  SERIAL
#  LEAST_LATENCY_SERIAL
#  WEIGHTED
#  NAMED
#
# by default, if nothing is set, the strategy will be ROUND_ROBIN
strategy=ROUND_ROBIN

#
# This is a list of tenants -i.e. named configurations for the c3p0 configuration
#
tenants=one,two,three,four


```






<a name="documentr_heading_5"></a>

## `LOAD_BALANCED` <sup><sup>[top](#)</sup></sup>

This load balances the connections between all of the pools of connection pools.  This strategy looks at the busy connections on all of the pools and chooses the one based on the minimum number of busy connections.

> In effect this will use a connection from the least busy pool

To instantiate a `LOAD_BALANCED` strategy pool and get a connection:



```
import java.sql.Connection;
import java.util.List;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource;
import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;


List<String> TENANTS = new ArrayList<String>();

TENANTS.add("one");
TENANTS.add("two");
TENANTS.add("three");
TENANTS.add("four");

MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.LOAD_BALANCED);
Connection connection = multiTenantComboPooledDataSource.getConnection();
```




### Property file usage

Should you wish to use property files, the pool of connection pools can be instantiated thusly:



```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.load_balanced.properties");
```



__Note that the property file is loaded from the classpath and can be named anything__

The property file required for this strategy is listed below:







```

# strategy can be one of
#
#  ROUND_ROBIN
#  LOAD_BALANCED
#  SERIAL
#  LEAST_LATENCY_SERIAL
#  WEIGHTED
#  NAMED
#
# by default, if nothing is set, the strategy will be ROUND_ROBIN
strategy=LOAD_BALANCED

#
# This is a list of tenants -i.e. named configurations for the c3p0 configuration
#
tenants=one,two,three,four



```






<a name="documentr_heading_7"></a>

## `SERIAL` <sup><sup>[top](#)</sup></sup>

This strategy chooses connections based on the number of busy connections, it will try and fill up all of the connection pool for the first available pool of connection pools.  Once this pool has been exhausted, it will move on to the next one (and so on for all of the available pools of connection pools).  Once the connections are freed from the earlier pools, it will re-use them.

> In effect this will use up as many connections from the first available pool of connection pools

To instantiate a `SERIAL` strategy pool and get a connection:



```
import java.sql.Connection;
import java.util.List;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource;
import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;


List<String> TENANTS = new ArrayList<String>();

TENANTS.add("one");
TENANTS.add("two");
TENANTS.add("three");
TENANTS.add("four");

MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.SERIAL);
Connection connection = multiTenantComboPooledDataSource.getConnection();
```



### Property file usage

Should you wish to use property files, the pool of connection pools can be instantiated thusly:



```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.serial.properties");
```



__Note that the property file is loaded from the classpath and can be named anything__

The property file required for this strategy is listed below:






```

# strategy can be one of
#
#  ROUND_ROBIN
#  LOAD_BALANCED
#  SERIAL
#  LEAST_LATENCY_SERIAL
#  WEIGHTED
#  NAMED
#
# by default, if nothing is set, the strategy will be ROUND_ROBIN
strategy=SERIAL

#
# This is a list of tenants -i.e. named configurations for the c3p0 configuration
#
tenants=one,two,three,four



```






<a name="documentr_heading_9"></a>

## `LEAST_LATENCY_SERIAL` <sup><sup>[top](#)</sup></sup>

This strategy chooses the connection with the least latency and the number of busy connections  It will try and fill up all of the connection pool for the least latency pool of connection pools.  Once this pool has been exhausted, it will move on to the next one (and so on for all of the available pools of connection pools).  Once the connections are freed from the earlier pools, it will re-use them.

This will allow you to have distributed systems use the closest available connection pool.

In order to do this, 10 connections are serially retrieved from each of the pools on startup, and then the strategy is the same as `SERIAL`

> In effect this will use up as many connections from the least latency pool of connection pools, before consuming connections from the next pool

To instantiate a `LEAST_LATENCY_SERIAL` strategy pool and get a connection:



```
import java.sql.Connection;
import java.util.List;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource;
import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;


List<String> TENANTS = new ArrayList<String>();

TENANTS.add("one");
TENANTS.add("two");
TENANTS.add("three");
TENANTS.add("four");

MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.LEAST_LATENCY_SERIAL);
Connection connection = multiTenantComboPooledDataSource.getConnection();
```



### Property file usage

Should you wish to use property files, the pool of connection pools can be instantiated thusly:



```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.least-latency-serial.properties");
```



__Note that the property file is loaded from the classpath and can be named anything__

The property file required for this strategy is listed below:






```

# strategy can be one of
#
#  ROUND_ROBIN
#  LOAD_BALANCED
#  SERIAL
#  LEAST_LATENCY_SERIAL
#  WEIGHTED
#  NAMED
#
# by default, if nothing is set, the strategy will be ROUND_ROBIN
strategy=LEAST_LATENCY_SERIAL

#
# This is a list of tenants -i.e. named configurations for the c3p0 configuration
#
tenants=one,two,three,four

#
# This is the number of tries for each connection to determine the latency, if
# not set, then it will default to 50
#
num_tries_latency=50


```






<a name="documentr_heading_11"></a>

## WEIGHTED <sup><sup>[top](#)</sup></sup>

This strategy allows you to weight certain connections for more use, than others, you are required to add in the weightings, or they will be equally weighted - in effect giving all of the pools of connection pools equal chance to be selected for usage.

> In effect this will randomly choose a connection from the pool weighted with the passed in weightings

To instantiate a `WEIGHTED` strategy pool and get a connection:



```
import java.sql.Connection;
import java.util.List;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource;
import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;


List<String> TENANTS = new ArrayList<String>();

TENANTS.add("one");
TENANTS.add("two");
TENANTS.add("three");
TENANTS.add("four");

List<Integer> WEIGHTINGS = new ArrayList<Integer>();
WEIGHTINGS.add(60);
WEIGHTINGS.add(25);
WEIGHTINGS.add(10);
WEIGHTINGS.add(5);

MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, WEIGHTINGS);
Connection connection = multiTenantComboPooledDataSource.getConnection();

```



In the above example, on average, **60%** will come from the first connection pool, **25%** from the second, **10%** from the third and **5%** from the fourth.  

If you instantiate a `WEIGHTED` strategy thusly:



```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, Strategy.WEIGHTED);
```



Then all of the connection pools will be given a default weighting of 1.


### Property file usage

Should you wish to use property files, the pool of connection pools can be instantiated thusly:



```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.weighted.properties");
```



__Note that the property file is loaded from the classpath and can be named anything__

The property file required for this strategy is listed below:






```

# strategy can be one of
#
#  ROUND_ROBIN
#  LOAD_BALANCED
#  SERIAL
#  LEAST_LATENCY_SERIAL
#  WEIGHTED
#  NAMED
#
# by default, if nothing is set, the strategy will be ROUND_ROBIN
strategy=WEIGHTED

#
# This is a list of tenants -i.e. named configurations for the c3p0 configuration
#
tenants=one,two,three,four

#
# NOTE: This property is only used if the strategy listed above is WEIGHTED
#
# The weightings that will be applied to the tenants, doesn't need to add up to
# 100, can add up to anything, they are relative to the total.  These must be in
# the same order as the tenants listed above, i.e.:
#
#   one   => 60 / 100 weighting
#   two   => 25 / 100 weighting
#   three => 10 / 100 weighting
#   four  =>  5 / 100 weighting
# 
# If there are too few weightings, the missing weights will be set to 1, if 
# there are too many weightings, the extra weightings will be ignored.
#

weightings=60,25,10,5



```






<a name="documentr_heading_13"></a>

## `NAMED` <sup><sup>[top](#)</sup></sup>

This strategy allows you to pool the pool of connection pools by name.  As an example, you may have `read` only databases that your web application mainly uses, whilst you may have `write` databases that is used by the back end system.

To instantiate a `WEIGHTED` strategy pool and get a connection:



```
import java.sql.Connection;
import java.util.List;

import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource;
import synapticloop.c3p0.multitenant.MultiTenantComboPooledDataSource.Strategy;


List<String> TENANTS = new ArrayList<String>();

TENANTS.add("one");
TENANTS.add("two");
TENANTS.add("three");
TENANTS.add("four");

String[] NAMES = { "read", "read", "read", "write" };

MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource(TENANTS, NAMES);

# to return a connection from the 'read' pool
Connection readConnection = multiTenantComboPooledDataSource.getConnection("read");

# to return a connection from the 'write' pool
Connection writeConnection = multiTenantComboPooledDataSource.getConnection("write");
```



If you mistakenly call `getConnection()` without passing in the named pool, you will get a `ROUND_ROBIN` connection.

### Property file usage

Should you wish to use property files, the pool of connection pools can be instantiated thusly:



```
MultiTenantComboPooledDataSource multiTenantComboPooledDataSource = new MultiTenantComboPooledDataSource("/c3p0.multitenant.named.properties");
```



__Note that the property file is loaded from the classpath and can be named anything__

The property file required for this strategy is listed below:






```

# strategy can be one of
#
#  ROUND_ROBIN
#  LOAD_BALANCED
#  SERIAL
#  LEAST_LATENCY_SERIAL
#  WEIGHTED
#  NAMED
#
# by default, if nothing is set, the strategy will be ROUND_ROBIN
strategy=NAMED

#
# This is a list of tenants -i.e. named configurations for the c3p0 configuration
#
tenants=one,two,three,four

#
# NOTE: This property is only used if the strategy listed above is NAMED
#
# The names that will be applied to the tenants.  Each of the names allow the 
# tenants to be grouped into a sub-pool, to utilise the pool, a call to 
# getConnection(String poolName), NOT just getConnection().  If a call to 
# getConnection() is used, then it will return a random connection.  Within each
# sub-pool, the connection that is retrieved is randonmly assigned.
#
# These __MUST__ be in the same order as the tenants listed above, i.e.:
#
#   one   => read
#   two   => read
#   three => read
#   four  => write
# 
# If there are too few names, the missing names will not be added to any pool 
# and will not be accessible.  If there are too many names, the extra names will
# be ignored.
#
names=read,read,read,write



```







<a name="documentr_heading_15"></a>

# Building the Package <sup><sup>[top](#)</sup></sup>



<a name="documentr_heading_16"></a>

## *NIX/Mac OS X <sup><sup>[top](#)</sup></sup>

From the root of the project, simply run

`./gradlew build`




<a name="documentr_heading_17"></a>

## Windows <sup><sup>[top](#)</sup></sup>

`./gradlew.bat build`


This will compile and assemble the artefacts into the `build/libs/` directory.

Note that this may also run tests (if applicable see the Testing notes)



<a name="documentr_heading_18"></a>

# Running the Tests <sup><sup>[top](#)</sup></sup>



<a name="documentr_heading_19"></a>

## *NIX/Mac OS X <sup><sup>[top](#)</sup></sup>

From the root of the project, simply run

`gradle --info test`

if you do not have gradle installed, try:

`gradlew --info test`



<a name="documentr_heading_20"></a>

## Windows <sup><sup>[top](#)</sup></sup>

From the root of the project, simply run

`gradle --info test`

if you do not have gradle installed, try:

`./gradlew.bat --info test`


The `--info` switch will also output logging for the tests




<a name="documentr_heading_21"></a>

# Additional Testing Notes <sup><sup>[top](#)</sup></sup>

In order for the test to run, you __MUST__ have a cockroach DB setup:


> see [https://www.cockroachlabs.com/docs/install-cockroachdb.html](https://www.cockroachlabs.com/docs/install-cockroachdb.html) for installation notes for your platform

You can use the in-built `start-cockroach.sh` bash script (*NIX variants) - to start up all of the nodes and create the user.



```
#!/bin/bash

cockroach start --store=cockroachdb/node1 --port=26257 --http-port=8080 --host=localhost --insecure --background
cockroach start --store=cockroachdb/node2 --port=26258 --http-port=8081 --host=localhost --insecure --join=localhost:26257 --background
cockroach start --store=cockroachdb/node3 --port=26259 --http-port=8082 --host=localhost --insecure --join=localhost:26257 --background
cockroach start --store=cockroachdb/node4 --port=26260 --http-port=8083 --host=localhost --insecure --join=localhost:26257 --background

cockroach sql --insecure --execute="create database if not exists multitenant" --execute="create user if not exists multitenant" --execute="grant all on database multitenant to multitenant"
```



This will start 4 nodes all clustered together and this will allow the user `multitenant` to connect to the database.

You can use the in-built `stop-cockroach.sh` bash script (*NIX variants) - to stop all of the nodes.



<a name="documentr_heading_22"></a>

# Artefact Publishing - Github <sup><sup>[top](#)</sup></sup>

This project publishes artefacts to [GitHub](https://github.com/)

> Note that the latest version can be found [https://github.com/synapticloopltd/c3p0-multitenant/releases](https://github.com/synapticloopltd/c3p0-multitenant/releases)

As such, this is not a repository, but a location to download files from.



<a name="documentr_heading_23"></a>

# Artefact Publishing - Bintray <sup><sup>[top](#)</sup></sup>

This project publishes artefacts to [bintray](https://bintray.com/)

> Note that the latest version can be found [https://bintray.com/synapticloop/maven/c3p0-multitenant/view](https://bintray.com/synapticloop/maven/c3p0-multitenant/view)



<a name="documentr_heading_24"></a>

## maven setup <sup><sup>[top](#)</sup></sup>

this comes from the jcenter bintray, to set up your repository:



```
<?xml version="1.0" encoding="UTF-8" ?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd' xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
  <profiles>
    <profile>
      <repositories>
        <repository>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
          <id>central</id>
          <name>bintray</name>
          <url>http://jcenter.bintray.com</url>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
          <id>central</id>
          <name>bintray-plugins</name>
          <url>http://jcenter.bintray.com</url>
        </pluginRepository>
      </pluginRepositories>
      <id>bintray</id>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>bintray</activeProfile>
  </activeProfiles>
</settings>
```





<a name="documentr_heading_25"></a>

## gradle setup <sup><sup>[top](#)</sup></sup>

Repository



```
repositories {
	maven {
		url  "http://jcenter.bintray.com" 
	}
}
```



or just



```
repositories {
	jcenter()
}
```





<a name="documentr_heading_26"></a>

## Dependencies - Gradle <sup><sup>[top](#)</sup></sup>



```
dependencies {
	runtime(group: 'synapticloop', name: 'c3p0-multitenant', version: '1.2.1', ext: 'jar')

	compile(group: 'synapticloop', name: 'c3p0-multitenant', version: '1.2.1', ext: 'jar')
}
```



or, more simply for versions of gradle greater than 2.1



```
dependencies {
	runtime 'synapticloop:c3p0-multitenant:1.2.1'

	compile 'synapticloop:c3p0-multitenant:1.2.1'
}
```





<a name="documentr_heading_27"></a>

## Dependencies - Maven <sup><sup>[top](#)</sup></sup>



```
<dependency>
	<groupId>synapticloop</groupId>
	<artifactId>c3p0-multitenant</artifactId>
	<version>1.2.1</version>
	<type>jar</type>
</dependency>
```





<a name="documentr_heading_28"></a>

## Dependencies - Downloads <sup><sup>[top](#)</sup></sup>


You will also need to download the following dependencies:



### cobertura dependencies

  - `net.sourceforge.cobertura:cobertura:2.1.1`: (It may be available on one of: [bintray](https://bintray.com/net.sourceforge.cobertura/maven/cobertura/2.1.1/view#files/net.sourceforge.cobertura/cobertura/2.1.1) [mvn central](http://search.maven.org/#artifactdetails|net.sourceforge.cobertura|cobertura|2.1.1|jar))


### compile dependencies

  - `com.mchange:c3p0:0.9.5.2`: (It may be available on one of: [bintray](https://bintray.com/com.mchange/maven/c3p0/0.9.5.2/view#files/com.mchange/c3p0/0.9.5.2) [mvn central](http://search.maven.org/#artifactdetails|com.mchange|c3p0|0.9.5.2|jar))


### runtime dependencies

  - `com.mchange:c3p0:0.9.5.2`: (It may be available on one of: [bintray](https://bintray.com/com.mchange/maven/c3p0/0.9.5.2/view#files/com.mchange/c3p0/0.9.5.2) [mvn central](http://search.maven.org/#artifactdetails|com.mchange|c3p0|0.9.5.2|jar))


### testCompile dependencies

  - `org.postgresql:postgresql:42.2.2`: (It may be available on one of: [bintray](https://bintray.com/org.postgresql/maven/postgresql/42.2.2/view#files/org.postgresql/postgresql/42.2.2) [mvn central](http://search.maven.org/#artifactdetails|org.postgresql|postgresql|42.2.2|jar))
  - `junit:junit:4.12`: (It may be available on one of: [bintray](https://bintray.com/junit/maven/junit/4.12/view#files/junit/junit/4.12) [mvn central](http://search.maven.org/#artifactdetails|junit|junit|4.12|jar))


### testRuntime dependencies

  - `org.postgresql:postgresql:42.2.2`: (It may be available on one of: [bintray](https://bintray.com/org.postgresql/maven/postgresql/42.2.2/view#files/org.postgresql/postgresql/42.2.2) [mvn central](http://search.maven.org/#artifactdetails|org.postgresql|postgresql|42.2.2|jar))
  - `junit:junit:4.12`: (It may be available on one of: [bintray](https://bintray.com/junit/maven/junit/4.12/view#files/junit/junit/4.12) [mvn central](http://search.maven.org/#artifactdetails|junit|junit|4.12|jar))
  - `org.slf4j:slf4j-api:1.7.25`: (It may be available on one of: [bintray](https://bintray.com/org.slf4j/maven/slf4j-api/1.7.25/view#files/org.slf4j/slf4j-api/1.7.25) [mvn central](http://search.maven.org/#artifactdetails|org.slf4j|slf4j-api|1.7.25|jar))

**NOTE:** You may need to download any dependencies of the above dependencies in turn (i.e. the transitive dependencies)



<a name="documentr_heading_34"></a>

# License <sup><sup>[top](#)</sup></sup>



```
The MIT License (MIT)

Copyright (c) 2018 synapticloop

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```




--

> `This README.md file was hand-crafted with care utilising synapticloop`[`templar`](https://github.com/synapticloop/templar/)`->`[`documentr`](https://github.com/synapticloop/documentr/)

--
