package com.example.annonymus_v1.config;

import com.example.annonymus_v1.util.AppTime;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Pins the process clock to Bangladesh Standard Time.
 *
 * <p>The entities already stamp rows through {@link AppTime}, so this is not what
 * makes stored timestamps correct. It exists so everything the entities do not
 * control - JDBC conversions, log lines, Jackson's rendering of any date it is
 * handed - reads the same clock, rather than the deployment host's zone. Without
 * it a container running on UTC would print and parse times six hours away from
 * the ones in the database.
 */
@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void applyDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(AppTime.BANGLADESH));
    }
}
