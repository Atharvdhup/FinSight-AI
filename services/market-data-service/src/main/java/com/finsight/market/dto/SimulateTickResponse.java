package com.finsight.market.dto;

import com.finsight.common.dto.StockQuoteDTO;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SimulateTickResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Instant timestamp;
    private int updatedCount;
    private List<StockQuoteDTO> updatedQuotes = new ArrayList<>();

    public SimulateTickResponse() {
        this.timestamp = Instant.now();
    }

    public SimulateTickResponse(Instant timestamp, int updatedCount, List<StockQuoteDTO> updatedQuotes) {
        this.timestamp = timestamp;
        this.updatedCount = updatedCount;
        this.updatedQuotes = updatedQuotes != null ? updatedQuotes : new ArrayList<>();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
    }

    public List<StockQuoteDTO> getUpdatedQuotes() {
        return updatedQuotes;
    }

    public void setUpdatedQuotes(List<StockQuoteDTO> updatedQuotes) {
        this.updatedQuotes = updatedQuotes != null ? updatedQuotes : new ArrayList<>();
    }
}

