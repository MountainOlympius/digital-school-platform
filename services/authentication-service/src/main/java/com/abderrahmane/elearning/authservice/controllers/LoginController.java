package com.abderrahmane.elearning.authservice.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.abderrahmane.elearning.common.converters.MapAccountConverter;
import com.abderrahmane.elearning.common.helpers.PasswordEncoder;
import com.abderrahmane.elearning.common.models.Account;
import com.abderrahmane.elearning.common.models.Session;
import com.abderrahmane.elearning.common.repositories.AccountDAO;
import com.abderrahmane.elearning.common.repositories.SessionDAO;
import com.abderrahmane.elearning.common.utils.ErrorMessageResolver;
import com.abderrahmane.elearning.authservice.validators.LoginFormValidator;

import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/${api.version}/login")
public class LoginController {
    @Autowired
    private LoginFormValidator validator;

    @Autowired
    private ErrorMessageResolver resolver;

    @Autowired
    private AccountDAO accountRepository;

    @Autowired
    private SessionDAO sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MapAccountConverter accountMapConverter;

    @Value("${session.key:sid}")
    private String sessionKey;

    @Value("${session.SameSite:Lax}")
    private String sameSiteAttribute;

    @PostMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
    public Map<String, Object> handlePostRequest(@RequestBody Map<String, Object> form, HttpServletResponse httpResponse) {
        Map<String, Object> response = new HashMap<>();
        MapBindingResult errors = new MapBindingResult(form, "login");
        validator.validate(form, errors);

        if (errors.hasErrors())
            return resolver.constructErrorResponse(errors);

        Account account = this.authenticate((String) form.get("username"), (String) form.get("password"));

        if (account == null) {
            response.put("success", false);
            response.put("errors", List.of("Username or password is incorrect"));
        } else if (!account.isActive()) {
            response.put("success", false);
            response.put("errors", List.of("email_unverified"));
        } else {
            response.put("success", true);
            response.put("data", accountMapConverter.convert(account));
            saveSession(httpResponse, account);
        }

        return response;
    }

    public Account authenticate(String username, String password) {
        Account account = accountRepository.selectByUsername(username);

        return account == null || !passwordEncoder.check(account.getPassword(), password) ? null : account;
    }

    // TODO : specify root domain to be the domain of the app
    public void saveSession (HttpServletResponse response, Account account) {
        Map<String, Object> sessionPayload = new HashMap<>();
        Session session;

        sessionPayload.put("account_id", account.getId());
        session = sessionRepository.save(sessionPayload);

        if (session != null) {
            ResponseCookie cookie = ResponseCookie.from(sessionKey, session.getSid())
                .path("/")
                .sameSite(sameSiteAttribute)
                .maxAge(session.getMaxAge())
                .build();
            
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
    }
}
