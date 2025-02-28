package com.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class DistributedMapWithPessimisticLock {

    private static final String MAP_NAME = "my-distributed-map";
    private static final String KEY = "key2";
    private static final int INCREMENTS = 10_000;
    private static final int CLIENTS = 3;
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(CLIENTS);

        long globalStart = System.nanoTime();

        for (int i = 0; i < CLIENTS; i++) {
            executor.submit(DistributedMapWithPessimisticLock::runClient);
        }

        executor.shutdown();
        
        try {
            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                System.err.println("Some tasks didn't finish within the timeout!");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Executor was interrupted.");
        }

        long globalEnd = System.nanoTime();
        System.out.println("Total execution time: " + (globalEnd - globalStart) / 1_000_000 + " ms");
    }

    private static void runClient(){
        ClientConfig config = new ClientConfig();
        config.setClusterName("hello-world");

        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);
        IMap<String, Integer> map = client.getMap(MAP_NAME);

        map.putIfAbsent(KEY, 0);

        for (int k = 0; k < 10_000; k++) {
            map.lock(KEY);
            try {
                int value = map.get(KEY);
                value++;
                map.put(KEY, value);
            } finally {
                map.unlock(KEY);
            }
        }

        client.shutdown();
    }
}

