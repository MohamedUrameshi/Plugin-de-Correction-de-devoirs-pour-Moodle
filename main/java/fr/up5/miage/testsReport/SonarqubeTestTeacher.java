package fr.up5.miage.testsReport;

public class SonarqubeTestTeacher {
    private int moduleID;
    private String sourcePath;
    private String method;
    private String score;

    public SonarqubeTestTeacher(int moduleID, String sourcePath, String method) {
        this.moduleID = moduleID;
        this.sourcePath = sourcePath;
        this.method = method;
    }

    public int getModuleID() {
        return moduleID;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getMethod() {
        return method;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
