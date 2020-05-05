package com.yo;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.JedisPool;

public class LockTest {

    protected JedisPool jedisPool;

    @Before
    public void before() {
        jedisPool = new JedisPool();
    }

    @Test
    public void fun() {
        RedisLock redisLock = new RedisLock(jedisPool);
        for (int i = 0; i < 200; i++) {
            redisLock.lock("key1");
        }

    }

    public static void main(String[] args) throws InterruptedException {
        RedisLock redisLock = new RedisLock(new JedisPool(), 5);

        new Thread(() -> {
            redisLock.lock("1");
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            redisLock.unlock("1");
        }).start();

        Thread.sleep(5000);

        new Thread(() -> {
            redisLock.lock("1");
            try {
                Thread.sleep(900);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            redisLock.unlock("1");
        }).start();

        Thread.sleep(500);

        new Thread(() -> {
            redisLock.lock("1");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            redisLock.unlock("1");
        }).start();


    }

}
