package com.yo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class QueueTest {

    static Logger logger = LoggerFactory.getLogger(QueueTest.class);

    public static void main(String[] args) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxWaitMillis(5 * 1000);
        config.setBlockWhenExhausted(true);
        JedisPool jedisPool = new JedisPool(config);
        RedisQueue queue = new RedisQueue("my_queue", jedisPool);

        logger.info("start");

        logger.info("{} 线程获取到数据 {}", Thread.currentThread().getName(), queue.dequeue());

//        queue.enqueue("abcd");
    }
}
