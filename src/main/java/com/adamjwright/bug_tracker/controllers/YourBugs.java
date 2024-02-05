/******************************************************************************
**  /home routes
**
**  /          -> displays the your-bugs page which is the 1st the user sees
**  /deleteBug -> delete a bug from the list
**  /searchBug -> shows the bugs with fields matching the search string
**  /viewAll   -> rerenders the entire your bugs list
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
import javax.servlet.http.HttpServletResponse;

import com.adamjwright.bug_tracker.enums.TemplateBodyEnum;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YourBugs extends BaseController {

    // "Your bugs" the initial dashboard page -- render the bug list
    @GetMapping("/home")
	public String renderYourBugs(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> context = new HashMap<>();
        addUserDataToModel(context, authentication, request);

        // Pull Programmer's Id from Google oauth added to context
        String userId = (String)((Map)context.get("user")).get("sub");
        
        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Sql query to look for the Google authorized user in the database
        String sql_query_1 = "SELECT firstName, lastName, programmerId, email, mobile_number, dateStarted, accessLevel"
                               + " FROM Programmers" 
                               + " WHERE programmerId = ?";

        // Sql query to register the Google user into the database if they are a new user
        String sql_query_2 = "INSERT INTO Programmers"
                        + " (programmerId, firstName, lastName, email, mobile_number, dateStarted, accessLevel)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Sql query to populate the bug list
        String sql_query_3 = "SELECT p.firstName, p.lastName, bp2.bugId, pj.projectName, b.bugSummary, b.bugDescription,"
                            + " b.dateStarted, b.resolution, b.priority, b.fixed"
                                + " FROM Bugs_Programmers AS bp"
                                + " JOIN Bugs ON bp.bugId = Bugs.bugId"
                                + " INNER JOIN Bugs_Programmers AS bp2 ON bp2.bugId = bp.bugId"
                                + " JOIN Bugs b ON b.bugId = bp2.bugId"
                                + " LEFT OUTER JOIN Programmers AS p ON bp2.programmerId = p.programmerId"
                                + " LEFT OUTER JOIN Projects pj ON b.projectId <=> pj.projectId"
                                    + " WHERE bp.programmerId = ?"
                                    + " ORDER BY dateStarted DESC, bugId DESC";

        // Array of maps to hold the bugs data
        ArrayList<Map<String, Object>> bugsDbData = new ArrayList<Map<String, Object>>();

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps1 = conn.prepareStatement(sql_query_1);
            PreparedStatement ps2 = conn.prepareStatement(sql_query_2);
            PreparedStatement ps3 = conn.prepareStatement(sql_query_3)) {
            
            // Set the programmerId to the Google UID to execute the query
            ps1.setString(1, userId);
            ResultSet rs1 = ps1.executeQuery();

            // No result means the user and new and must be added
            if (!rs1.next()) {
                // Set the new user to an accessLevel of 3
                Cookie cookie = new Cookie("accessLevel", "3");
                cookie.setMaxAge(24 * 60 * 60);
                cookie.setSecure(true);
                response.addCookie(cookie);
                context.put("accessLevel", 3);

                // Register the user in the db
                ps2.setString(1, userId);
                ps2.setString(2, (String)((Map)context.get("user")).get("given_name"));
                ps2.setString(3, (String)((Map)context.get("user")).get("family_name"));
                ps2.setString(4, (String)((Map)context.get("user")).get("email"));
                ps2.setString(5, null);
                ps2.setString(6, new java.sql.Date(System.currentTimeMillis()).toString());
                ps2.setString(7, "3");
                ps2.executeUpdate();
            }
            // The user is already registered in the system
            else {
                // Set the user's stored accessLevel in session cookie and context object
                Cookie cookie = new Cookie("accessLevel", rs1.getString("accessLevel"));
                cookie.setMaxAge(24 * 60 * 60);
                cookie.setSecure(true);
                response.addCookie(cookie);
                context.put("accessLevel", Integer.parseInt(rs1.getString("accessLevel")));

                // Gather the user's bugs from the db
                ps3.setString(1, userId);
                ResultSet rs2 = ps3.executeQuery();

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
            }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Add the company data to the context object
        context.put("bugs", bugsDbData);
		return getMarkupString(context, TemplateBodyEnum.YOUR_BUGS);
	}


    // Delete a bug from the list
    @PostMapping("/home/deleteBug")
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
    @PostMapping("/home/viewAll")
	public Map<String, Object> viewAllYourBugs(Authentication authentication) throws IOException {
        // Retrieve the user data from the oauth token
        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
        Map<String, Object> context = new HashMap<>();
        context.put("user", principal.getAttributes());

        // Pull Programmer's Id from Google oauth added to context
        String userId = (String)((Map)context.get("user")).get("sub");
        
        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Sql query to populate the bug list
        String sqlFindAllBugs = "SELECT p.firstName, p.lastName, bp2.bugId, pj.projectName, b.bugSummary, b.bugDescription,"
                            + " b.dateStarted, b.resolution, b.priority, b.fixed"
                                + " FROM Bugs_Programmers AS bp"
                                + " JOIN Bugs ON bp.bugId = Bugs.bugId"
                                + " INNER JOIN Bugs_Programmers AS bp2 ON bp2.bugId = bp.bugId"
                                + " JOIN Bugs b ON b.bugId = bp2.bugId"
                                + " LEFT OUTER JOIN Programmers AS p ON bp2.programmerId = p.programmerId"
                                + " LEFT OUTER JOIN Projects pj ON b.projectId <=> pj.projectId"
                                    + " WHERE bp.programmerId = ?"
                                    + " ORDER BY dateStarted DESC, bugId DESC";

        // Array of maps to hold the bugs data
        ArrayList<Map<String, Object>> bugsDbData = new ArrayList<Map<String, Object>>();

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps = conn.prepareStatement(sqlFindAllBugs)) {

            // Gather the user's bugs from the db
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

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
    @PostMapping("/home/searchBug")
    public Map<String, Object> searchYourBugs(Authentication authentication, @RequestBody Map<String, Object> payload) throws IOException {
        // Retrieve the user data from the oauth token
        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
        Map<String, Object> context = new HashMap<>();
        context.put("user", principal.getAttributes());

        // Pull Programmer's Id from Google oauth added to context
        String userId = (String)((Map)context.get("user")).get("sub");
        
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
                                    +     " firstName, lastName, priority) LIKE (?)"
                                    +     " AND p.programmerId = ?";

        // Array of maps to hold the bugs data
        ArrayList<Map<String, Object>> bugsDbData = new ArrayList<Map<String, Object>>();

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps1 = conn.prepareStatement(sqlSearchBugsStr)) {

                // Find all bugs matching the search string and userId
                ps1.setString(1, "%"+ (String)payload.get("searchString") +"%");
                ps1.setString(2, userId);
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
