package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commercial")
public class Commercial {

    @Id
    private Integer policyNumber;

    private LocalDateTime requestDate;

    private LocalDate startDate;

    private LocalDate renewalDate;

    @Column(length = 255)
    private String address;

    @Column(length = 8)
    private String zipcode;

    @Column(length = 11)
    private String latitudeN;

    @Column(length = 11)
    private String longitudeW;

    @Column(length = 255)
    private String customer;

    @Column(length = 255)
    private String propertyType;

    private Integer firePeril;
    private Integer firePremium;
    private Integer crimePeril;
    private Integer crimePremium;
    private Integer floodPeril;
    private Integer floodPremium;
    private Integer weatherPeril;
    private Integer weatherPremium;
    private Integer status;

    @Column(length = 255)
    private String rejectionReason;

    public Integer getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(Integer policyNumber) { this.policyNumber = policyNumber; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getRenewalDate() { return renewalDate; }
    public void setRenewalDate(LocalDate renewalDate) { this.renewalDate = renewalDate; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }
    public String getLatitudeN() { return latitudeN; }
    public void setLatitudeN(String latitudeN) { this.latitudeN = latitudeN; }
    public String getLongitudeW() { return longitudeW; }
    public void setLongitudeW(String longitudeW) { this.longitudeW = longitudeW; }
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    public Integer getFirePeril() { return firePeril; }
    public void setFirePeril(Integer firePeril) { this.firePeril = firePeril; }
    public Integer getFirePremium() { return firePremium; }
    public void setFirePremium(Integer firePremium) { this.firePremium = firePremium; }
    public Integer getCrimePeril() { return crimePeril; }
    public void setCrimePeril(Integer crimePeril) { this.crimePeril = crimePeril; }
    public Integer getCrimePremium() { return crimePremium; }
    public void setCrimePremium(Integer crimePremium) { this.crimePremium = crimePremium; }
    public Integer getFloodPeril() { return floodPeril; }
    public void setFloodPeril(Integer floodPeril) { this.floodPeril = floodPeril; }
    public Integer getFloodPremium() { return floodPremium; }
    public void setFloodPremium(Integer floodPremium) { this.floodPremium = floodPremium; }
    public Integer getWeatherPeril() { return weatherPeril; }
    public void setWeatherPeril(Integer weatherPeril) { this.weatherPeril = weatherPeril; }
    public Integer getWeatherPremium() { return weatherPremium; }
    public void setWeatherPremium(Integer weatherPremium) { this.weatherPremium = weatherPremium; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
