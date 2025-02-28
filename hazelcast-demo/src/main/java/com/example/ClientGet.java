package com.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class ClientGet {
    private static final String MAP_NAME = "my-distributed-map";
    private static final String KEY = "key3";
    public static void main(String[] args) {
        ClientConfig config = new ClientConfig();
        config.setClusterName("hello-world");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);

        IMap<String, Integer> map = client.getMap(MAP_NAME);
        int value = map.get(KEY);

        System.out.println("Key3 value: "+ value);
        client.shutdown();

    }

}
