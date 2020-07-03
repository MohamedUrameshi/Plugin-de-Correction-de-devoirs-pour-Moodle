package fr.up5.miage.testsReport;

public class SonarqubeTestResults {

    private int projectId;
    private int run;
    private int failure;
    private int error;
    private int skypped;


    public SonarqubeTestResults(int run, int failure, int error, int skypped) {
        this.run = run;
        this.failure = failure;
        this.error = error;
        this.skypped = skypped;
    }
    public int getProjectId() {
        return projectId;
    }

    public int getRun() {
        return run;
    }

    public int getFailure() {
        return failure;
    }

    public int getError() {
        return error;
    }

    public int getSkypped() {
        return skypped;
    }
}
