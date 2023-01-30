* This very simple PROGRAM takes a look at using 3 databases in an endless ring of timeouts
* It is a possible remedy for conditions that would force partitioning data by date
####    IE:  Put today's information in DB_A, Yesterday's in DB_B and the day before that in DB_C - then ... 
#### Rotate the instances and time out the data so you always have the latest and the oldest data vanishes with TTL/Expiration 
* In reality - you would likely have some dedicated process that populates the databases and shifts from one to another as needed. 
  
Clients would look for the MOST_RECENT_ENTRY_DATE_TIME key in each of the databases and by comparing those values it is fairly obvious which instance has what partition of data
  
* To run this example program, you need to supply three hosts and ports for the 3 rotating instances of Redis
* There are a few other tweakable settings - like the duration of the program, the TTL to set on the entries written, the amount of time to write to each instance, etc...
* Check the main program for all the possible args... below is the bare minimum start command:
```
mvn compile exec:java -Dexec.cleanupDaemonThreads=false -Dexec.args="--hosta myhost.com --porta 10000 --hostb myhost2.com --portb 10001 --hostc myhost3.com --portc 10003"
```
The program writes entries to one of the databases for the specified write duration, before switching to the next instance.

The program reads a random key from each of the databases every time something new is written. 
<br/>Below is a bit of output from the program while it is busy: 

```
LOOP COUNTER == 3
                Reading new Data From DataBase_A: 1675115480020blah : A TTL == 121
Reading older Data From DataBase_B: 1675115431527blah : B TTL == 73
Reading oldest Data From DataBase_C: 1675115398703blah : C TTL == 40
                Reading new Data From DataBase_A: 1675115480020blah : A TTL == 121
Reading older Data From DataBase_B: 1675115423084blah : B TTL == 64
Reading oldest Data From DataBase_C: 1675115391792blah : C TTL == 33
                Reading new Data From DataBase_A: 1675115481079blah : A TTL == 121
Reading older Data From DataBase_B: 1675115420951blah : B TTL == 61
Reading oldest Data From DataBase_C: 1675115384933blah : C TTL == 25

```
