/******************************************************************************
**  /add_bug routes
**
**  /          -> displays the add bug form
**  /insertBug -> insert a new bug
******************************************************************************/

package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import com.adamjwright.bug_tracker.enums.TemplateBodyEnum;
import com.adamjwright.bug_tracker.enums.TemplateLayoutEnum;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AddBug extends BaseController {
    
    @GetMapping("/add_bug")
    public String renderAddBug(Authentication authentication, HttpServletRequest request) throws IOException {
        Map<String, Object> context = new HashMap<>();
        addUserDataToModel(context, authentication, request);

        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Array of maps to hold the project data
        ArrayList<Map<String, String>> projectDbData = new ArrayList<>();
        ArrayList<Map<String, String>> programmerDbData = new ArrayList<>();

        // 1st query gathers the projects for the dropdown
        String sql_query_1 = "SELECT projectName, projectId FROM Projects";

        // 2nd query gathers the programmers for the scrolling checkbox list
        String sql_query_2 = "SELECT programmerId, firstName, lastName FROM Programmers";

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps1 = conn.prepareStatement(sql_query_1); 
            PreparedStatement ps2 = conn.prepareStatement(sql_query_2);
            ResultSet rs1 = ps1.executeQuery();
            ResultSet rs2 = ps2.executeQuery()) {

                // Iterate over project result set
                while (rs1.next()) {
                    Map<String, String> hm = new HashMap<>();

                    hm.put("projectId", rs1.getString("projectId"));
                    hm.put("projectName", rs1.getString("projectName"));
                    
                    projectDbData.add(hm);
                }

                // Iterate over programmer result set
                while (rs2.next()) {
                    Map<String, String> hm = new HashMap<>();

                    hm.put("programmerId", rs2.getString("programmerId"));
                    hm.put("firstName", rs2.getString("firstName"));
                    hm.put("lastName", rs2.getString("lastName"));
                    
                    programmerDbData.add(hm);
                }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Add the company data to the context object
        context.put("projects", projectDbData);
        context.put("programmers", programmerDbData);
		return getMarkupString(context, TemplateBodyEnum.ADD_BUG, TemplateLayoutEnum.MAIN);
	}


    // Submits a new bug
    @PostMapping("/add_bug/insertBug")
    public void insertBug(@RequestBody Map<String, Object> payload) throws IOException {

        // Get database configuration
        ResourceBundle reader = ResourceBundle.getBundle("dbconfig");
        String CONNECTION_URL = reader.getString("db.url");
        String DB_USER = reader.getString("db.username");
        String DB_PASSWORD = reader.getString("db.password");

        // Get the set of programmers for the new bug
        ArrayList<String> programmerArr = (ArrayList<String>)payload.get("programmerArr");

        // Query to insert the bug data
        String sql_query_1 = "INSERT INTO Bugs (bugSummary, bugDescription, projectId, dateStarted, priority, fixed, resolution)"
                                + " VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Query to run in loop to create Bugs_Programmers instances
        String sql_query_2 = "INSERT INTO Bugs_Programmers (bugId, programmerId)"
                                + " VALUES (?, ?)";

        // Connect to db
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, DB_USER, DB_PASSWORD); 
            PreparedStatement ps1 = conn.prepareStatement(sql_query_1, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement ps2 = conn.prepareStatement(sql_query_2)) {

            // Set the query string param companyId in the mysql prepared statement
            ps1.setString(1, (String)payload.get("bugSummary"));
            ps1.setString(2, (String)payload.get("bugDescription"));
            ps1.setString(4, (String)payload.get("bugStartDate"));
            ps1.setInt(5, Integer.parseInt((String)payload.get("bugPriority")));
            ps1.setInt(6, Integer.parseInt((String)payload.get("bugFixed")));
            ps1.setString(7, (String)payload.get("bugResolution"));

            // Check for null project
            if (payload.get("bugProject").equals("null")) {
                ps1.setNull(3, java.sql.Types.INTEGER);
            }
            else {
                ps1.setInt(3, Integer.parseInt((String)payload.get("bugProject")));
            }

            ps1.executeUpdate();
            
            // Get the insertion id of the new bug
            ResultSet rs = ps1.getGeneratedKeys();
            int insertId = -1;
            
            if (rs.next()) {
                insertId = rs.getInt(1);
            }

            // Insert the bug id into bugs_programmers junction table
            for (String i : programmerArr) {
                ps2.setInt(1, insertId);
                ps2.setString(2, i);

                ps2.executeUpdate();
            }

            conn.close();
        } 
        // Handle a failed db connection
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
