package com.finsight.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Standard enterprise event metadata header attached to all Kafka domain events.
 * Enables distributed tracing, correlation, idempotent processing, and schema evolution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventHeader implements Serializable {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private String eventType;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private String source;

    @Builder.Default
    private String version = "1.0";

    private String correlationId;
}
