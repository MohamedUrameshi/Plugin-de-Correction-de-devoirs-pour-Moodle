package fr.up5.miage.testsReport;

public class SonarqubeTestDetails {

    private String className;
    private String method;
    private String spected;
    private String result;

    public SonarqubeTestDetails(String className, String method)
    {
        this.className =className;
        this.method = method;
    }
    public SonarqubeTestDetails(String className, String method, String spected, String result) {
        this.className =className;
        this.method = method;
        this.spected = spected;
        this.result = result;
    }

    public String getClassName() {
        return className;
    }

    public String getMethod() {
        return method;
    }

    public String getSpected() {
        return spected;
    }

    public String getResult() {
        return result;
    }
}
