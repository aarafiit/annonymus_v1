-- Token bucket rate limiter.
--
-- Runs as a single Redis script so the read-refill-write cycle is atomic: Redis
-- executes it on its command loop with nothing interleaved, which is what stops
-- two concurrent requests from both seeing the same token count and both passing.
--
-- KEYS[1] bucket key
-- ARGV[1] capacity      - maximum tokens, i.e. the largest burst allowed
-- ARGV[2] refillTokens  - tokens added per refill period
-- ARGV[3] refillPeriod  - refill period in milliseconds
-- ARGV[4] requested     - tokens this request wants to spend
--
-- returns { allowed (0|1), tokensRemaining, retryAfterMillis }

local capacity     = tonumber(ARGV[1])
local refillTokens = tonumber(ARGV[2])
local refillPeriod = tonumber(ARGV[3])
local requested    = tonumber(ARGV[4])

-- Redis' own clock, not the caller's. Every application instance therefore
-- shares one time source and clock skew between instances cannot widen a budget.
local time = redis.call('TIME')
local now = (tonumber(time[1]) * 1000) + math.floor(tonumber(time[2]) / 1000)

local bucket = redis.call('HMGET', KEYS[1], 'tokens', 'updatedAt')
local tokens = tonumber(bucket[1])
local updatedAt = tonumber(bucket[2])

if tokens == nil or updatedAt == nil then
    -- First request for this key: hand out a full bucket.
    tokens = capacity
    updatedAt = now
end

-- Refill continuously rather than in discrete windows. This is what removes the
-- fixed-window boundary burst, where a client can spend a full budget at 0:59
-- and another at 1:01.
local elapsed = now - updatedAt
if elapsed > 0 then
    tokens = math.min(capacity, tokens + (elapsed / refillPeriod) * refillTokens)
    updatedAt = now
end

local allowed = 0
local retryAfter = 0

if tokens >= requested then
    tokens = tokens - requested
    allowed = 1
else
    -- Time for the deficit to refill, so the client gets an honest Retry-After
    -- instead of being told to guess.
    retryAfter = math.ceil(((requested - tokens) / refillTokens) * refillPeriod)
end

redis.call('HSET', KEYS[1], 'tokens', tokens, 'updatedAt', updatedAt)

-- Expire once the bucket would have refilled completely: an idle client's key is
-- indistinguishable from a fresh one, so keeping it wastes memory. This bounds
-- memory to the active client set rather than every client ever seen.
redis.call('PEXPIRE', KEYS[1], math.ceil((capacity / refillTokens) * refillPeriod) + refillPeriod)

return { allowed, math.floor(tokens), retryAfter }
