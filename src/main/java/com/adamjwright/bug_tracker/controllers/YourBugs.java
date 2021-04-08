/******************************************************************************
**  /home route displays the your-bugs page which is the 1st the user sees
******************************************************************************/

package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.adamjwright.bug_tracker.HandlebarsHelpers;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YourBugs {

    // "Your bugs" the initial dashboard page -- render the bug list
    @GetMapping("/home")
	public String index(Authentication authentication) throws IOException {
        // Retrieve the user data from the oauth token
        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
        Map<String, Object> context = new HashMap<>();
        context.put("user", principal.getAttributes());

        // System.out.println("\n-- REQUEST --\n" + context.get("user") + "\n-- REQUEST --\n");

        // Set the directory and file extension of the templates
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".hbs");

        // Create handlebars object and add helper methods
        Handlebars handlebars = new Handlebars(loader);
        handlebars.registerHelpers(HandlebarsHelpers.class);

        // Select the outer layout and inner body templates
        Template layout = handlebars.compile("layouts/main");
        Template body = handlebars.compile("your-bugs");

        // Parse into a string and return
        Object bodyStr = body.apply("");
        context.put("body", bodyStr);
        String templateString = layout.apply(context);
		return templateString;
	}
}
