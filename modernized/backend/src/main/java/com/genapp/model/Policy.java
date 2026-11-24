package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer policyNumber;

    @Column(nullable = false)
    private Integer customerNumber;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Column(length = 1)
    private String policyType;

    private LocalDateTime lastChanged;

    private Integer brokerId;

    @Column(length = 10)
    private String brokersReference;

    private Integer payment;

    private Integer commission;

    public Integer getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(Integer policyNumber) { this.policyNumber = policyNumber; }
    public Integer getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(Integer customerNumber) { this.customerNumber = customerNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }
    public LocalDateTime getLastChanged() { return lastChanged; }
    public void setLastChanged(LocalDateTime lastChanged) { this.lastChanged = lastChanged; }
    public Integer getBrokerId() { return brokerId; }
    public void setBrokerId(Integer brokerId) { this.brokerId = brokerId; }
    public String getBrokersReference() { return brokersReference; }
    public void setBrokersReference(String brokersReference) { this.brokersReference = brokersReference; }
    public Integer getPayment() { return payment; }
    public void setPayment(Integer payment) { this.payment = payment; }
    public Integer getCommission() { return commission; }
    public void setCommission(Integer commission) { this.commission = commission; }
}
