package com.niic.erp.production;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "challan_number_counters")
public class ChallanNumberCounter {

    @Id
    @Column(name = "challan_year")
    private Integer year;

    @Column(nullable = false)
    private Integer lastNumber;

    protected ChallanNumberCounter() {
    }

    public ChallanNumberCounter(Integer year, Integer lastNumber) {
        this.year = year;
        this.lastNumber = lastNumber;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getLastNumber() {
        return lastNumber;
    }

    public void setLastNumber(Integer lastNumber) {
        this.lastNumber = lastNumber;
    }
}
