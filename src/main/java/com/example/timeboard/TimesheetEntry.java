package com.example.timeboard;

public class TimesheetEntry {

    private String day;
    private String project;
    private int hours;

    public TimesheetEntry(String day, String project, int hours) {
        this.day = day;
        this.project = project;
        this.hours = hours;
    }

    public String getDay() {
        return day;
    }

    public String getProject() {
        return project;
    }

    public int getHours() {
        return hours;
    }
}
