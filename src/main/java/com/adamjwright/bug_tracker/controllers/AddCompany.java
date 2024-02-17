/******************************************************************************
**  /add_company routes
**
**  /              -> displays the add company form
**  /insertCompany ->  Adds a new company
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
public class AddCompany extends BaseController {

    // Displays the list of current companies
    @GetMapping("/add_company")
    public String renderAddCompany(Authentication authentication, HttpServletRequest request) throws IOException {
        Map<String, Object> context = addUserDataToModel(authentication, request);
        
        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql query
        String sqlSelectAllCompanies = "SELECT * FROM Companies ORDER BY dateJoined DESC";

        // Array of maps to hold the company data
        ArrayList<Map<String, String>> companyDbData = new ArrayList<Map<String, String>>();

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlSelectAllCompanies); 
            ResultSet rs = ps.executeQuery()) {

                // Iterate over result set and create an array of maps
                while (rs.next()) {
                    Map<String, String> hm = new HashMap<>();

                    hm.put("companyId", rs.getString("companyId"));
                    hm.put("companyName", rs.getString("companyName"));
                    hm.put("dateJoined", rs.getString("dateJoined"));
                    
                    companyDbData.add(hm);
                }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Add the company data to the context object
        context.put("companies", companyDbData);
        return getMarkupString(context, TemplateBodyEnum.ADD_COMPANY);
    }


    // Submits a new company
    @PostMapping("/add_company/insertCompany")
    public void insertCompany(@RequestBody Map<String, Object> payload) throws IOException {

        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql query
        String sqlAddCompany = "INSERT INTO Companies (companyName, dateJoined) VALUES (?, ?)";

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlAddCompany)) {

            // Set the query string param companyId in the mysql prepared statement
            ps.setString(1, (String)payload.get("companyName"));
            ps.setString(2, (String)payload.get("dateJoined"));

            ps.executeUpdate();

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
