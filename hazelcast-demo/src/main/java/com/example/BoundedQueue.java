package com.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.collection.IQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BoundedQueue {
    private static final String QUEUE_NAME = "bounded_Queue";

    public static void main (String[] args){
        Config config = new Config();
        QueueConfig queueConfig = new QueueConfig(QUEUE_NAME);
        queueConfig.setMaxSize(10);
        config.addQueueConfig(queueConfig);
        HazelcastInstance server = Hazelcast.newHazelcastInstance(config);

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName("hello-world");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        IQueue<Integer> queue = client.getQueue(QUEUE_NAME);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(() -> producer(queue));

        executor.submit(() -> consumer(queue, "Consumer-1"));
        executor.submit(() -> consumer(queue, "Consumer-2"));

        executor.shutdown();

        try {
            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                System.err.println("Some tasks didn't finish within the timeout!");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Executor was interrupted.");
        }
    }

    private static void producer(IQueue<Integer> queue){
        for (int i = 1; i <= 100; i++) {
            try {
                queue.put(i); // Блокується, якщо черга заповнена
                System.out.println("Produced: " + i);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Producer finished.");
    }

    private static void consumer(IQueue<Integer> queue, String consumerName) {
        try {
            while (true) {
                Integer value = queue.take();
                System.out.println(consumerName + " consumed: " + value);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
