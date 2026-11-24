package com.genapp.web;

import com.genapp.dto.CustomerDto;
import com.genapp.dto.PolicyResponse;
import com.genapp.service.CustomerService;
import com.genapp.service.PolicyService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final PolicyService policyService;

    public CustomerController(CustomerService customerService, PolicyService policyService) {
        this.customerService = customerService;
        this.policyService = policyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto add(@RequestBody CustomerDto dto) {
        return customerService.add(dto);
    }

    @GetMapping("/{customerNumber}")
    public CustomerDto get(@PathVariable Integer customerNumber) {
        return customerService.get(customerNumber);
    }

    @PutMapping("/{customerNumber}")
    public CustomerDto update(@PathVariable Integer customerNumber, @RequestBody CustomerDto dto) {
        return customerService.update(customerNumber, dto);
    }

    @GetMapping("/{customerNumber}/policies")
    public List<PolicyResponse> policies(@PathVariable Integer customerNumber) {
        return policyService.listForCustomer(customerNumber);
    }
}
