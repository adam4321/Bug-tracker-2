/******************************************************************************
**  /all_bugs routes
**
**  /          -> displays the list of all current bugs
**  /deleteBug -> delete a bug from the list (this route is reused in /home)
**  /searchBug -> display bugs matching the search string
**  /viewAll   -> restore the full list of bugs
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
public class AllBugs {

    // Displays the list of all currenct bugs
    @GetMapping("/all_bugs")
    public String renderAllBugs(Authentication authentication, HttpServletRequest request) throws IOException {
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

        // Query to populate the bug list
        String sqlSelectAllBugs = "SELECT p.firstName, p.lastName, b.bugId, pj.projectName, b.bugSummary,"
                                    + " b.bugDescription, b.dateStarted, b.resolution, b.priority, b.fixed"
                                    + " FROM Programmers p"
                                    + " JOIN Bugs_Programmers bp ON p.programmerId = bp.programmerId"
                                    + " JOIN Bugs b ON bp.bugId = b.bugId"
                                    + " LEFT OUTER JOIN Projects pj ON b.projectId <=> pj.projectId"
                                        + " ORDER BY dateStarted DESC, bugId DESC";

        // Array of maps to hold the bugs data
        ArrayList<Map<String, Object>> bugsDbData = new ArrayList<Map<String, Object>>();

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlSelectAllBugs); 
            ResultSet rs = ps.executeQuery()) {

                String prevEntryBugId = "";  // Cache the previous entry's id to avoid duplication
                ArrayList<String> bugProgrammers = new ArrayList<>();  // Hold the programmers for each entry

                // Iterate over result set
                while (rs.next()) {
                    // If this is the same bug as the last, then only add the programmer to the array
                    if (prevEntryBugId.equals(rs.getString("bugId"))) {
                        bugProgrammers.add(rs.getString("firstName") + ' ' + rs.getString("lastName"));
                    }
                    // This is a different bug than the last
                    else {
                        prevEntryBugId = rs.getString("bugId");
                        bugProgrammers = new ArrayList<>();
                        bugProgrammers.add(rs.getString("firstName") + ' ' + rs.getString("lastName"));

                        // Add a completed bug entry
                        Map<String, Object> hm = new HashMap<>();

                        hm.put("bugId", rs.getString("bugId"));
                        hm.put("bugSummary", rs.getString("bugSummary"));
                        hm.put("bugDescription", rs.getString("bugDescription"));
                        hm.put("projectName", rs.getString("projectName"));
                        hm.put("programmers", bugProgrammers);
                        hm.put("dateStarted", rs.getString("dateStarted"));
                        hm.put("priority", rs.getString("priority"));
                        hm.put("fixed", rs.getString("fixed"));
                        hm.put("resolution", rs.getString("resolution"));
                        
                        bugsDbData.add(hm);
                    }
                }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Add the company data to the context object
        context.put("bugs", bugsDbData);

        // Set the directory and file extension of the templates
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".hbs");

        // Create handlebars object and add helper methods
        Handlebars handlebars = new Handlebars(loader);
        handlebars.registerHelpers(HandlebarsHelpers.class);

        // Select the outer layout and inner body templates
        Template layout = handlebars.compile("layouts/main");
        Template body = handlebars.compile("all-bugs");

        // Parse into a string and return
        Object bodyStr = body.apply(context);
        context.put("body", bodyStr);
        String templateString = layout.apply(context);
        return templateString;
    }


    // Delete a bug from the list
    @PostMapping("/all_bugs/deleteBug")
    public void deleteBug(@RequestBody Map<String, Object> payload) throws IOException {

        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Define sql query
        String sqlDeleteBug = "DELETE FROM Bugs WHERE bugId = ?";

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlDeleteBug)) {

            // Set the query string param bugId in the mysql prepared statement
            ps.setInt(1, (Integer)payload.get("bugId"));
            ps.executeUpdate();

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Return the entire your bugs list when view all button is pressed
    @PostMapping("/all_bugs/viewAll")
    public Map<String, Object> viewAllBugs() throws IOException {
        // Create empty context object
        Map<String, Object> context = new HashMap<>();
        
        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Query to populate the bug list
        String sqlSelectAllBugs = "SELECT p.firstName, p.lastName, b.bugId, pj.projectName, b.bugSummary,"
                                    + " b.bugDescription, b.dateStarted, b.resolution, b.priority, b.fixed"
                                    + " FROM Programmers p"
                                    + " JOIN Bugs_Programmers bp ON p.programmerId = bp.programmerId"
                                    + " JOIN Bugs b ON bp.bugId = b.bugId"
                                    + " LEFT OUTER JOIN Projects pj ON b.projectId <=> pj.projectId"
                                        + " ORDER BY dateStarted DESC, bugId DESC";

        // Array of maps to hold the bugs data
        ArrayList<Map<String, Object>> bugsDbData = new ArrayList<Map<String, Object>>();

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlSelectAllBugs); 
            ResultSet rs = ps.executeQuery()) {

                String prevEntryBugId = "";  // Cache the previous entry's id to avoid duplication
                ArrayList<String> bugProgrammers = new ArrayList<>();  // Hold the programmers for each entry

                // Iterate over result set
                while (rs.next()) {
                    // If this is the same bug as the last, then only add the programmer to the array
                    if (prevEntryBugId.equals(rs.getString("bugId"))) {
                        bugProgrammers.add(rs.getString("firstName") + ' ' + rs.getString("lastName"));
                    }
                    // This is a different bug than the last
                    else {
                        prevEntryBugId = rs.getString("bugId");
                        bugProgrammers = new ArrayList<>();
                        bugProgrammers.add(rs.getString("firstName") + ' ' + rs.getString("lastName"));

                        // Add a completed bug entry
                        Map<String, Object> hm = new HashMap<>();

                        hm.put("bugId", rs.getString("bugId"));
                        hm.put("bugSummary", rs.getString("bugSummary"));
                        hm.put("bugDescription", rs.getString("bugDescription"));
                        hm.put("projectName", rs.getString("projectName"));
                        hm.put("programmers", bugProgrammers);
                        hm.put("dateStarted", rs.getString("dateStarted"));
                        hm.put("priority", rs.getString("priority"));
                        hm.put("fixed", rs.getString("fixed"));
                        hm.put("resolution", rs.getString("resolution"));
                        
                        bugsDbData.add(hm);
                    }
                }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Add the company data to the context object
        context.put("bugs", bugsDbData);
        return context;
    }


    // Return the entire your bugs list when view all button is pressed
    @PostMapping("/all_bugs/searchBug")
    public Map<String, Object> searchAllBugs(@RequestBody Map<String, Object> payload) throws IOException {
        // Create empty context object
        Map<String, Object> context = new HashMap<>();
        
        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Query to find bugs that match substring
        String sqlSearchBugsStr = "SELECT p.firstName, p.lastName, b.bugId, pj.projectName, b.bugSummary, b.bugDescription,"
                                    + " b.dateStarted, b.resolution, b.priority, b.fixed FROM Programmers p"
                                    + " JOIN Bugs_Programmers bp ON p.programmerId = bp.programmerId"
                                    + " JOIN Bugs b ON bp.bugId = b.bugId"
                                    + " LEFT OUTER JOIN Projects pj ON b.projectId = pj.projectId"
                                    +     " WHERE CONCAT(bugSummary, bugDescription, resolution, projectName,"
                                    +     " firstName, lastName, priority) LIKE (?)";

        // Array of maps to hold the bugs data
        ArrayList<Map<String, Object>> bugsDbData = new ArrayList<Map<String, Object>>();

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps1 = conn.prepareStatement(sqlSearchBugsStr)) {

                // Find all bugs matching the search string
                ps1.setString(1, "%"+ (String)payload.get("searchString") +"%");
                ResultSet rs1 = ps1.executeQuery();

                // If there are no matches, then return
                if (!rs1.isBeforeFirst()) {
                    context.put("bugs", bugsDbData);
                    return context;
                }

                // Create a comma separated string of the matching bugIds
                StringBuilder sb = new StringBuilder();

                while (rs1.next()) {
                    sb.append((String)rs1.getString("bugId")).append(",");
                }

                sb.deleteCharAt(sb.length() - 1).toString();
                String idString = sb.toString();

                // Query to gather data of bugs in the initial query results
                String sqlBugsQuery = "SELECT p.firstName, p.lastName, b.bugId, pj.projectName, b.bugSummary, b.bugDescription,"
                                    + " b.dateStarted, b.resolution, b.priority, b.fixed FROM Programmers p"
                                    + " JOIN Bugs_Programmers bp ON p.programmerId = bp.programmerId"
                                    + " JOIN Bugs b ON bp.bugId = b.bugId"
                                    + " LEFT OUTER JOIN Projects pj ON b.projectId = pj.projectId"
                                    +     " WHERE b.bugId IN ("+ idString +")"
                                    +     " ORDER BY dateStarted DESC, bugId DESC";

                // Find the matching bugs
                PreparedStatement ps2 = conn.prepareStatement(sqlBugsQuery);
                ResultSet rs2 = ps2.executeQuery();

                String prevEntryBugId = "";  // Cache the previous entry's id to avoid duplication
                ArrayList<String> bugProgrammers = new ArrayList<>();  // Hold the programmers for each entry

                // Iterate over result set
                while (rs2.next()) {
                    // If this is the same bug as the last, then only add the programmer to the array
                    if (prevEntryBugId.equals(rs2.getString("bugId"))) {
                        bugProgrammers.add(rs2.getString("firstName") + ' ' + rs2.getString("lastName"));
                    }
                    // This is a different bug than the last
                    else {
                        prevEntryBugId = rs2.getString("bugId");
                        bugProgrammers = new ArrayList<>();
                        bugProgrammers.add(rs2.getString("firstName") + ' ' + rs2.getString("lastName"));

                        // Add a completed bug entry
                        Map<String, Object> hm = new HashMap<>();

                        hm.put("bugId", rs2.getString("bugId"));
                        hm.put("bugSummary", rs2.getString("bugSummary"));
                        hm.put("bugDescription", rs2.getString("bugDescription"));
                        hm.put("projectName", rs2.getString("projectName"));
                        hm.put("programmers", bugProgrammers);
                        hm.put("dateStarted", rs2.getString("dateStarted"));
                        hm.put("priority", rs2.getString("priority"));
                        hm.put("fixed", rs2.getString("fixed"));
                        hm.put("resolution", rs2.getString("resolution"));
                        
                        bugsDbData.add(hm);
                    }
                }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Add the company data to the context object
        context.put("bugs", bugsDbData);
        return context;
    }
}
