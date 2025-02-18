package com.abderrahmane.elearning.socialservice.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.abderrahmane.elearning.common.converters.MapSchoolProfileConverter;
import com.abderrahmane.elearning.common.converters.MapStudentProfileConverter;
import com.abderrahmane.elearning.common.converters.StringDateConverter;
import com.abderrahmane.elearning.common.converters.StringDateTimeConverter;
import com.abderrahmane.elearning.common.models.Account;
import com.abderrahmane.elearning.common.models.RequestForConnection;
import com.abderrahmane.elearning.common.models.SchoolTeacher;
import com.abderrahmane.elearning.common.utils.ErrorMessageResolver;
import com.abderrahmane.elearning.common.repositories.ProfileDAO;
import com.abderrahmane.elearning.common.repositories.TeacherDAO;
import com.abderrahmane.elearning.socialservice.validators.JoinTeacherFormValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/${api.version}/teacher")
public class TeacherController {
    @Autowired
    private ProfileDAO profileDAO;

    @Autowired
    private TeacherDAO teacherDAO;

    @Autowired
    private StringDateConverter stringDateConverter;

    @Autowired
    private MapSchoolProfileConverter schoolProfileConverter;

    @Autowired
    private JoinTeacherFormValidator joinTeacherFormValidator;

    @Autowired
    private MapStudentProfileConverter studentProfileConverter;

    @Autowired
    private ErrorMessageResolver messageResolver;

    @Autowired
    private StringDateTimeConverter dateTimeConverter;

    @GetMapping(path = "/schools")
    public Map<String, Object> getTeacherSchoolList (@RequestAttribute(name = "account") Account account) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("success", true);

        List<Map<String, Object>> schools = teacherDAO.getTeacherSchools(account.getId()).stream().map(school -> {
           Map<String, Object> schoolTeacherMap = new HashMap<>();

           schoolTeacherMap.put("verified", school.isVerified());
           schoolTeacherMap.put("title", school.getTitle());
           schoolTeacherMap.put("addedDate", stringDateConverter.convert(school.getCreatedDate()));
           schoolTeacherMap.put("endedDate", school.getEndedDate() != null ? stringDateConverter.convert(school.getEndedDate()) : null);
           schoolTeacherMap.put("school", schoolProfileConverter.convert(school.getSchool()));

           return schoolTeacherMap;
        }).toList();

        response.put("data", schools);

        return response;
    }

    @PostMapping("/join-school")
    public Map<String, Object> joinSchool (@RequestAttribute("account") Account account, @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        MapBindingResult errors = new MapBindingResult(body, "joinSchool");

        joinTeacherFormValidator.validate(body, errors);

        if (errors.hasErrors()) return messageResolver.constructErrorResponse(errors);

        // Check if the teacher is current in a school
        List<SchoolTeacher> schools = account.getTeacherProfile().getSchooles().stream().filter(schoolTeacher -> schoolTeacher.getEndedDate() == null).toList();
        Optional<SchoolTeacher> endedSchool = account.getTeacherProfile().getSchooles().stream().filter(schoolTeacher -> schoolTeacher.getEndedDate() != null && schoolTeacher.getSchoolId().equals((String)body.get("id"))).findFirst();

        if (schools.size() > 0) {
            response.put("success", false);
            response.put("errors", List.of("teacher_already_in_school"));
            return response;
        }

        if (endedSchool.isPresent()){
            boolean joined = profileDAO.teacherRejoinSchool(account.getTeacherProfile().getId(), (String)body.get("id"), (String)body.get("title"));
            response.put("success", joined);
            return response;
        }

        // FIXME : Error could be handled globaly
        // TODO : Send notification to school account
        try {
            boolean created = profileDAO.teacherJoinSchool(account.getId(), (String)body.get("id"), (String)body.get("title"));
            response.put("success", created);
        } catch (DataIntegrityViolationException ex) {      
            response.put("success", false);
            response.put("errors", List.of(this.translateException(ex)));
        } catch (Exception ex) {
            System.out.print("[" + ex.getClass().getName() + "] ");
            System.out.println(ex.getMessage());
            response.put("success", false);
            response.put("errors", List.of("unknown_error")); 
        }

        return response;
    }

    @DeleteMapping(path = "/leave-school", params = "id")
    public Map<String, Object> leaveSchool (@RequestAttribute("account") Account account, @RequestParam(name = "id") String schoolId) {
        Map<String, Object> response = new HashMap<>();

        response.put("success", true);

        List<SchoolTeacher> schools = account.getTeacherProfile().getSchooles().stream().filter(schoolTeacher -> {
            return schoolTeacher.getEndedDate() == null && schoolTeacher.getSchool().getId().equals(schoolId);
        }).toList();

        if (schools.size() <= 0) {
            response.put("success", false);
            response.put("errors", List.of("no_school_joined"));
            return response;
        }

        SchoolTeacher schoolTeacher = schools.get(0);

        if (!schoolTeacher.isVerified()) {
            boolean deleted = profileDAO.deleteTeacherSchool(account.getTeacherProfile().getId(), schoolId);
            response.put("success", deleted);
        } else {
            boolean ended = profileDAO.endTeacherSchool(account.getTeacherProfile().getId(), schoolId);
            response.put("success", ended);
        }

        return response;
    }

    @GetMapping(path = "/requests")
    public Map<String, Object> getRequestsForConnections (@RequestAttribute("account") Account account) {
        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("data", account.getTeacherProfile().getRequests().stream().map(request -> {
            Map<String, Object> requestObject = studentProfileConverter.convert(request.getStudentProfile());
            requestObject.put("accountId", request.getStudentProfile().getId());
            requestObject.put("id", request.getId());
            requestObject.put("createdDate", dateTimeConverter.convert(request.getCreatedDate()));

            return requestObject;
        }).toList());

        return response;
    }

    // TODO : May send notification to the student
    @PostMapping(path = "/requests/accept")
    public Map<String, Object> acceptConnectionRequest (@RequestAttribute("account") Account account, @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        RequestForConnection requestForConnection = null;

        response.put("success", true);

        if (!body.containsKey("id") || !body.get("id").getClass().equals(String.class) || ((String)body.get("id")).length() <= 0) {
            response.put("success", false);
            response.put("errors", List.of("Id field is required"));
            return response;
        }

        requestForConnection = profileDAO.getRequestForConnection((String)body.get("id"), account.getId());

        if (requestForConnection == null) {
            response.put("success", false);
            response.put("errors", List.of("not_found"));
        }

        if (!profileDAO.deleteRequestForConnection((String)body.get("id"))) {
            response.put("success", false);
            return response;
        }
        
        try {
            profileDAO.createTeacherStudentConnection(account.getId(), requestForConnection.getStudentProfile().getId());
        } catch (DataIntegrityViolationException ex) {
            response.put("success", false);
            response.put("errors", List.of(this.translateException(ex)));
        }
        
        return response;
    }

    @PostMapping(path = "/requests/reject")
    public Map<String, Object> rejectRequestForConnection (@RequestAttribute("account") Account account, @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        boolean deleted = false;

        if (!body.containsKey("id") || !body.get("id").getClass().equals(String.class) || ((String)body.get("id")).length() <= 0) {
            response.put("success", false);
            response.put("errors", List.of("Id field is required"));
            return response;
        }

        deleted = profileDAO.deleteRequestForConnection((String)body.get("id"), account.getId());
        response.put("success", deleted);

        if (!deleted) response.put("errors", List.of("not_found"));

        return response;
    }

    private String translateException (DataIntegrityViolationException ex) {
        String message = ex.getMessage();

        if (message.contains("teacher_school_pkey")) {
            return "school_already_joined";
        } else if (message.contains("teacher_school_school_id_fkey")) {
            return "school_does_not_exist";
        } else if (message.contains("connection_pkey")) {
            return "connection_already_exists";
        }

        System.out.println("[UNKOWN-ERROR] " + ex.getMessage());

        return "unknown_error";
    }
}
