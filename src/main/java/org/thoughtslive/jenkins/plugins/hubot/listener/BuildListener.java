package org.thoughtslive.jenkins.plugins.hubot.listener;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.model.listeners.RunListener;

import java.io.PrintStream;

import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;
import org.thoughtslive.jenkins.plugins.hubot.util.Common;
import org.thoughtslive.jenkins.plugins.hubot.util.HubotConfig;

import static org.thoughtslive.jenkins.plugins.hubot.util.Common.log;

/**
 * Send failed job notifications
 */
@Extension
public class BuildListener extends RunListener<Run> {

  public BuildListener() {
  }

  /**
   * If enabled by the `NOTIFY_FAILED_BUILDS=true` environment variable we will send notifications when jobs fail
   *
   * We will used the top level Jenkins folder that folks use to organise their jobs as the channel name to send messages
   *
   * @param run
   * @param listener
   */
  @Override
  public synchronized void onCompleted(Run run, TaskListener listener) {
    PrintStream logger = listener.getLogger();
    Result result = run.getResult();

    if (result != null && result == Result.FAILURE) {
      HubotConfig config;
      EnvVars envVars;
      try {
        envVars = run.getEnvironment(listener);
        config = new HubotConfig(envVars);
      } catch (Exception e) {
        log(logger, "Hubot: Error sending job failed message." + e.getMessage());
        return;
      }

      if (config.isNotifyFailedBuild()) {
        String buildUser = Common.prepareBuildUser(run.getCauses(), envVars);

        // if this is a branch plugin CI build the we should have a change URL
        String changeURL = Util.fixEmpty(envVars.get("CHANGE_URL")) != null ? envVars.get("CHANGE_URL") : null;

        String room = config.getRoom();
        // if HUBOT_AUTO_ROOM set then use top level Jenkins folder name for the channel to send teh failed messages to
        if (config.isAutoRoom()){
          // lets get the top level folder as this could be a organisation / folder / multi branch or normal job
          room = config.getChannelPrefix() + "-" + getTopLevelFolder(run.getParent());
        }

        String message = run.getFullDisplayName() + " Job failed" + "\n\n" + "Job: "
                + config.getBuildUrl();

        if (changeURL != null){
          message = message + "\n" + "Change URL: " + changeURL;
        }
        message = message  + "\n" + "User: " + buildUser;
        HubotService svc = new HubotService(config.getUrl());
        svc.sendMessage(room, message);

      }
    }
  }

  private String getTopLevelFolder(Item item){
    if (item.getParent() instanceof Hudson) {
      return item.getName();
    }

    ItemGroup<? extends Item> grandParent = item.getParent();
    if (grandParent instanceof Item) {
      return getTopLevelFolder((Item) grandParent);
    }

    return item.getName();
  }
}