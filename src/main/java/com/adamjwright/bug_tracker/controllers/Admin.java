/******************************************************************************
**  /admin routes
**
**  /admin/ - displays the admin functions
******************************************************************************/

package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import com.adamjwright.bug_tracker.ResetDbQuery;
import com.adamjwright.bug_tracker.enums.TemplateBodyEnum;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Admin extends BaseController {
    
    @GetMapping("/admin")
    public String renderAdmin(Authentication authentication, HttpServletRequest request) throws IOException {
        Map<String, Object> context = new HashMap<>();
        addUserDataToModel(context, authentication, request);
		return getMarkupString(context, TemplateBodyEnum.ADMIN);
	}


    // Delete a company from the list
    @PostMapping("/admin/resetTable")
    public void deleteCompany() throws IOException {

        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql query
        String sqlDeleteCompany = new ResetDbQuery().getDbQuery();
        //System.out.println(sqlDeleteCompany);

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlDeleteCompany)) {

            ps.executeQuery();

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
