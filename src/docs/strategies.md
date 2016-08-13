
# Strategies

Built in to the multi tenant connection pools are various strategies for how connections 
are provided.

 - `ROUND_ROBIN` - choose a connection from the next pool
 - `LOAD_BALANCED` - choose a connection from the least busy pool
 - `SERIAL` - choose a connection 
 - `WEIGHTED` - randomly choose a connection from a weighted selection
 - `NAMED` - randomly choose a connection from a pool of pools

The strategies are detailed below in more detail: