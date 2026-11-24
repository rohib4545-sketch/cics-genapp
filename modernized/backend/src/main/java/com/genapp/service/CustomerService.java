package com.genapp.service;

import com.genapp.dto.CustomerDto;
import com.genapp.model.Customer;
import com.genapp.model.CustomerSecure;
import com.genapp.repo.CustomerRepository;
import com.genapp.repo.CustomerSecureRepository;
import com.genapp.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Parity with LGACUS01/LGICUS01/LGUCUS01 + LGACDB01/02, LGICDB01, LGUCDB01.
 *
 * Legacy rules preserved:
 * - No required-field validation on add/update (blank customers are legal).
 * - Postcode is upper-cased by the driver before the add/update call.
 * - Over-long strings are truncated to the Db2 CHAR width.
 * - Every new customer also gets a customer_secure row with the default
 *   password checksum, state 'N' and pass_changes 0.
 */
@Service
public class CustomerService {

    /** Default BD5 password checksum inserted by LGACDB02. */
    static final String DEFAULT_PASSWORD = "5732fec825535eeafb8fac50fee3a8aa";

    private final CustomerRepository customers;
    private final CustomerSecureRepository secure;

    public CustomerService(CustomerRepository customers, CustomerSecureRepository secure) {
        this.customers = customers;
        this.secure = secure;
    }

    @Transactional
    public CustomerDto add(CustomerDto dto) {
        Customer c = new Customer();
        apply(c, dto);
        c = customers.save(c);

        CustomerSecure sec = new CustomerSecure();
        sec.setCustomerNumber(c.getCustomerNumber());
        sec.setCustomerPass(DEFAULT_PASSWORD);
        sec.setStateIndicator("N");
        sec.setPassChanges(0);
        secure.save(sec);

        return toDto(c);
    }

    public CustomerDto get(Integer customerNumber) {
        Customer c = customers.findById(customerNumber)
                .orElseThrow(() -> ApiException.notFound("Customer not found: " + customerNumber));
        return toDto(c);
    }

    @Transactional
    public CustomerDto update(Integer customerNumber, CustomerDto dto) {
        Customer c = customers.findById(customerNumber)
                .orElseThrow(() -> ApiException.notFound("Customer not found: " + customerNumber));
        apply(c, dto);
        return toDto(customers.save(c));
    }

    private static void apply(Customer c, CustomerDto dto) {
        c.setFirstName(trunc(dto.firstName(), 10));
        c.setLastName(trunc(dto.lastName(), 20));
        c.setDateOfBirth(dto.dateOfBirth());
        c.setHouseName(trunc(dto.houseName(), 20));
        c.setHouseNumber(trunc(dto.houseNumber(), 4));
        c.setPostcode(upper(trunc(dto.postcode(), 8)));
        c.setPhoneMobile(trunc(dto.phoneMobile(), 20));
        c.setPhoneHome(trunc(dto.phoneHome(), 20));
        c.setEmailAddress(trunc(dto.emailAddress(), 100));
    }

    static String trunc(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    static String upper(String s) {
        return s == null ? null : s.toUpperCase();
    }

    static CustomerDto toDto(Customer c) {
        return new CustomerDto(
                c.getCustomerNumber(), c.getFirstName(), c.getLastName(), c.getDateOfBirth(),
                c.getHouseName(), c.getHouseNumber(), c.getPostcode(),
                c.getPhoneMobile(), c.getPhoneHome(), c.getEmailAddress());
    }
}
