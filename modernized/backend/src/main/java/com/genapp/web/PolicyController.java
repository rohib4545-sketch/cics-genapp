package com.genapp.web;

import com.genapp.dto.PolicyResponse;
import com.genapp.dto.PolicyWriteRequest;
import com.genapp.service.PolicyService;
import com.genapp.service.PolicyService.PolicyType;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping("/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyResponse add(@PathVariable String type, @RequestBody PolicyWriteRequest req) {
        return policyService.add(PolicyType.fromPath(type), req);
    }

    @GetMapping("/{type}/{policyNumber}")
    public PolicyResponse inquire(@PathVariable String type,
                                  @PathVariable Integer policyNumber,
                                  @RequestParam(required = false) Integer customerNumber) {
        return policyService.inquire(PolicyType.fromPath(type), policyNumber, customerNumber);
    }

    @PutMapping("/{type}/{policyNumber}")
    public PolicyResponse update(@PathVariable String type,
                                 @PathVariable Integer policyNumber,
                                 @RequestBody PolicyWriteRequest req) {
        return policyService.update(PolicyType.fromPath(type), policyNumber, req);
    }

    @DeleteMapping("/{type}/{policyNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String type, @PathVariable Integer policyNumber) {
        policyService.delete(PolicyType.fromPath(type), policyNumber);
    }

    @GetMapping("/commercial/search")
    public List<PolicyResponse> commercialSearch(@RequestParam(required = false) Integer customerNumber,
                                                 @RequestParam(required = false) Integer policyNumber,
                                                 @RequestParam(required = false) String postcode) {
        return policyService.commercialSearch(customerNumber, policyNumber, postcode);
    }
}
