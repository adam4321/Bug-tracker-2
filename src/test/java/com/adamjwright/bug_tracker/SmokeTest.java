/*********************************************************************
**  Smoketest that makes sure that all of the controllers are loading
*********************************************************************/

package com.adamjwright.bug_tracker;

import static org.assertj.core.api.Assertions.assertThat;

import com.adamjwright.bug_tracker.controllers.AddBug;
import com.adamjwright.bug_tracker.controllers.AddCompany;
import com.adamjwright.bug_tracker.controllers.AddProject;
import com.adamjwright.bug_tracker.controllers.Admin;
import com.adamjwright.bug_tracker.controllers.AllBugs;
import com.adamjwright.bug_tracker.controllers.Companies;
import com.adamjwright.bug_tracker.controllers.EditBug;
import com.adamjwright.bug_tracker.controllers.EditCompany;
import com.adamjwright.bug_tracker.controllers.EditProgrammer;
import com.adamjwright.bug_tracker.controllers.EditProject;
import com.adamjwright.bug_tracker.controllers.ErrorPage;
import com.adamjwright.bug_tracker.controllers.LoginPage;
import com.adamjwright.bug_tracker.controllers.Programmers;
import com.adamjwright.bug_tracker.controllers.Projects;
import com.adamjwright.bug_tracker.controllers.UnauthorizedPage;
import com.adamjwright.bug_tracker.controllers.YourBugs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SmokeTest {
    @Autowired
    private AddBug addBugController;

    @Autowired
    private AddCompany addCompanyController;

    @Autowired
    private AddProject addProjectController;

    @Autowired
    private Admin adminController;

    @Autowired
    private AllBugs allBugsController;

    @Autowired
    private Companies companiesController;

    @Autowired
    private EditBug editBugController;

    @Autowired
    private EditCompany editCompanyController;

    @Autowired
    private EditProgrammer editProgrammerController;

    @Autowired
    private EditProject editProjectController;

    @Autowired
    private ErrorPage errorPageController;

    @Autowired
	private LoginPage loginController;

    @Autowired
    private Programmers programmersController;

    @Autowired
    private Projects projectsController;

    @Autowired
	private UnauthorizedPage unauthorizedController;

    @Autowired
    private YourBugs yourBugsController;

	@Test
	public void contextLoads() throws Exception {
        assertThat(addBugController).isNotNull();
        assertThat(addCompanyController).isNotNull();
        assertThat(addProjectController).isNotNull();
        assertThat(adminController).isNotNull();
        assertThat(allBugsController).isNotNull();
        assertThat(companiesController).isNotNull();
        assertThat(editBugController).isNotNull();
        assertThat(editCompanyController).isNotNull();
        assertThat(editProgrammerController).isNotNull();
        assertThat(editProjectController).isNotNull();
        assertThat(errorPageController).isNotNull();
		assertThat(loginController).isNotNull();
        assertThat(programmersController).isNotNull();
        assertThat(projectsController).isNotNull();
        assertThat(unauthorizedController).isNotNull();
        assertThat(yourBugsController).isNotNull();
	}
}
