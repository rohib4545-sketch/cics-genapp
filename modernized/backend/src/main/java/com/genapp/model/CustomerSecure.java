package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_secure")
public class CustomerSecure {

    @Id
    private Integer customerNumber;

    @Column(length = 32)
    private String customerPass;

    @Column(name = "state_indicator", length = 1)
    private String stateIndicator;

    @Column(name = "pass_changes")
    private Integer passChanges;

    public Integer getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(Integer customerNumber) { this.customerNumber = customerNumber; }
    public String getCustomerPass() { return customerPass; }
    public void setCustomerPass(String customerPass) { this.customerPass = customerPass; }
    public String getStateIndicator() { return stateIndicator; }
    public void setStateIndicator(String stateIndicator) { this.stateIndicator = stateIndicator; }
    public Integer getPassChanges() { return passChanges; }
    public void setPassChanges(Integer passChanges) { this.passChanges = passChanges; }
}
