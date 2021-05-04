/******************************************************************************
**  /login route that displays the Google oauth login button
******************************************************************************/

package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginPage {

    // The login page which is the first page rendered when visiting the app
    @GetMapping("/login")
	public String renderLogin() throws IOException {
        // Set the directory and file extension of the templates
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".hbs");

        // Select the outer layout and inner body templates
        Handlebars handlebars = new Handlebars(loader);
        Template layout = handlebars.compile("layouts/login");
        Template body = handlebars.compile("login-page");

        // Parse into a string and return
        String bodyStr = body.apply("");
        String templateString = layout.apply(bodyStr);
		return templateString;
	}
}
