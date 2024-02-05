/******************************************************************************
**  /programmers routes
**
**  / - displays the list of current companies
******************************************************************************/

package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import com.adamjwright.bug_tracker.enums.TemplateBodyEnum;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Programmers extends BaseController {
    
    // Displays the list of current programmers
    @GetMapping("/programmers")
    public String renderProgrammers(Authentication authentication, HttpServletRequest request) throws IOException {
        Map<String, Object> context = new HashMap<>();
        addUserDataToModel(context, authentication, request);
        
        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql query
        String sqlSelectAllProgrammers = "SELECT * FROM Programmers ORDER BY dateStarted DESC";

        // Array of maps to hold the company data
        ArrayList<Map<String, String>> programmersDbData = new ArrayList<Map<String, String>>();

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlSelectAllProgrammers); 
            ResultSet rs = ps.executeQuery()) {

                // Iterate over result set
                while (rs.next()) {
                    Map<String, String> hm = new HashMap<>();

                    hm.put("programmerId", rs.getString("programmerId"));
                    hm.put("firstName", rs.getString("firstName"));
                    hm.put("lastName", rs.getString("lastName"));
                    hm.put("email", rs.getString("email"));
                    hm.put("dateStarted", rs.getString("dateStarted"));
                    hm.put("accessLevel", rs.getString("accessLevel"));

                    programmersDbData.add(hm);
                }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Add the company data to the context object
        context.put("programmers", programmersDbData);
        return getMarkupString(context, TemplateBodyEnum.PROGRAMMERS);
    }
}
