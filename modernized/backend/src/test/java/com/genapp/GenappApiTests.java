package com.genapp;

import com.genapp.model.CustomerSecure;
import com.genapp.repo.CustomerSecureRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GenappApiTests {

    @Autowired MockMvc mvc;
    @Autowired CustomerSecureRepository secureRepo;
    private final ObjectMapper om = new ObjectMapper();

    private JsonNode addCustomer(String body) throws Exception {
        MvcResult r = mvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return om.readTree(r.getResponse().getContentAsString());
    }

    private JsonNode addMotorPolicy(int customerNumber) throws Exception {
        String body = """
            {
              "customerNumber": %d,
              "issueDate": "2026-01-01",
              "expiryDate": "2027-01-01",
              "motor": {
                "make": "Tesla", "model": "Model 3", "value": 42000,
                "regNumber": "AB26CDE", "colour": "Red", "cc": 2000,
                "yearOfManufacture": "2024-05-01", "premium": 1200, "accidents": 0
              }
            }
            """.formatted(customerNumber);
        MvcResult r = mvc.perform(post("/api/policies/motor")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return om.readTree(r.getResponse().getContentAsString());
    }

    @Test
    void addCustomerReturnsGeneratedNumberAndSecurityRow() throws Exception {
        JsonNode c = addCustomer("""
            {"firstName":"Andrew","lastName":"Pandy","dateOfBirth":"1950-07-11",
             "houseNumber":"34","postcode":"pi101oo","phoneHome":"01962 811234",
             "phoneMobile":"07799 123456","emailAddress":"A.Pandy@beebhouse.com"}
            """);
        int num = c.get("customerNumber").asInt();
        assertTrue(num >= 1000001, "identity should start at 1000001 like Db2");
        assertEquals("PI101OO", c.get("postcode").asText(), "postcode must be upper-cased");
        CustomerSecure sec = secureRepo.findById(num).orElseThrow();
        assertEquals("5732fec825535eeafb8fac50fee3a8aa", sec.getCustomerPass());
        assertEquals("N", sec.getStateIndicator());
        assertEquals(0, sec.getPassChanges());
    }

    @Test
    void blankCustomerIsLegal() throws Exception {
        JsonNode c = addCustomer("{}");
        assertTrue(c.get("customerNumber").asInt() >= 1000001);
    }

    @Test
    void getMissingCustomerReturnsLegacyRc01() throws Exception {
        mvc.perform(get("/api/customers/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.returnCode").value("01"));
    }

    @Test
    void updateCustomerWorks() throws Exception {
        int num = addCustomer("{\"firstName\":\"Scott\",\"lastName\":\"Tracey\"}")
                .get("customerNumber").asInt();
        mvc.perform(put("/api/customers/" + num)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Scott\",\"lastName\":\"Tracey-Updated\",\"postcode\":\"tb14tv\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Tracey-Updated"))
                .andExpect(jsonPath("$.postcode").value("TB14TV"));
    }

    @Test
    void addInquireUpdateDeleteMotorPolicy() throws Exception {
        int cust = addCustomer("{\"firstName\":\"John\"}").get("customerNumber").asInt();
        JsonNode p = addMotorPolicy(cust);
        int policyNum = p.at("/common/policyNumber").asInt();
        assertEquals("M", p.at("/common/policyType").asText());
        assertEquals("Tesla", p.at("/motor/make").asText());
        assertTrue(p.at("/common/lastChanged").asText().length() > 0);

        mvc.perform(get("/api/policies/motor/" + policyNum + "?customerNumber=" + cust))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motor.model").value("Model 3"));

        mvc.perform(put("/api/policies/motor/" + policyNum)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"customerNumber": %d, "issueDate": "2026-01-01", "expiryDate": "2028-01-01",
                             "motor": {"make": "Tesla", "model": "Model Y", "value": 45000,
                                       "regNumber": "AB26CDE", "colour": "Blue", "cc": 2000,
                                       "yearOfManufacture": "2024-05-01", "premium": 1300, "accidents": 1}}
                            """.formatted(cust)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motor.model").value("Model Y"));

        mvc.perform(delete("/api/policies/motor/" + policyNum))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/policies/motor/" + policyNum))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.returnCode").value("01"));
    }

    @Test
    void addPolicyWithUnknownCustomerReturnsRc70() throws Exception {
        mvc.perform(post("/api/policies/motor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerNumber\": 999999999, \"motor\": {\"make\": \"X\"}}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.returnCode").value("70"));
    }

    @Test
    void inquireWithMismatchedCustomerIsNotFound() throws Exception {
        int cust = addCustomer("{\"firstName\":\"Louis\"}").get("customerNumber").asInt();
        int other = addCustomer("{\"firstName\":\"Graham\"}").get("customerNumber").asInt();
        int policyNum = addMotorPolicy(cust).at("/common/policyNumber").asInt();
        mvc.perform(get("/api/policies/motor/" + policyNum + "?customerNumber=" + other))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.returnCode").value("01"));
    }

    @Test
    void commercialUpdateIsUnsupportedRc99() throws Exception {
        int cust = addCustomer("{\"firstName\":\"Biz\"}").get("customerNumber").asInt();
        String addBody = """
            {"customerNumber": %d, "issueDate": "2026-02-01", "expiryDate": "2027-02-01",
             "commercial": {"address": "1 Industrial Way", "zipcode": "so212jn",
                            "propertyType": "Warehouse", "firePeril": 2, "firePremium": 5000, "status": 1}}
            """.formatted(cust);
        MvcResult r = mvc.perform(post("/api/policies/commercial")
                        .contentType(MediaType.APPLICATION_JSON).content(addBody))
                .andExpect(status().isCreated())
                .andReturn();
        int policyNum = om.readTree(r.getResponse().getContentAsString()).at("/common/policyNumber").asInt();

        mvc.perform(put("/api/policies/commercial/" + policyNum)
                        .contentType(MediaType.APPLICATION_JSON).content(addBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.returnCode").value("99"));

        // zipcode stored upper-cased; search by postcode finds it
        mvc.perform(get("/api/policies/commercial/search?postcode=SO212JN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].commercial.zipcode").value("SO212JN"));

        // variant: by customer only
        mvc.perform(get("/api/policies/commercial/search?customerNumber=" + cust))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].common.policyNumber").value(policyNum));

        // variant: by policy only
        mvc.perform(get("/api/policies/commercial/search?policyNumber=" + policyNum))
                .andExpect(status().isOk());
    }

    @Test
    void wrongTypeInquireIsNotFound() throws Exception {
        int cust = addCustomer("{\"firstName\":\"X\"}").get("customerNumber").asInt();
        int policyNum = addMotorPolicy(cust).at("/common/policyNumber").asInt();
        mvc.perform(get("/api/policies/house/" + policyNum))
                .andExpect(status().isNotFound());
    }

    @Test
    void listCustomerPolicies() throws Exception {
        int cust = addCustomer("{\"firstName\":\"Multi\"}").get("customerNumber").asInt();
        addMotorPolicy(cust);
        mvc.perform(get("/api/customers/" + cust + "/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void healthEndpoint() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
