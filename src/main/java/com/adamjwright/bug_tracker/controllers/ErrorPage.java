package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorPage implements ErrorController {

    // "Your bugs" the initial dashboard page -- render the bug list
    @RequestMapping("/error")
	public String handleError(HttpServletRequest request) throws IOException {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        // Set the directory and file extension of the templates
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".hbs");

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            // Handle 404 Error
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                // Select the outer layout and inner body templates
                Handlebars handlebars = new Handlebars(loader);
                Template layout = handlebars.compile("layouts/main");
                Template body = handlebars.compile("404");

                // Parse into a string and return
                String bodyStr = body.apply("");
                String templateString = layout.apply(bodyStr);

                return templateString;
            }

            // Handle 401 Error
            else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                // Select the outer layout and inner body templates
                Handlebars handlebars = new Handlebars(loader);
                Template layout = handlebars.compile("layouts/login");
                Template body = handlebars.compile("unauthorized-page");

                // Parse into a string and return
                String bodyStr = body.apply("");
                String templateString = layout.apply(bodyStr);

                System.out.println("\n UNAUTHORIZED");

                return templateString;
            }
            
            // Handle 500 Error
            else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                // Select the outer layout and inner body templates
                Handlebars handlebars = new Handlebars(loader);
                Template layout = handlebars.compile("layouts/main");
                Template body = handlebars.compile("500");

                // Parse into a string and return
                String bodyStr = body.apply("");
                String templateString = layout.apply(bodyStr);

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