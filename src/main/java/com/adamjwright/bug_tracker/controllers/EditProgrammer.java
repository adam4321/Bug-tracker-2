/******************************************************************************
**  /edit_programmer routes
**
**  /                 -> get route that displays the edit project form
**  /updateProgrammer -> post route to edit a project
******************************************************************************/

package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EditProgrammer {
        
    // Displays the current programmer's info
    @GetMapping("/edit_programmer")
    @ResponseBody
    public String renderEditProgrammer(@RequestParam(name = "programmerId", required  = true) String programmerId, Authentication authentication, HttpServletRequest request) throws IOException {
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
        String sqlFindProgrammer = "SELECT firstName, lastName, email, mobile_number, dateStarted, accessLevel FROM Programmers"
                                    + " WHERE programmerId = ?";

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlFindProgrammer)) {

                // Set the query string param programmerId in the mysql prepared statement
                ps.setString(1, programmerId);
                ResultSet rs = ps.executeQuery();

                // Iterate over result set and create an array of maps
                while (rs.next()) {
                    context.put("programmerId", programmerId);
                    context.put("firstName", rs.getString("firstName"));
                    context.put("lastName", rs.getString("lastName"));
                    context.put("email", rs.getString("email"));
                    context.put("phone", rs.getString("mobile_number"));
                    context.put("dateStarted", rs.getString("dateStarted"));
                    context.put("accLvl", rs.getString("accessLevel"));
                }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the directory and file extension of the templates
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".hbs");

        // Create handlebars object and add helper methods
        Handlebars handlebars = new Handlebars(loader);
        handlebars.registerHelpers(HandlebarsHelpers.class);

        // Select the outer layout and inner body templates
        Template layout = handlebars.compile("layouts/main");
        Template body = handlebars.compile("edit-programmer");

        // Parse into a string and return
        Object bodyStr = body.apply(context);
        context.put("body", bodyStr);
        String templateString = layout.apply(context);
        return templateString;
    }


    // Submits a programmer edit
    @PostMapping("/edit_programmer/updateProgrammer")
    public void updateProgrammer(@RequestBody Map<String, Object> payload) throws IOException {

        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql query
        String sqlUpdateProgrammer = "UPDATE Programmers SET firstName = ?, lastName = ?, email = ?," 
                                    + " mobile_number = ?, dateStarted = ?, accessLevel = ?"
                                        + " WHERE programmerId = ?";

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlUpdateProgrammer)) {

            // Set the query string param companyId in the mysql prepared statement
            ps.setString(1, (String)payload.get("firstName"));
            ps.setString(2, (String)payload.get("lastName"));
            ps.setString(3, (String)payload.get("email"));
            ps.setString(4, (String)payload.get("mobile_number"));
            ps.setString(5, (String)payload.get("dateStarted"));
            ps.setInt(6, Integer.parseInt((String)payload.get("accessLevel")));
            ps.setString(7, (String)payload.get("programmerId"));
            ps.executeUpdate();

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
