/******************************************************************************
**  /login route that displays the Google oauth login button
******************************************************************************/

package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;

import com.adamjwright.bug_tracker.enums.TemplateBodyEnum;
import com.adamjwright.bug_tracker.enums.TemplateLayoutEnum;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginPage extends BaseController {

    // The login page which is the first page rendered when visiting the app
    @GetMapping("/login")
	public String renderLogin() throws IOException {
		return getMarkupString(TemplateBodyEnum.LOGIN, TemplateLayoutEnum.LOGIN);
	}
}
