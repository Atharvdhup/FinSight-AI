package com.finsight.common.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Standard enterprise event metadata header attached to all Kafka domain events.
 * Enables distributed tracing, correlation, idempotent processing, and schema evolution.
 */
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

    // Explicit Getters and Setters (Guarantees IDE compatibility without requiring Lombok extension)
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventHeader that = (EventHeader) o;
        return Objects.equals(eventId, that.eventId) &&
               Objects.equals(eventType, that.eventType) &&
               Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, eventType, source);
    }

    @Override
    public String toString() {
        return "EventHeader{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                ", version='" + version + '\'' +
                ", correlationId='" + correlationId + '\'' +
                '}';
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
