/******************************************************************************
**  /error 404 and 500 http code routes
******************************************************************************/

package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import com.adamjwright.bug_tracker.HandlebarsHelpers;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorPage implements ErrorController {

    // "Your bugs" the initial dashboard page -- render the bug list
    @GetMapping("/error")
	public String handleError(HttpServletRequest request, Authentication authentication) throws IOException {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        // Set the directory and file extension of the templates
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".hbs");

        // Retrieve the user data from the oauth token
        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
        Map<String, Object> context = new HashMap<>();
        context.put("user", principal.getAttributes());

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            // Handle 404 Error
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                // Create handlebars object and add helper methods
                Handlebars handlebars = new Handlebars(loader);
                handlebars.registerHelpers(HandlebarsHelpers.class);
                
                // Select the outer layout and inner body templates
                Template layout = handlebars.compile("layouts/main");
                Template body = handlebars.compile("404");

                // Parse into a string and return
                String bodyStr = body.apply("");
                context.put("body", bodyStr);
                String templateString = layout.apply(context);
                return templateString;
            }
            
            // Handle 500 Error
            else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                // Create handlebars object and add helper methods
                Handlebars handlebars = new Handlebars(loader);
                handlebars.registerHelpers(HandlebarsHelpers.class);
                
                // Select the outer layout and inner body templates
                Template layout = handlebars.compile("layouts/main");
                Template body = handlebars.compile("500");

                // Parse into a string and return
                String bodyStr = body.apply("");
                context.put("body", bodyStr);
                String templateString = layout.apply(context);
                return templateString;
            }
        }

        return "error";
	}

    @Override
    public String getErrorPath() {
        return null;
    }
}