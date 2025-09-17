# 📘 Redis Syllabus for Spring Boot (2 YOE)

----------

## 1. **Redis Fundamentals**

- What is Redis? (in-memory, key-value, super fast)

- Difference between Redis & traditional DB

- Redis architecture (single-threaded, event loop, in-memory with optional persistence)

- Core data types & use cases:

    - **String** → counters, tokens

    - **Hash** → storing objects (like JSON)

    - **List** → queues

    - **Set** → unique items (e.g. online users)

    - **Sorted Set** → leaderboard, ranking

- TTL & expiration (`EXPIRE`, `TTL`)

- Persistence options: RDB (snapshot) vs AOF (append log)

- Eviction policies: LRU, LFU, noeviction

----------

## 2. **Practical Use Cases**

- Caching expensive DB queries / API responses

- Session management (Spring Session)

- Rate limiting (API protection)

- Pub/Sub messaging

- Distributed locks (using `SETNX`)

----------

## 3. **Spring Boot Integration**

- Dependencies (`spring-boot-starter-data-redis`, `spring-session-data-redis`)

- Configuring Redis connection (`application.properties`)

- RedisTemplate vs StringRedisTemplate

- Serializers (why JSON serializer is better than default JDK)

----------

## 4. **Spring Boot Caching with Redis**

- Enabling caching: `@EnableCaching`

- Cache annotations:

    - `@Cacheable` → read-through cache

    - `@CachePut` → update cache

    - `@CacheEvict` → remove cache

- TTL in cache entries

- Custom cache keys (SpEL expressions)

- Handling stale data, cache misses

- Avoiding cache stampede (random TTL, locks)

----------

## 5. **Spring Boot Advanced Redis Usage**

- Spring Session with Redis (distributed login/session storage)

- Rate limiting with counters + TTL

- Pub/Sub with Redis in Spring Boot

- Distributed locks (conceptual, basic usage)

----------

## 6. **Operational Knowledge**

- What happens if Redis crashes?

- Cache consistency:

    - Cache penetration (missing keys problem)

    - Cache avalanche (all keys expire together)

    - Cache stampede (many threads recompute at once)

- Scaling Redis:

    - Sentinel (failover, HA)

    - Cluster (sharding)

- Monitoring & troubleshooting (`INFO`, `MONITOR`, RedisInsight UI)

- Security basics: authentication, SSL/TLS

----------

## 7. **Interview-Focused Questions**

- What is Redis? When to use it?

- Difference between Redis and DB/cache like EhCache?

- How do you integrate Redis in Spring Boot?

- Explain `@Cacheable`, `@CachePut`, `@CacheEvict`.

- What if Redis is down?

- RedisTemplate vs StringRedisTemplate?

- RDB vs AOF persistence?

- What is cache stampede and how to solve it?
    
