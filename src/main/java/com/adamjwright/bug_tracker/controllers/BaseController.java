package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import com.adamjwright.bug_tracker.HandlebarsHelpers;
import com.adamjwright.bug_tracker.enums.TemplateBodyEnum;
import com.adamjwright.bug_tracker.enums.TemplateLayoutEnum;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

public class BaseController {
    public void addUserDataToModel(Map<String, Object> context, Authentication authentication, HttpServletRequest request) {
        // Retrieve the user data from the oauth token
        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
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
    }

    public String getMarkupString(TemplateBodyEnum bodyTemplate, TemplateLayoutEnum layoutTemplate) throws IOException {
        Map<String, Object> context = new HashMap<>();
        return getMarkupString(context, bodyTemplate, layoutTemplate);
    }

    public String getMarkupString(Map<String, Object> context, TemplateBodyEnum bodyTemplate) throws IOException {
        return getMarkupString(context, bodyTemplate, TemplateLayoutEnum.MAIN);
    }

    public String getMarkupString(Map<String, Object> context, TemplateBodyEnum bodyTemplate, TemplateLayoutEnum layoutTemplate) throws IOException {
        // Set the directory and file extension of the templates
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".hbs");

        // Create handlebars object and add helper methods
        Handlebars handlebars = new Handlebars(loader);
        handlebars.registerHelpers(HandlebarsHelpers.class);

        // Select the outer layout and inner body templates
        Template layout = handlebars.compile("layouts/" + layoutTemplate.getName());
        Template body = handlebars.compile(bodyTemplate.getName());

        // Parse into a string and return
        Object bodyStr = body.apply(context);
        context.put("body", bodyStr);
        return layout.apply(context);
    }
}
