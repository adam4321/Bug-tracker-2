package com.adamjwright.bug_tracker.enums;

public enum TemplateBodyEnum {
    ADD_BUG("add-bug"),
    ADD_COMPANY("add-company"),
    ADD_PROJECT("add-project"),
    ADMIN("admin"),
    ALL_BUGS("all-bugs"),
    COMPANIES("companies"),
    EDIT_COMPANY("edit-company"),
    LOGIN("login-page"),
    EDIT_PROGRAMMER("edit-programmer"),
    EDIT_PROJECT("edit-project"),
    NOT_FOUND("not-found"),
    SERVER_ERROR("server-error"),
    PROGRAMMERS("programmers"),
    PROJECTS("projects"),
    UNAUTHORIZED("unauthorized-page"),
    YOUR_BUGS("your-bugs");

    private String template;

    TemplateBodyEnum(String template) {
        this.template = template;
    }

    public String getName() {
        return this.template;
    }
}
