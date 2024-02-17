/******************************************************************************
**  /edit_company routes
**
**  /              -> get route that displays the edit company form
**  /updateCompany -> post route to edit a company
******************************************************************************/

package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class EditCompany extends BaseController {
    
    // Displays the current company's info
    @GetMapping("/edit_company")
    @ResponseBody
    public String renderEditCompany(@RequestParam(name = "companyId", required  = true) String companyId,
            Authentication authentication, HttpServletRequest request) throws IOException {
        Map<String, Object> context = addUserDataToModel(authentication, request);
        
        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql query
        String sqlSelectAllCompanies = "SELECT companyName, dateJoined FROM Companies WHERE companyId = ?";

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlSelectAllCompanies)) {

                // Set the query string param companyId in the mysql prepared statement
                ps.setString(1, companyId);
                ResultSet rs = ps.executeQuery();

                // Iterate over result set and create an array of maps
                while (rs.next()) {
                    context.put("companyId", companyId);
                    context.put("companyName", rs.getString("companyName"));
                    context.put("dateJoined", rs.getString("dateJoined"));
                }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        return getMarkupString(context, TemplateBodyEnum.EDIT_COMPANY);
    }


    // Submits a company edit
    @PostMapping("/edit_company/updateCompany")
    public void updateCompany(@RequestBody Map<String, Object> payload) throws IOException {

        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql query
        String sqlUpdateCompany = "UPDATE Companies SET companyName = ?, dateJoined = ? WHERE companyId = ?";

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlUpdateCompany)) {

            // Set the query string param companyId in the mysql prepared statement
            ps.setString(1, (String)payload.get("companyName"));
            ps.setString(2, (String)payload.get("dateJoined"));
            ps.setInt(3, Integer.parseInt((String)payload.get("companyId")));
            ps.executeUpdate();

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
