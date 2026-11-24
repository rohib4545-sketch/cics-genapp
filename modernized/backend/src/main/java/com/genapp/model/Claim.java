package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "claim")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer claimNumber;

    @Column(nullable = false)
    private Integer policyNumber;

    private LocalDate claimDate;

    private Integer paid;

    @Column(name = "value")
    private Integer value;

    @Column(length = 255)
    private String cause;

    @Column(length = 255)
    private String observations;

    public Integer getClaimNumber() { return claimNumber; }
    public void setClaimNumber(Integer claimNumber) { this.claimNumber = claimNumber; }
    public Integer getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(Integer policyNumber) { this.policyNumber = policyNumber; }
    public LocalDate getClaimDate() { return claimDate; }
    public void setClaimDate(LocalDate claimDate) { this.claimDate = claimDate; }
    public Integer getPaid() { return paid; }
    public void setPaid(Integer paid) { this.paid = paid; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    public String getCause() { return cause; }
    public void setCause(String cause) { this.cause = cause; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}
