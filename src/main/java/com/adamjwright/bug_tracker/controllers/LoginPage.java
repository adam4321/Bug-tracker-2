package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginPage {
    @RequestMapping("/")
	public String index() throws IOException {
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".hbs");

        Handlebars handlebars = new Handlebars(loader);
        Template body = handlebars.compile("login-page");
        Template layout = handlebars.compile("layouts/login");
        String templateString = layout.apply(body);

		return templateString;
	}
}
