/******************************************************************************
**  Tests that routes contain the correct title and css file
******************************************************************************/

package com.adamjwright.bug_tracker;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class HttpRequestTests {

	@Autowired
	private MockMvc mockMvc;

    @Test
	public void addBugTemplateString() throws Exception {
		this.mockMvc.perform(get("/unauthorized")).andDo(print()).andExpect(status().isOk())
			.andExpect(content().string(containsString("<title>Bug Tracker</title>\r\n"
            + "<link type=\"text/css\" rel=\"stylesheet\" href=\"css/login-page.css\">\r\n"
            + "<link type=\"text/css\" rel=\"stylesheet\" href=\"css/unauthorized.css\">")));
	}

	@Test
	public void loginTemplateString() throws Exception {
		this.mockMvc.perform(get("/login")).andDo(print()).andExpect(status().isOk())
			.andExpect(content().string(containsString("<title>Bug Tracker</title>\r\n"
            + "<link type=\"text/css\" rel=\"stylesheet\" href=\"css/login-page.css\">")));
	}
}
