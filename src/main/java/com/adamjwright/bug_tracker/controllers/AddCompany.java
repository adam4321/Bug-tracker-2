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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.adamjwright.bug_tracker.HandlebarsHelpers;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AddCompany {

    // Displays the list of current companies
    @GetMapping("/add_company")
    public String renderAddCompany(Authentication authentication, HttpServletRequest request) throws IOException {
        // Retrieve the user data from the oauth token
        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
        Map<String, Object> context = new HashMap<>();
        context.put("user", principal.getAttributes());

        // Gather and set the user accessLevel
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("accessLevel")) {
                    context.put("accessLevel", Integer.parseInt(c.getValue()));
                }
            }
        }
        
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

        // Set the directory and file extension of the templates
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".hbs");

        // Create handlebars object and add helper methods
        Handlebars handlebars = new Handlebars(loader);
        handlebars.registerHelpers(HandlebarsHelpers.class);

        // Select the outer layout and inner body templates
        Template layout = handlebars.compile("layouts/main");
        Template body = handlebars.compile("add-company");

        // Parse into a string and return
        Object bodyStr = body.apply(context);
        context.put("body", bodyStr);
        String templateString = layout.apply(context);
        return templateString;
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
