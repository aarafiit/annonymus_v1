package com.example.annonymus_v1.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * The single definition of "now" for persisted timestamps.
 *
 * <p>Every stored timestamp is a wall clock reading in Bangladesh Standard Time
 * (UTC+6), because that is the only clock the readers of this record keep. The
 * columns are {@code timestamp without time zone}, so a value written against the
 * host's default zone would silently mean something different on a server deployed
 * abroad; naming the zone here removes that dependency on where the process runs.
 */
public final class AppTime {

    public static final ZoneId BANGLADESH = ZoneId.of("Asia/Dhaka");

    private AppTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(BANGLADESH);
    }
}
