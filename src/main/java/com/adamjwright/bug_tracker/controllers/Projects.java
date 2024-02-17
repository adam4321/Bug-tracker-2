/******************************************************************************
**  /projects routes
**
**  / -> displays the list of current projects
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Projects extends BaseController {

    // Displays the list of current projects
    @GetMapping("/projects")
    public String renderProjects(Authentication authentication, HttpServletRequest request) throws IOException {
        Map<String, Object> context = addUserDataToModel(authentication, request);
        
        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql query
        String sqlProjectsQuery = "SELECT * FROM Projects AS p JOIN Companies AS c ON p.companyId = c.companyId"
                               + " ORDER BY dateStarted DESC, projectId DESC";

        // Array of maps to hold the company data
        ArrayList<Map<String, String>> projectDbData = new ArrayList<Map<String, String>>();

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlProjectsQuery); 
            ResultSet rs = ps.executeQuery()) {

                // Iterate over result set
                while (rs.next()) {
                    Map<String, String> hm = new HashMap<>();

                    hm.put("projectId", rs.getString("projectId"));
                    hm.put("projectName", rs.getString("projectName"));
                    hm.put("companyName", rs.getString("companyName"));
                    hm.put("dateStarted", rs.getString("dateStarted"));
                    hm.put("lastUpdated", rs.getString("lastUpdated"));
                    hm.put("inMaintenance", rs.getString("inMaintenance"));
                    
                    projectDbData.add(hm);
                }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Add the company data to the context object
        context.put("projects", projectDbData);
        return getMarkupString(context, TemplateBodyEnum.PROJECTS);
    }


    // Delete a project from the list
    @PostMapping("/projects/deleteProject")
    public void deleteProject(@RequestBody Map<String, Object> payload) throws IOException {

        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql query
        String sqlDeleteCompany = "DELETE FROM Projects WHERE projectId = ?";

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlDeleteCompany)) {

            // Set the query string param companyId in the mysql prepared statement
            ps.setInt(1, (Integer)payload.get("projectId"));
            ps.executeUpdate();

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
