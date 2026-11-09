package com.finsight.portfolio.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class SectorAllocationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sector;
    private BigDecimal value;
    private BigDecimal weightPercent;

    public SectorAllocationDTO() {
    }

    public SectorAllocationDTO(String sector, BigDecimal value, BigDecimal weightPercent) {
        this.sector = sector;
        this.value = value;
        this.weightPercent = weightPercent;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getWeightPercent() {
        return weightPercent;
    }

    public void setWeightPercent(BigDecimal weightPercent) {
        this.weightPercent = weightPercent;
    }
}

