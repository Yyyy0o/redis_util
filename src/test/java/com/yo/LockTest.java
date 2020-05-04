package com.yo;

import com.yo.lock.RedisLock;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class LockTest {

    static final Logger logger = LoggerFactory.getLogger(LockTest.class);

    RedisLock redisLock;

    JedisPool pool;

    @Before
    public void before() {
        pool = new JedisPool();
        redisLock = new RedisLock(pool);
    }

    @Test
    public void fun() {
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
                Thread.sleep(3000);
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
