package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "endowment")
public class Endowment {

    @Id
    private Integer policyNumber;

    @Column(length = 1)
    private String equities;

    @Column(length = 1)
    private String withProfits;

    @Column(length = 1)
    private String managedFund;

    @Column(length = 10)
    private String fundName;

    private Integer term;

    private Integer sumAssured;

    @Column(length = 31)
    private String lifeAssured;

    @Column(length = 32606)
    private String paddingData;

    public Integer getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(Integer policyNumber) { this.policyNumber = policyNumber; }
    public String getEquities() { return equities; }
    public void setEquities(String equities) { this.equities = equities; }
    public String getWithProfits() { return withProfits; }
    public void setWithProfits(String withProfits) { this.withProfits = withProfits; }
    public String getManagedFund() { return managedFund; }
    public void setManagedFund(String managedFund) { this.managedFund = managedFund; }
    public String getFundName() { return fundName; }
    public void setFundName(String fundName) { this.fundName = fundName; }
    public Integer getTerm() { return term; }
    public void setTerm(Integer term) { this.term = term; }
    public Integer getSumAssured() { return sumAssured; }
    public void setSumAssured(Integer sumAssured) { this.sumAssured = sumAssured; }
    public String getLifeAssured() { return lifeAssured; }
    public void setLifeAssured(String lifeAssured) { this.lifeAssured = lifeAssured; }
    public String getPaddingData() { return paddingData; }
    public void setPaddingData(String paddingData) { this.paddingData = paddingData; }
}
