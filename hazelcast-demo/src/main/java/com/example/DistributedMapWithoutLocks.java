package com.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DistributedMapWithoutLocks {
    private static final String MAP_NAME = "my-distributed-map";
    private static final String KEY = "key";
    private static final int INCREMENTS = 10_000;
    private static final int CLIENTS = 3;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(CLIENTS);

        for (int i = 0; i < CLIENTS; i++) {
            executor.submit(DistributedMapWithoutLocks::runClient);
        }

        executor.shutdown();
    }

    private static void runClient() {
        ClientConfig config = new ClientConfig();
        config.setClusterName("hello-world");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);
        
        IMap<String, Integer> map = client.getMap(MAP_NAME);

        map.putIfAbsent(KEY, 0);

        for (int k = 0; k < INCREMENTS; k++) {
            int value = map.get(KEY);
            value++;
            map.put(KEY, value);
        }

        client.shutdown();
    }
}
