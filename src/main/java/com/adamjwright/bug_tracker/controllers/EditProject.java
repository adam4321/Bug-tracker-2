/******************************************************************************
**  /edit_project routes
**
**  /              -> get route that displays the edit project form
**  /updateProject -> post route to edit a project
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EditProject extends BaseController {

    // Displays the current project's info
    @GetMapping("/edit_project")
    @ResponseBody
    public String renderEditProject(@RequestParam(name = "projectId", required  = true) String projectId,
            Authentication authentication, HttpServletRequest request) throws IOException {
        Map<String, Object> context = new HashMap<>();
        addUserDataToModel(context, authentication, request);
        
        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql queries
        String sqlSelectAllCompanies = "SELECT companyId, companyName FROM Companies";

        String sqlFindProject = "SELECT c.companyName, p.projectName, p.dateStarted, p.lastUpdated, p.inMaintenance"
                            + " FROM Projects p JOIN Companies c ON p.companyId = c.companyId"
                            + " WHERE p.projectId = ?";

        // Array of maps to hold the company data
        ArrayList<Map<String, String>> companyDbData = new ArrayList<Map<String, String>>();

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps1 = conn.prepareStatement(sqlSelectAllCompanies);
            PreparedStatement ps2 = conn.prepareStatement(sqlFindProject)) {
                ResultSet rs1 = ps1.executeQuery();

                // Iterate over result set and create an array of maps
                while (rs1.next()) {
                    Map<String, String> hm = new HashMap<>();

                    hm.put("companyId", rs1.getString("companyId"));
                    hm.put("companyName", rs1.getString("companyName"));
                    
                    companyDbData.add(hm);
                }

                // Set the query string param companyId in the mysql prepared statement
                ps2.setString(1, projectId);
                ResultSet rs2 = ps2.executeQuery();

                // Iterate over result set and create an array of maps
                while (rs2.next()) {
                    context.put("projectId", projectId);
                    context.put("projectName", rs2.getString("projectName"));
                    context.put("companyName", rs2.getString("companyName"));
                    context.put("dateStarted", rs2.getString("dateStarted"));
                    context.put("lastUpdated", rs2.getString("lastUpdated"));
                    context.put("inMaintenance", rs2.getString("inMaintenance"));
                }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Add the company data to the context object
        context.put("companies", companyDbData);
        return getMarkupString(context, TemplateBodyEnum.EDIT_PROJECT);
    }


    // Submits a project edit
    @PostMapping("/edit_project/updateProject")
    public void updateProject(@RequestBody Map<String, Object> payload) throws IOException {

        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql query
        String sqlUpdateCompany = "UPDATE Projects SET projectName = ?,"
                                + " companyId = (SELECT companyId FROM Companies WHERE companyName = ?),"
                                + " dateStarted = ?, lastUpdated = ?, inMaintenance = ?"
                                    + " WHERE projectId = ?";

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlUpdateCompany)) {

            // Set the query string param companyId in the mysql prepared statement
            ps.setString(1, (String)payload.get("projectName"));
            ps.setString(2, (String)payload.get("companyName"));
            ps.setString(3, (String)payload.get("dateStarted"));
            ps.setString(4, (String)payload.get("lastUpdated"));
            ps.setInt(5, Integer.parseInt((String)payload.get("inMaintenance")));
            ps.setString(6, (String)payload.get("projectId"));
            ps.executeUpdate();

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
