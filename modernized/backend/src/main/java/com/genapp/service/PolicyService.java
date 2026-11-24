package com.genapp.service;

import com.genapp.dto.CommercialDto;
import com.genapp.dto.EndowmentDto;
import com.genapp.dto.HouseDto;
import com.genapp.dto.MotorDto;
import com.genapp.dto.PolicyCommon;
import com.genapp.dto.PolicyResponse;
import com.genapp.dto.PolicyWriteRequest;
import com.genapp.model.Commercial;
import com.genapp.model.Endowment;
import com.genapp.model.House;
import com.genapp.model.Motor;
import com.genapp.model.Policy;
import com.genapp.repo.CommercialRepository;
import com.genapp.repo.CustomerRepository;
import com.genapp.repo.EndowmentRepository;
import com.genapp.repo.HouseRepository;
import com.genapp.repo.MotorRepository;
import com.genapp.repo.PolicyRepository;
import com.genapp.web.ApiException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.genapp.service.CustomerService.trunc;

/**
 * Parity with LGAPOL01/LGIPOL01/LGDPOL01/LGUPOL01 + their Db2 layers.
 *
 * Legacy rules preserved:
 * - Policy number is system-generated; lastChanged set at insert.
 * - Policy row + subtype row are atomic (legacy abend/backout).
 * - Add with an unknown customer fails with RC 70 (FK violation -530).
 * - Inquire joins POLICY with the subtype table; both customer and policy
 *   number must match when the caller supplies a customer number.
 * - Update exists only for endowment/house/motor; commercial update is an
 *   unsupported request in the original (RC 99).
 * - Delete removes the POLICY row; FK cascade removes subtype + claims.
 */
@Service
public class PolicyService {

    public enum PolicyType {
        ENDOWMENT("E"), HOUSE("H"), MOTOR("M"), COMMERCIAL("C");

        private final String code;

        PolicyType(String code) { this.code = code; }

        public String code() { return code; }

        public static PolicyType fromPath(String path) {
            for (PolicyType t : values()) {
                if (t.name().equalsIgnoreCase(path)) return t;
            }
            throw ApiException.unsupported("Unknown policy type: " + path);
        }
    }

    private final PolicyRepository policies;
    private final CustomerRepository customers;
    private final EndowmentRepository endowments;
    private final HouseRepository houses;
    private final MotorRepository motors;
    private final CommercialRepository commercials;

    public PolicyService(PolicyRepository policies, CustomerRepository customers,
                         EndowmentRepository endowments, HouseRepository houses,
                         MotorRepository motors, CommercialRepository commercials) {
        this.policies = policies;
        this.customers = customers;
        this.endowments = endowments;
        this.houses = houses;
        this.motors = motors;
        this.commercials = commercials;
    }

    @Transactional
    public PolicyResponse add(PolicyType type, PolicyWriteRequest req) {
        if (req.customerNumber() == null || !customers.existsById(req.customerNumber())) {
            throw ApiException.customerMissing("Customer does not exist: " + req.customerNumber());
        }
        Policy p = new Policy();
        applyCommon(p, type, req);
        p.setLastChanged(LocalDateTime.now());
        p = policies.save(p);

        switch (type) {
            case ENDOWMENT -> saveEndowment(p.getPolicyNumber(), req.endowment());
            case HOUSE -> saveHouse(p.getPolicyNumber(), req.house());
            case MOTOR -> saveMotor(p.getPolicyNumber(), req.motor());
            case COMMERCIAL -> saveCommercial(p.getPolicyNumber(), req.commercial());
        }
        return inquire(type, p.getPolicyNumber(), p.getCustomerNumber());
    }

    public PolicyResponse inquire(PolicyType type, Integer policyNumber, Integer customerNumber) {
        Policy p = policies.findById(policyNumber)
                .orElseThrow(() -> ApiException.notFound("Policy not found: " + policyNumber));
        if (!type.code().equals(p.getPolicyType())) {
            throw ApiException.notFound("Policy " + policyNumber + " is not of type " + type);
        }
        // Legacy join requires the customer number to match when supplied.
        if (customerNumber != null && !customerNumber.equals(p.getCustomerNumber())) {
            throw ApiException.notFound("Policy " + policyNumber + " does not belong to customer " + customerNumber);
        }
        return toResponse(p);
    }

    @Transactional
    public PolicyResponse update(PolicyType type, Integer policyNumber, PolicyWriteRequest req) {
        if (type == PolicyType.COMMERCIAL) {
            throw ApiException.unsupported("Commercial policy update is not supported (parity with legacy)");
        }
        Policy p = policies.findById(policyNumber)
                .orElseThrow(() -> ApiException.notFound("Policy not found: " + policyNumber));
        if (!type.code().equals(p.getPolicyType())) {
            throw ApiException.notFound("Policy " + policyNumber + " is not of type " + type);
        }
        applyCommon(p, type, req);
        p.setLastChanged(LocalDateTime.now());
        policies.save(p);

        switch (type) {
            case ENDOWMENT -> saveEndowment(policyNumber, req.endowment());
            case HOUSE -> saveHouse(policyNumber, req.house());
            case MOTOR -> saveMotor(policyNumber, req.motor());
            default -> { }
        }
        return inquire(type, policyNumber, null);
    }

    @Transactional
    public void delete(PolicyType type, Integer policyNumber) {
        Policy p = policies.findById(policyNumber)
                .orElseThrow(() -> ApiException.notFound("Policy not found: " + policyNumber));
        if (!type.code().equals(p.getPolicyType())) {
            throw ApiException.notFound("Policy " + policyNumber + " is not of type " + type);
        }
        policies.delete(p);
    }

    /** Commercial inquire variants: 01ICOM cust+policy, 02ICOM policy only,
     *  03ICOM customer only, 05ICOM postcode. */
    public List<PolicyResponse> commercialSearch(Integer customerNumber, Integer policyNumber, String postcode) {
        List<PolicyResponse> out = new ArrayList<>();
        if (customerNumber != null && policyNumber != null) {
            out.add(inquire(PolicyType.COMMERCIAL, policyNumber, customerNumber));
        } else if (policyNumber != null) {
            out.add(inquire(PolicyType.COMMERCIAL, policyNumber, null));
        } else if (customerNumber != null) {
            for (Policy p : policies.findByCustomerNumber(customerNumber)) {
                if (PolicyType.COMMERCIAL.code().equals(p.getPolicyType())) {
                    out.add(toResponse(p));
                }
            }
            if (out.isEmpty()) throw ApiException.notFound("No commercial policies for customer " + customerNumber);
        } else if (postcode != null) {
            for (Commercial c : commercials.findByZipcode(postcode.toUpperCase())) {
                Policy p = policies.findById(c.getPolicyNumber())
                        .orElseThrow(() -> ApiException.notFound("Policy not found: " + c.getPolicyNumber()));
                out.add(toResponse(p));
            }
            if (out.isEmpty()) throw ApiException.notFound("No commercial policies for postcode " + postcode);
        } else {
            throw new ApiException("98", org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Supply customerNumber, policyNumber or postcode");
        }
        return out;
    }

    /** Extension (not in legacy): list all policies of a customer for the UI. */
    public List<PolicyResponse> listForCustomer(Integer customerNumber) {
        if (!customers.existsById(customerNumber)) {
            throw ApiException.notFound("Customer not found: " + customerNumber);
        }
        List<PolicyResponse> out = new ArrayList<>();
        for (Policy p : policies.findByCustomerNumber(customerNumber)) {
            out.add(toResponse(p));
        }
        return out;
    }

    private void applyCommon(Policy p, PolicyType type, PolicyWriteRequest req) {
        if (req.customerNumber() != null) p.setCustomerNumber(req.customerNumber());
        p.setIssueDate(req.issueDate());
        p.setExpiryDate(req.expiryDate());
        p.setPolicyType(type.code());
        // Drivers pass brokerId=0, payment=0, brokersRef=spaces when unset.
        p.setBrokerId(req.brokerId() == null ? 0 : req.brokerId());
        p.setBrokersReference(trunc(req.brokersReference() == null ? "        " : req.brokersReference(), 10));
        p.setPayment(req.payment() == null ? 0 : req.payment());
    }

    private void saveEndowment(Integer policyNumber, EndowmentDto d) {
        Endowment e = new Endowment();
        e.setPolicyNumber(policyNumber);
        if (d != null) {
            e.setWithProfits(trunc(d.withProfits(), 1));
            e.setEquities(trunc(d.equities(), 1));
            e.setManagedFund(trunc(d.managedFund(), 1));
            e.setFundName(trunc(d.fundName(), 10));
            e.setTerm(d.term());
            e.setSumAssured(d.sumAssured());
            e.setLifeAssured(trunc(d.lifeAssured(), 31));
            e.setPaddingData(d.paddingData());
        }
        endowments.save(e);
    }

    private void saveHouse(Integer policyNumber, HouseDto d) {
        House h = new House();
        h.setPolicyNumber(policyNumber);
        if (d != null) {
            h.setPropertyType(trunc(d.propertyType(), 15));
            h.setBedrooms(d.bedrooms());
            h.setValue(d.value());
            h.setHouseName(trunc(d.houseName(), 20));
            h.setHouseNumber(trunc(d.houseNumber(), 4));
            h.setPostcode(trunc(d.postcode() == null ? null : d.postcode().toUpperCase(), 8));
        }
        houses.save(h);
    }

    private void saveMotor(Integer policyNumber, MotorDto d) {
        Motor m = new Motor();
        m.setPolicyNumber(policyNumber);
        if (d != null) {
            m.setMake(trunc(d.make(), 15));
            m.setModel(trunc(d.model(), 15));
            m.setValue(d.value());
            m.setRegNumber(trunc(d.regNumber(), 7));
            m.setColour(trunc(d.colour(), 8));
            m.setCc(d.cc());
            m.setYearOfManufacture(d.yearOfManufacture());
            m.setPremium(d.premium());
            m.setAccidents(d.accidents());
        }
        motors.save(m);
    }

    private void saveCommercial(Integer policyNumber, CommercialDto d) {
        Commercial c = new Commercial();
        c.setPolicyNumber(policyNumber);
        if (d != null) {
            c.setRequestDate(d.requestDate() == null ? LocalDateTime.now() : d.requestDate());
            c.setStartDate(d.startDate());
            c.setRenewalDate(d.renewalDate());
            c.setAddress(trunc(d.address(), 255));
            c.setZipcode(trunc(d.zipcode() == null ? null : d.zipcode().toUpperCase(), 8));
            c.setLatitudeN(trunc(d.latitudeN(), 11));
            c.setLongitudeW(trunc(d.longitudeW(), 11));
            c.setCustomer(trunc(d.customer(), 255));
            c.setPropertyType(trunc(d.propertyType(), 255));
            c.setFirePeril(d.firePeril());
            c.setFirePremium(d.firePremium());
            c.setCrimePeril(d.crimePeril());
            c.setCrimePremium(d.crimePremium());
            c.setFloodPeril(d.floodPeril());
            c.setFloodPremium(d.floodPremium());
            c.setWeatherPeril(d.weatherPeril());
            c.setWeatherPremium(d.weatherPremium());
            c.setStatus(d.status());
            c.setRejectionReason(trunc(d.rejectionReason(), 255));
        }
        commercials.save(c);
    }

    private PolicyResponse toResponse(Policy p) {
        PolicyCommon common = new PolicyCommon(
                p.getPolicyNumber(), p.getCustomerNumber(), p.getIssueDate(), p.getExpiryDate(),
                p.getLastChanged(), p.getBrokerId(), p.getBrokersReference(), p.getPayment(),
                p.getPolicyType());
        EndowmentDto end = null;
        HouseDto hou = null;
        MotorDto mot = null;
        CommercialDto com = null;
        switch (p.getPolicyType()) {
            case "E" -> {
                Endowment e = endowments.findById(p.getPolicyNumber())
                        .orElseThrow(() -> ApiException.notFound("Endowment details missing for policy " + p.getPolicyNumber()));
                end = new EndowmentDto(e.getWithProfits(), e.getEquities(), e.getManagedFund(),
                        e.getFundName(), e.getTerm(), e.getSumAssured(), e.getLifeAssured(), e.getPaddingData());
            }
            case "H" -> {
                House h = houses.findById(p.getPolicyNumber())
                        .orElseThrow(() -> ApiException.notFound("House details missing for policy " + p.getPolicyNumber()));
                hou = new HouseDto(h.getPropertyType(), h.getBedrooms(), h.getValue(),
                        h.getHouseName(), h.getHouseNumber(), h.getPostcode());
            }
            case "M" -> {
                Motor m = motors.findById(p.getPolicyNumber())
                        .orElseThrow(() -> ApiException.notFound("Motor details missing for policy " + p.getPolicyNumber()));
                mot = new MotorDto(m.getMake(), m.getModel(), m.getValue(), m.getRegNumber(),
                        m.getColour(), m.getCc(), m.getYearOfManufacture(), m.getPremium(), m.getAccidents());
            }
            case "C" -> {
                Commercial c = commercials.findById(p.getPolicyNumber())
                        .orElseThrow(() -> ApiException.notFound("Commercial details missing for policy " + p.getPolicyNumber()));
                com = new CommercialDto(c.getRequestDate(), c.getStartDate(), c.getRenewalDate(),
                        c.getAddress(), c.getZipcode(), c.getLatitudeN(), c.getLongitudeW(), c.getCustomer(),
                        c.getPropertyType(), c.getFirePeril(), c.getFirePremium(), c.getCrimePeril(),
                        c.getCrimePremium(), c.getFloodPeril(), c.getFloodPremium(), c.getWeatherPeril(),
                        c.getWeatherPremium(), c.getStatus(), c.getRejectionReason());
            }
            default -> throw ApiException.unsupported("Unknown policy type code: " + p.getPolicyType());
        }
        return new PolicyResponse(common, end, hou, mot, com);
    }
}
