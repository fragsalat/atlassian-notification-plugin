package eu.stagetwo.jenkins.plugin;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.model.listeners.RunListener;

/**
 * Class to listen ob build events
 */
@Extension
@SuppressWarnings("rawtypes")
public class BuildListener extends RunListener<Run> {

    /**
     * Initialize listener
     */
    public BuildListener() {
        super(Run.class);
    }

    /**
     * Handle start phase of builds
     * @param r
     * @param listener
     */
    @Override
    public void onStarted(Run r, TaskListener listener) {
        Phase.STARTED.handle(r, listener);
    }

    /**
     * Handle building phase
     *
     * @param r
     * @param listener
     */
    @Override
    public void onCompleted(Run r, TaskListener listener) {
        Phase.COMPLETED.handle(r, listener);
    }

    /**
     * Handle final build phase
     *
     * @param r
     */
    @Override
    public void onFinalized(Run r) {
        Phase.FINALIZED.handle(r, TaskListener.NULL);
    }

}