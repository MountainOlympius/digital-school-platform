package com.abderrahmane.elearning.socialservice.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.abderrahmane.elearning.common.converters.MapStudentProfileConverter;
import com.abderrahmane.elearning.common.converters.StringDateTimeConverter;
import com.abderrahmane.elearning.common.models.Account;
import com.abderrahmane.elearning.common.models.StudentTeacherConnection;
import com.abderrahmane.elearning.common.repositories.ProfileDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/${api.version}/teacher/connections")
public class TeacherConnections {
    private final int itemsPerPage = 10;
    
    @Autowired
    private ProfileDAO profileDAO;

    @Autowired
    private MapStudentProfileConverter studentProfileConverter;

    @Autowired
    private StringDateTimeConverter dateTimeConverter;
    

    // May wanna send a timestamp instead of formated date

    @GetMapping
    public Map<String, Object> getTeacherConnections (@RequestAttribute("account") Account account, @RequestParam(name = "page", defaultValue = "1", required = false) int page) {
        Map<String, Object> response = new HashMap<>();
        int offset = 0;

        page = page <= 0 ? 1 : page;
        offset = (page - 1) * itemsPerPage;

        List<StudentTeacherConnection> connections = profileDAO.getConnectionsList(account.getId(), itemsPerPage, offset);

        response.put("success", true);
        response.put("data", connections.stream().map(conn -> {
            Map<String, Object> connObject = studentProfileConverter.convert(conn.getStudent());
            connObject.put("createdDate", dateTimeConverter.convert(conn.getCreatedDate()));

            return connObject;
        }).toList());


        return response;
    }

    @DeleteMapping(params = "id")
    public Map<String, Object> deleteTeacherConnection (@RequestAttribute("account") Account account, @RequestParam("id") String id) {
        Map<String, Object> response = new HashMap<>();
        boolean deleted = profileDAO.deleteTeacherStudentConnection(account.getId(), id);

        response.put("success", deleted);

        return response;
    }
}
