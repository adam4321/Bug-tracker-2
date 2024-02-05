/******************************************************************************
**  /unauthorized 401 http code route
******************************************************************************/

package com.adamjwright.bug_tracker.controllers;

import java.io.IOException;

import com.adamjwright.bug_tracker.enums.TemplateBodyEnum;
import com.adamjwright.bug_tracker.enums.TemplateLayoutEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UnauthorizedPage extends BaseController {

    // The login page which is the first page rendered when visiting the app
    @GetMapping("/unauthorized")
	public String renderUnauthorized() throws IOException {
		return getMarkupString(TemplateBodyEnum.UNAUTHORIZED, TemplateLayoutEnum.LOGIN);
	}
}
