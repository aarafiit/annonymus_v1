package com.example.annonymus_v1.controller;

import com.example.annonymus_v1.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisController {


    private final RedisService redisService;

    @PostMapping("/set")
    public ResponseEntity<String> setValue(@RequestParam String key, @RequestParam String value) {
        redisService.setValue(key, value);
        return ResponseEntity.ok("Value set successfully for key: " + key);
    }

    @PostMapping("/set-with-ttl")
    public ResponseEntity<String> setValueWithTTL(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam long ttl) {
        redisService.setValue(key, value, ttl, TimeUnit.SECONDS);
        return ResponseEntity.ok("Value set successfully with TTL: " + ttl + " seconds");
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getValue(@RequestParam String key) {
        Object value = redisService.getValue(key);
        if (value != null) {
            return ResponseEntity.ok(value);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteValue(@RequestParam String key) {
        redisService.deleteValue(key);
        return ResponseEntity.ok("Value deleted successfully for key: " + key);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> hasKey(@RequestParam String key) {
        boolean exists = redisService.hasKey(key);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/ttl")
    public ResponseEntity<Long> getTTL(@RequestParam String key) {
        Long ttl = redisService.getExpire(key);
        return ResponseEntity.ok(ttl);
    }
}
