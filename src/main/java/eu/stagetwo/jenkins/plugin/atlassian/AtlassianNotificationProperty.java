/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.stagetwo.jenkins.plugin.atlassian;

import hudson.model.AbstractProject;
import hudson.model.JobProperty;
import org.kohsuke.stapler.DataBoundConstructor;

public class AtlassianNotificationProperty extends JobProperty<AbstractProject<?, ?>> {

    public final String stashUrl;

    public final String jiraUrl;

    @DataBoundConstructor
    public AtlassianNotificationProperty(String stashUrl, String jiraUrl) {
        this.stashUrl = stashUrl;
        this.jiraUrl = jiraUrl;
    }

    public String getStashUrl() {
        return this.stashUrl;
    }

    public String getJiraUrl() {
        return this.jiraUrl;
    }

    public boolean isEnabled() {
        return !("".equals(this.stashUrl) || "".equals(this.jiraUrl));
    }

    @SuppressWarnings( "CastToConcreteClass" )
    @Override
    public AtlassianNotificationPropertyDescriptor getDescriptor() {
        return (AtlassianNotificationPropertyDescriptor) super.getDescriptor();
    }
}