package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "motor")
public class Motor {

    @Id
    private Integer policyNumber;

    @Column(length = 15)
    private String make;

    @Column(length = 15)
    private String model;

    @Column(name = "value")
    private Integer value;

    @Column(length = 7)
    private String regNumber;

    @Column(length = 8)
    private String colour;

    private Integer cc;

    private LocalDate yearOfManufacture;

    private Integer premium;

    private Integer accidents;

    public Integer getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(Integer policyNumber) { this.policyNumber = policyNumber; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }
    public Integer getCc() { return cc; }
    public void setCc(Integer cc) { this.cc = cc; }
    public LocalDate getYearOfManufacture() { return yearOfManufacture; }
    public void setYearOfManufacture(LocalDate yearOfManufacture) { this.yearOfManufacture = yearOfManufacture; }
    public Integer getPremium() { return premium; }
    public void setPremium(Integer premium) { this.premium = premium; }
    public Integer getAccidents() { return accidents; }
    public void setAccidents(Integer accidents) { this.accidents = accidents; }
}
