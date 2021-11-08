/******************************************************************************
**  /edit_bug routes
**
**  /          -> get route that displays the edit bug form
**  /updateBug -> post route to edit a bug
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EditBug {
    
    // Displays the current bug's info
    @GetMapping("/edit_bug")
    @ResponseBody
    public String renderEditBug(@RequestParam(name = "bugId", required  = true) String bugId, Authentication authentication, HttpServletRequest request) throws IOException {
        // Retrieve the user data from the oauth token
        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
        Map<String, Object> context = new HashMap<>();
        context.put("user", principal.getAttributes());

        // Gather and set the user accessLevel
        Cookie[] cookies = request.getCookies();
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

        // 1st query gathers the projects for the dropdown
        String sql_query_1 = "SELECT projectName, projectId FROM Projects";

        // 2nd query gathers the programmers for the scrolling checkbox list
        String sql_query_2 = "SELECT programmerId, firstName, lastName FROM Programmers";

        // 3rd query populates the update bug form
        String sql_query_3 = "SELECT p.programmerId, b.bugId, pj.projectName, b.bugSummary, b.bugDescription, b.dateStarted, b.resolution, b.priority, b.fixed, b.projectId"
                            + " FROM Programmers p"
                                + " JOIN Bugs_Programmers bp ON p.programmerId = bp.programmerId"
                                + " JOIN Bugs b ON bp.bugId = b.bugId"
                                + " LEFT OUTER JOIN Projects pj ON b.projectId = pj.projectId"
                                + " WHERE bp.bugId = ?"
                                    + " ORDER BY bugId";

        // Arrays of maps to hold the project and  data
        ArrayList<Map<String, String>> projectDbData = new ArrayList<Map<String, String>>();
        ArrayList<Map<String, Object>> programmersDbData = new ArrayList<Map<String, Object>>();

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps1 = conn.prepareStatement(sql_query_1);
            PreparedStatement ps2 = conn.prepareStatement(sql_query_2);
            PreparedStatement ps3 = conn.prepareStatement(sql_query_3)) {

                ResultSet rs1 = ps1.executeQuery();
                ResultSet rs2 = ps2.executeQuery();
                // Gather the user's bugs from the db
                ps3.setString(1, bugId);
                ResultSet rs3 = ps3.executeQuery();

                // Iterate over result set
                while (rs1.next()) {
                    Map<String, String> hm = new HashMap<>();

                    hm.put("projectId", rs1.getString("projectId"));
                    hm.put("projectName", rs1.getString("projectName"));
                    
                    projectDbData.add(hm);
                }

                String prevEntryBugId = "";  // Cache the previous entry's id to avoid duplication
                ArrayList<String> bugProgrammers = new ArrayList<>();  // Hold the programmers for each entry

                // Iterate over result set
                while (rs3.next()) {
                    // If this is the same bug as the last, then only add the programmer to the array
                    if (prevEntryBugId.equals(rs3.getString("bugId"))) {
                        bugProgrammers.add(rs3.getString("programmerId"));
                    }
                    // This is a different bug than the last
                    else {
                        prevEntryBugId = rs3.getString("bugId");
                        bugProgrammers = new ArrayList<>();
                        bugProgrammers.add(rs3.getString("programmerId"));

                        // Add a completed bug entry
                        Map<String, Object> hm = new HashMap<>();

                        hm.put("bugId", rs3.getString("bugId"));
                        hm.put("bugSummary", rs3.getString("bugSummary"));
                        hm.put("bugDescription", rs3.getString("bugDescription"));
                        hm.put("projectName", rs3.getString("projectName"));
                        hm.put("programmers", bugProgrammers);
                        hm.put("dateStarted", rs3.getString("dateStarted"));
                        hm.put("priority", rs3.getString("priority"));
                        hm.put("fixed", rs3.getString("fixed"));
                        hm.put("resolution", rs3.getString("resolution"));
                        hm.put("projectId", rs3.getInt("projectId"));
                        
                        context.put("editBug", hm);
                    }
                }

                // Iterate over result set
                while (rs2.next()) {
                    Map<String, Object> hm = new HashMap<>();

                    hm.put("programmerId", rs2.getString("programmerId"));
                    hm.put("firstName", rs2.getString("firstName"));
                    hm.put("lastName", rs2.getString("lastName"));
                    hm.put("checked", false);

                    // If the programmer is assigned the bug then set checked prop to true
                    for (String i : bugProgrammers) {
                        if (rs2.getString("programmerId").equals(i)) {
                            hm.put("checked", true);
                        }
                    }

                    programmersDbData.add(hm);
                }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Add the data to the context object
        context.put("projects", projectDbData);
        context.put("programmers", programmersDbData);

        // Set the directory and file extension of the templates
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".hbs");

        // Create handlebars object and add helper methods
        Handlebars handlebars = new Handlebars(loader);
        handlebars.registerHelpers(HandlebarsHelpers.class);

        // Select the outer layout and inner body templates
        Template layout = handlebars.compile("layouts/main");
        Template body = handlebars.compile("edit-bug");

        // Parse into a string and return
        Object bodyStr = body.apply(context);
        context.put("body", bodyStr);
        String templateString = layout.apply(context);
        return templateString;
    }


    // Submits a bug edit
    @PostMapping("/edit_bug/updateBug")
    public void updateBug(@RequestBody Map<String, Object> payload) throws IOException {

        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Query to insert the bug data
        String sql_query_1 = "UPDATE Bugs SET bugSummary = ?, bugDescription = ?, projectId = ?, dateStarted = ?,"
                            + " priority = ?, fixed = ?, resolution = ?"
                                + " WHERE bugId = ?";

        // Query to delete all Bugs_Programmers for the current bugId
        String sql_query_2 = "DELETE FROM Bugs_Programmers WHERE bugId = ?";

        // Query to run in loop to create Bugs_Programmers instances for the current bugId
        String sql_query_3 = "INSERT INTO Bugs_Programmers (bugId, programmerId)"
                                + " VALUES (?, ?)";

        // Get the set of programmers for the new bug
        ArrayList<String> programmerArr = (ArrayList<String>)payload.get("programmerArr");

        // System.out.println("\n --- PAYLOAD --- \n" + payload + "\n --- PAYLOAD --- \n");

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps1 = conn.prepareStatement(sql_query_1);
            PreparedStatement ps2 = conn.prepareStatement(sql_query_2);
            PreparedStatement ps3 = conn.prepareStatement(sql_query_3)) {

            // Set the query string param companyId in the mysql prepared statement
            ps1.setString(1, (String)payload.get("bugSummary"));
            ps1.setString(2, (String)payload.get("bugDescription"));
            ps1.setString(4, (String)payload.get("bugStartDate"));
            ps1.setInt(5, Integer.parseInt((String)payload.get("bugPriority")));
            ps1.setBoolean(6, Boolean.parseBoolean((String)payload.get("bugFixed")));
            ps1.setString(7, (String)payload.get("bugResolution"));
            ps1.setInt(8, Integer.parseInt((String)payload.get("bugId")));

            // Check for null project
            if (payload.get("bugProject").equals("null")) {
                ps1.setNull(3, java.sql.Types.INTEGER);
            }
            else {
                ps1.setInt(3, Integer.parseInt((String)payload.get("bugProject")));
            }

            ps1.executeUpdate();

            // Delete the current bugs_programmers entries
            ps2.setInt(1, Integer.parseInt((String)payload.get("bugId")));
            ps2.executeUpdate();

            // Insert the bug id into bugs_programmers junction table
            for (String i : programmerArr) {
                ps3.setInt(1, Integer.parseInt((String)payload.get("bugId")));
                ps3.setString(2, i);

                ps3.executeUpdate();
            }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
