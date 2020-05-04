package com.yo.lock;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RedisLock {

    Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private JedisPool pool;
    private int expire_time = 5;
    private long retryAwait = 500;
    // 避免任务超时，锁错乱问题
    private ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public RedisLock(JedisPool pool) {
        this.pool = pool;
    }

    public RedisLock(JedisPool pool, int expire_time) {
        this.pool = pool;
        this.expire_time = expire_time;
    }

    public RedisLock(JedisPool pool, int expire_time, long retryAwait) {
        this.pool = pool;
        this.expire_time = expire_time;
        this.retryAwait = retryAwait;
    }

    public boolean lock(String key) {
        final long startTime = System.currentTimeMillis();
        String ret = null;
        while (ret == null) {
            ret = addKey(key);
            // 获取到锁
            if (ret != null) {
                logger.info(Thread.currentThread().getName() + "获取锁成功");
                break;
            }
            // 获取锁超时
            if (System.currentTimeMillis() - startTime - retryAwait > 0) {
                logger.info(Thread.currentThread().getName() + "获取锁超时");
                break;
            }
        }

        return ret != null;
    }


    public String addKey(String key) {
        SetParams params = new SetParams();
        params.nx();
        params.ex(expire_time);

        String value = randomId(1);
        threadLocal.set(value);

        try (Jedis jedis = pool.getResource()) {
            return jedis.set(key, value, params);
        }
    }

    public int unlock(String key) {
        try (Jedis jedis = pool.getResource()) {
            String luaScript = ""
                    + "\nlocal v = redis.call('GET', KEYS[1]);"
                    + "\nlocal r = 0;"
                    + "\nif v == ARGV[1] then"
                    + "\nr =redis.call('DEL',KEYS[1]);"
                    + "\nend"
                    + "\nreturn r";

            List<String> keys = new ArrayList<>();
            keys.add(key);
            List<String> args = new ArrayList<>();
            args.add(threadLocal.get());
            Object r = jedis.eval(luaScript, keys, args);
            int ret = Integer.parseInt(r.toString());

            if (ret > 0)
                logger.info(Thread.currentThread().getName() + "释放锁");

            return ret;
        }
    }

    private final static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
            'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z'};

    private String randomId(int size) {
        char[] cs = new char[size];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = digits[ThreadLocalRandom.current().nextInt(digits.length)];
        }
        return new String(cs);
    }
}
