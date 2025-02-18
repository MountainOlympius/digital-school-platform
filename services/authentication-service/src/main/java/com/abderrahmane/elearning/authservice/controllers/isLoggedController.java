package com.abderrahmane.elearning.authservice.controllers;

import java.util.HashMap;
import java.util.Map;

import com.abderrahmane.elearning.common.converters.MapAccountConverter;
import com.abderrahmane.elearning.common.models.Account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/${api.version}/isLoggedIn")
public class isLoggedController {
    @Autowired
    private MapAccountConverter accountMapConverter;

    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
    public Map<String, Object> handleGetRequest (@RequestAttribute(name = "account", required = false) Account account) {
        Map<String, Object> response = new HashMap<>();

        if (account == null) {
            response.put("isLoggedIn", false);
            return response;
        }

        response.put("isLoggedIn", true);
        response.put("data", accountMapConverter.convert(account));
        return response;
    }
}
