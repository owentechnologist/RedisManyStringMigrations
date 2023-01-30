package com.redislabs.sa.ot.rmsm;

import redis.clients.jedis.JedisPooled;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * this PROGRAM demonstrates writing to 3 databases in an endless ring of timeouts
 * * To run the program with the default settings (supplying the host and port for Redis) do:
 *  mvn compile exec:java -Dexec.cleanupDaemonThreads=false -Dexec.args="--hosta myhost.com --porta 10000 --hostb myhost2.com --portb 10001 --hostc myhost3.com --portc 10003"
*/
public class Main {

    public static void main(String[] args) {
        ArrayList<String> argList = null;
        long writingPeriodSecs = 60;//data is written for 1 min in each DB by default
        int dataTTLSecs = 122;//data lives for ~2 minutes in the system by default before being replaced by newer data
        int programDurationMinutes = 10;//10 min default
        String hostA = "localhost";
        int portA = 6379;
        String hostB = "localhost";
        int portB = 6379;
        String hostC = "localhost";
        int portC = 6379;
        String userName = "default";
        String password = "";
        int maxConnections = 2;
        long writeSleepTimeMillis =100l;

        if (args.length > 0) {
            argList = new ArrayList<>(Arrays.asList(args));
            if (argList.contains("--username")) {
                int argIndex = argList.indexOf("--username");
                userName = argList.get(argIndex + 1);
            }
            if (argList.contains("--password")) {
                int argIndex = argList.indexOf("--password");
                password = argList.get(argIndex + 1);
            }
            if (argList.contains("--hosta")) {
                int argIndex = argList.indexOf("--hosta");
                hostA = argList.get(argIndex + 1);
            }
            if (argList.contains("--porta")) {
                int argIndex = argList.indexOf("--porta");
                portA = Integer.parseInt(argList.get(argIndex + 1));
            }
            if (argList.contains("--hostb")) {
                int argIndex = argList.indexOf("--hostb");
                hostB = argList.get(argIndex + 1);
            }
            if (argList.contains("--portb")) {
                int argIndex = argList.indexOf("--portb");
                portB = Integer.parseInt(argList.get(argIndex + 1));
            }
            if (argList.contains("--hostc")) {
                int argIndex = argList.indexOf("--hostc");
                hostC = argList.get(argIndex + 1);
            }
            if (argList.contains("--portc")) {
                int argIndex = argList.indexOf("--portc");
                portC = Integer.parseInt(argList.get(argIndex + 1));
            }
            if (argList.contains("--writingperiodsecs")) {
                int argIndex = argList.indexOf("--writingperiodsecs");
                writingPeriodSecs = Integer.parseInt(argList.get(argIndex + 1));
                if(!argList.contains("--datattlsecs")){
                    dataTTLSecs = (int) ((writingPeriodSecs*2)+2);
                }
            }
            if (argList.contains("--datattlsecs")) {
                int argIndex = argList.indexOf("--datattlsecs");
                dataTTLSecs = Integer.parseInt(argList.get(argIndex + 1));
            }
            if (argList.contains("--programdurationminutes")) {
                int argIndex = argList.indexOf("--programdurationminutes");
                programDurationMinutes = Integer.parseInt(argList.get(argIndex + 1));
            }
            if (argList.contains("--writesleeptimemillis")) {
                int argIndex = argList.indexOf("--writesleeptimemillis");
                writeSleepTimeMillis = Long.parseLong(argList.get(argIndex + 1));
            }

        }
        long mainThreadExecutionStartTime = System.currentTimeMillis();
        System.out.println("Starting Program");
        JedisConnectionHelper connectionHelperA = new JedisConnectionHelper(hostA, portA, userName, password, maxConnections);
        JedisConnectionHelper connectionHelperB = new JedisConnectionHelper(hostB, portB, userName, password, maxConnections);
        JedisConnectionHelper connectionHelperC = new JedisConnectionHelper(hostC, portC, userName, password, maxConnections);


        //Write and read data for a while
        long loopCounter = -1;
        JedisPooled jedisA = connectionHelperA.getPooledJedis();
        JedisPooled jedisB = connectionHelperB.getPooledJedis();
        JedisPooled jedisC = connectionHelperC.getPooledJedis();
        DateFormat outputformat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        while(System.currentTimeMillis()<(mainThreadExecutionStartTime+(60000*programDurationMinutes))){
            long loopTimeStamp = System.currentTimeMillis();
            loopCounter++;
            System.out.println("\nLOOP COUNTER == "+loopCounter);
            while(System.currentTimeMillis()<loopTimeStamp+(writingPeriodSecs*1000)){
                if(loopCounter%3==0){
                    String latestKey = System.currentTimeMillis()+"blah";
                    jedisA.set(latestKey,"A");
                    jedisA.expire(latestKey,dataTTLSecs);
                    jedisA.set("MOST_RECENT_ENTRY_DATE_TIME",outputformat.format(new Date()));
                    try {
                        Thread.sleep(writeSleepTimeMillis);
                        String newKeyName = jedisA.randomKey();
                        System.out.println("\t\tReading new Data From DataBase_A: " + newKeyName + " : " + jedisA.get(newKeyName)
                                +" TTL == "+jedisA.ttl(newKeyName));
                        String old1KeyName = jedisB.randomKey();
                        System.out.println("Reading older Data From DataBase_B: " + old1KeyName + " : " + jedisB.get(old1KeyName)
                                +" TTL == "+jedisB.ttl(old1KeyName));
                        String old2KeyName = jedisC.randomKey();
                        System.out.println("Reading oldest Data From DataBase_C: " + old2KeyName + " : " + jedisC.get(old2KeyName)
                                +" TTL == "+jedisC.ttl(old2KeyName));
                    }catch(Throwable t){System.out.println(t.getMessage());}
                }
                if(loopCounter%3==2){
                    String latestKey = System.currentTimeMillis()+"blah";
                    jedisB.set(latestKey,"B");
                    jedisB.expire(latestKey,dataTTLSecs);
                    jedisB.set("MOST_RECENT_ENTRY_DATE_TIME",outputformat.format(new Date()));
                    try {
                        Thread.sleep(writeSleepTimeMillis);
                        String newKeyName = jedisB.randomKey();
                        System.out.println("\tReading new Data From DataBase_B: " + newKeyName + " : " + jedisB.get(newKeyName)
                                +" TTL == "+jedisB.ttl(newKeyName));
                        String old1KeyName = jedisC.randomKey();
                        System.out.println("Reading older Data From DataBase_C: " + old1KeyName + " : " + jedisC.get(old1KeyName)
                                +" TTL == "+jedisC.ttl(old1KeyName));
                        String old2KeyName = jedisA.randomKey();
                        System.out.println("Reading oldest Data From DataBase_A: " + old2KeyName + " : " + jedisA.get(old2KeyName)
                                +" TTL == "+jedisA.ttl(old2KeyName));
                    }catch(Throwable t){System.out.println(t.getMessage());}
                }
                if(loopCounter%3==1){
                    String latestKey = System.currentTimeMillis()+"blah";
                    jedisC.set(latestKey,"C");
                    jedisC.expire(latestKey,dataTTLSecs);
                    jedisC.set("MOST_RECENT_ENTRY_DATE_TIME",outputformat.format(new Date()));
                    try{
                        Thread.sleep(writeSleepTimeMillis);
                        String newKeyName =  jedisC.randomKey();
                        System.out.println("\tReading new Data From DataBase_C: "+newKeyName+" : "+jedisC.get(newKeyName)
                                +" TTL == "+jedisC.ttl(newKeyName));
                        String old1KeyName =  jedisA.randomKey();
                        System.out.println("Reading older Data From DataBase_A: "+old1KeyName+" : "+jedisA.get(old1KeyName)
                                +" TTL == "+jedisA.ttl(old1KeyName));
                        String old2KeyName =  jedisB.randomKey();
                        System.out.println("Reading oldest Data From DataBase_B: "+old2KeyName+" : "+jedisB.get(old2KeyName)
                                +" TTL == "+jedisB.ttl(old2KeyName));
                    }catch(Throwable t){System.out.println(t.getMessage());}
                }
            }
        }
    }
}
