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
A bit of output from the program while it is busy: 

```
LOOP COUNTER == 9
                Reading new Data From DataBase_A: 1675099106229blah : A TTL == 0
Reading older Data From DataBase_B: 1675099168718blah : B TTL == 63
Reading oldest Data From DataBase_C: 1675099147343blah : C TTL == 42
                Reading new Data From DataBase_A: 1675099105957blah : A TTL == 0
Reading older Data From DataBase_B: 1675099186995blah : B TTL == 81
Reading oldest Data From DataBase_C: 1675099155396blah : C TTL == 49
                Reading new Data From DataBase_A: 1675099106857blah : A TTL == 1
Reading older Data From DataBase_B: 1675099188279blah : B TTL == 82
Reading oldest Data From DataBase_C: 1675099156438blah : C TTL == 50
                Reading new Data From DataBase_A: 1675099106481blah : A TTL == 0
Reading older Data From DataBase_B: 1675099211356blah : B TTL == 105
Reading oldest Data From DataBase_C: 1675099147207blah : C TTL == 41
                Reading new Data From DataBase_A: 1675099228059blah : A TTL == 122
Reading older Data From DataBase_B: 1675099195372blah : B TTL == 89
Reading oldest Data From DataBase_C: 1675099161002blah : C TTL == 55
                Reading new Data From DataBase_A: 1675099227766blah : A TTL == 121
Reading older Data From DataBase_B: 1675099215720blah : B TTL == 109
Reading oldest Data From DataBase_C: 1675099143882blah : C TTL == 37
                Reading new Data From DataBase_A: 1675099106857blah : A TTL == 0
Reading older Data From DataBase_B: 1675099202490blah : B TTL == 96
Reading oldest Data From DataBase_C: 1675099117127blah : C TTL == 10
                Reading new Data From DataBase_A: MOST_RECENT_ENTRY_DATE_TIME : 01-30-2023 11:20:28 TTL == -1
Reading older Data From DataBase_B: 1675099216738blah : B TTL == 110
Reading oldest Data From DataBase_C: 1675099153253blah : C TTL == 46
                Reading new Data From DataBase_A: 1675099228846blah : A TTL == 122
Reading older Data From DataBase_B: 1675099181974blah : B TTL == 75
Reading oldest Data From DataBase_C: 1675099135631blah : C TTL == 28
```