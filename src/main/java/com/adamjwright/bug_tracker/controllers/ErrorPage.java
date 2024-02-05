/******************************************************************************
**  /error 404 and 500 http code routes
******************************************************************************/

package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import com.adamjwright.bug_tracker.enums.TemplateBodyEnum;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorPage extends BaseController implements ErrorController {

    // Parse the http error and display the correct error page
    @GetMapping("/error")
	public String handleError(Authentication authentication, HttpServletRequest request) throws IOException {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        Map<String, Object> context = new HashMap<>();
        addUserDataToModel(context, authentication, request);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            // Handle 404 Error
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return getMarkupString(context, TemplateBodyEnum.NOT_FOUND);
            }
            
            // Handle 500 Error
            else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return getMarkupString(context, TemplateBodyEnum.SERVER_ERROR);
            }
        }

        return "error";
	}

    @Override
    public String getErrorPath() {
        return null;
    }
}