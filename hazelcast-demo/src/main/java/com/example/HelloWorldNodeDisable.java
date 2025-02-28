package com.example;


import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class HelloWorldNodeDisable {
  public static void main(String[] args) {
    Config helloWorldConfig = new Config();
    helloWorldConfig.setClusterName("hello-world"); 

    
    HazelcastInstance hz = Hazelcast.newHazelcastInstance(helloWorldConfig);
    HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(helloWorldConfig);
    HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(helloWorldConfig);

    ClientConfig config = new ClientConfig();
    config.setClusterName("hello-world");

    HazelcastInstance client = HazelcastClient.newHazelcastClient(config);

    IMap<Integer, String> map = client.getMap("my-distributed-map");

    for (int i = 0; i < 1000; i++) {
        map.put(i, "value" + i);
    }
    System.out.println("Inserted 1000 entries into the map.");
    client.shutdown();

    Thread t1 = new Thread(hz::shutdown);
    Thread t2 = new Thread(hz2::shutdown);

    t1.start();
    t2.start();

    try {
        t1.join(); // Чекаємо, поки завершиться вимкнення першої ноди
        t2.join(); // Чекаємо, поки завершиться вимкнення другої ноди
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    System.out.println("Two nodes have been shut down simultaneously.");
    
  }
}