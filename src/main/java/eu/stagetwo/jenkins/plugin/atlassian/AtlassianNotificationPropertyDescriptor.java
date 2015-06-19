package eu.stagetwo.jenkins.plugin.atlassian;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 */
@Extension
public final class AtlassianNotificationPropertyDescriptor extends JobPropertyDescriptor {

    public AtlassianNotificationPropertyDescriptor() {
        super(AtlassianNotificationProperty.class);
        load();
    }

    private String stashUrl;

    private String jiraUrl;

    public boolean isEnabled() {
        return !("".equals(this.stashUrl) || "".equals(this.jiraUrl));
    }

    public String getStashUrl() {
        return this.stashUrl;
    }

    public String getJiraUrl() {
        return this.jiraUrl;
    }

    public void setStashUrl(String stashUrl) {
        this.stashUrl = stashUrl;
    }

    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }

    @Override
    public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends Job> jobType) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Atlassian Notification";
    }

    @Override
    public AtlassianNotificationProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        String stashUrl = null;
        String jiraURL = null;
        // Read urls from form data
        if (formData != null && !formData.isNullObject()) {
            stashUrl = formData.getString("stashUrl");
            jiraURL = formData.getString("jiraUrl");
        }
        AtlassianNotificationProperty notificationProperty = new AtlassianNotificationProperty(stashUrl, jiraURL);

        return notificationProperty;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) {
        save();
        return true;
    }

}