package com.yo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

public class RedisQueue {

    Logger logger = LoggerFactory.getLogger(RedisQueue.class);

    private String queue_name;
    private JedisPool jedisPool;
    // 阻塞读取超时时间
    private int pop_timeout = 0;

    public RedisQueue(String queue_name, JedisPool jedisPool) {
        this.queue_name = queue_name;
        this.jedisPool = jedisPool;
    }

    public boolean enqueue(String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            Long lpush = jedis.lpush(queue_name, value);
            return lpush > 0;
        }
    }

    public String dequeue() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> list = jedis.brpop(pop_timeout, queue_name);
            return list == null ? null : list.get(1);
        }
    }

    public void printALl() {
        try (Jedis jedis = jedisPool.getResource()) {
            Long length = jedis.llen(queue_name);
            List<String> list = jedis.lrange(queue_name, 0, length);
            logger.info("{} 内容 : {}", queue_name, list.toArray());
        }
    }

}
