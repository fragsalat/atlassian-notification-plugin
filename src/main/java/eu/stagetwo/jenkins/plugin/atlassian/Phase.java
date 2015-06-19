package eu.stagetwo.jenkins.plugin.atlassian;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stagetwo.jenkins.plugin.atlassian.model.BuildState;
import eu.stagetwo.jenkins.plugin.atlassian.model.JobState;
import eu.stagetwo.jenkins.plugin.atlassian.model.ScmState;
import hudson.EnvVars;
import hudson.model.*;
import jenkins.model.Jenkins;
import java.io.IOException;

/**
 * Send notifications to atlassian product instances for build phases
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public enum Phase {
    STARTED, COMPLETED, FINALIZED;

    /**
     * Handle the phase events
     *
     * @param run
     * @param listener
     */
    @SuppressWarnings( "CastToConcreteClass" )
    public void handle(Run run, TaskListener listener) {
        // Get config property for
        AtlassianNotificationProperty property = (AtlassianNotificationProperty) run.getParent().getProperty(AtlassianNotificationProperty.class);
        if (property == null) {
            return;
        }
        // Check if either jira or stash url is set
        if (property.isEnabled()) {
            try {
                // Prepare json data
                JobState jobState = buildJobState(run.getParent(), run, listener);
                EnvVars environment = run.getEnvironment(listener);
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                byte[] jsonData = gson.toJson(jobState).getBytes("UTF-8");
                // Notify stash instance
                if (property.getStashUrl() != null) {
                    listener.getLogger().println(String.format("Notifying Stash '%s'", property.getStashUrl()));
                    String expandedUrl = environment.expand(property.getStashUrl());
                    Request.send(expandedUrl, jsonData, 10000);
                }
                // Notify jira instance
                if (property.getJiraUrl() != null) {
                    listener.getLogger().println(String.format("Notifying JIRA '%s'", property.getJiraUrl()));
                    String expandedUrl = environment.expand(property.getJiraUrl());
                    Request.send(expandedUrl, jsonData, 10000);
                }
            }
            catch (Throwable error) {
                error.printStackTrace(
                    listener.error(
                        String.format(
                            "Failed to notify endpoint '%s' or '%s'",
                            property.getStashUrl(),
                            property.getJiraUrl()
                        )
                    )
                );
            }
        }
    }

    /**
     * Prepare a object containing job and build status
     *
     * @param job
     * @param run
     * @param listener
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private JobState buildJobState(Job job, Run run, TaskListener listener) throws IOException, InterruptedException {
        Jenkins            jenkins      = Jenkins.getInstance();
        String             rootUrl      = jenkins.getRootUrl();
        JobState           jobState     = new JobState();
        BuildState buildState   = new BuildState();
        ScmState scmState     = new ScmState();
        Result             result       = run.getResult();
        ParametersAction   paramsAction = run.getAction(ParametersAction.class);
        EnvVars            environment  = run.getEnvironment(listener);

        jobState.setName(job.getName());
        jobState.setUrl(job.getUrl());
        jobState.setBuild(buildState);

        buildState.setNumber(run.number);
        buildState.setUrl(run.getUrl());
        buildState.setPhase(this);
        buildState.setScm(scmState);

        if (result != null) {
            buildState.setStatus(result.toString());
        }
        if (rootUrl != null) {
            jobState.setFullUrl(rootUrl + job.getUrl());
            buildState.setFullUrl(rootUrl + run.getUrl());
        }

        buildState.updateArtifacts(job, run);
        // Set build parameters
        if (paramsAction != null) {
            EnvVars env = new EnvVars();
            for (ParameterValue value : paramsAction.getParameters()){
                if (!value.isSensitive()) {
                    value.buildEnvironment(run, env);
                }
            }
            buildState.setParameters(env);
        }
        // Set version control info
        if (environment.get("GIT_URL") != null) {
            scmState.setUrl(environment.get("GIT_URL"));
        }
        if (environment.get("GIT_BRANCH") != null) {
            scmState.setBranch(environment.get("GIT_BRANCH"));
        }
        if (environment.get("GIT_COMMIT") != null) {
            scmState.setCommit(environment.get("GIT_COMMIT"));
        }

        return jobState;
    }
}
