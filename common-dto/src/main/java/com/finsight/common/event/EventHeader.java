package com.finsight.common.event;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Standard enterprise event metadata header attached to all Kafka domain events.
 * Enables distributed tracing, correlation, idempotent processing, and schema evolution.
 */
@Data
public class EventHeader implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String source;
    private String version;
    private String correlationId;

    public EventHeader() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.version = "1.0";
    }

    public EventHeader(String eventId, String eventType, Instant timestamp, String source, String version, String correlationId) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.source = source;
        this.version = version != null ? version : "1.0";
        this.correlationId = correlationId;
    }

    public static EventHeader of(String eventType, String source) {
        return new EventHeader(null, eventType, null, source, "1.0", null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String eventId = UUID.randomUUID().toString();
        private String eventType;
        private Instant timestamp = Instant.now();
        private String source;
        private String version = "1.0";
        private String correlationId;

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public EventHeader build() {
            return new EventHeader(eventId, eventType, timestamp, source, version, correlationId);
        }
    }
}
