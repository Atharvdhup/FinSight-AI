package com.finsight.common.event;

import java.io.Serializable;
import java.util.Objects;

/**
 * Generic event envelope for all asynchronous Kafka events in FinSight AI.
 * Guarantees standard event structure across microservices.
 *
 * @param <T> Payload domain type
 */
public class BaseEvent<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private EventHeader header;
    private T data;

    public BaseEvent() {
    }

    public BaseEvent(EventHeader header, T data) {
        this.header = header;
        this.data = data;
    }

    public EventHeader getHeader() {
        return header;
    }

    public void setHeader(EventHeader header) {
        this.header = header;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getEventId() {
        return header != null ? header.getEventId() : null;
    }

    public String getEventType() {
        return header != null ? header.getEventType() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEvent<?> baseEvent = (BaseEvent<?>) o;
        return Objects.equals(header, baseEvent.header) && Objects.equals(data, baseEvent.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, data);
    }

    @Override
    public String toString() {
        return "BaseEvent{" +
                "header=" + header +
                ", data=" + data +
                '}';
    }
}
