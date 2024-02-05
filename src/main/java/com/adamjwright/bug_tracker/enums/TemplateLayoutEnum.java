package com.adamjwright.bug_tracker.enums;

public enum TemplateLayoutEnum {
    MAIN("main"),
    LOGIN("login");

    private String template;

    TemplateLayoutEnum(String template) {
        this.template = template;
    }

    public String getName() {
        return this.template;
    }
}
