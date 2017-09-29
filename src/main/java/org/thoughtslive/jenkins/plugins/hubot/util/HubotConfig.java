package org.thoughtslive.jenkins.plugins.hubot.util;

import hudson.EnvVars;
import hudson.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.thoughtslive.jenkins.plugins.hubot.steps.BasicHubotStep;

import java.io.Serializable;

@ToString
public class HubotConfig implements Serializable{

  @Getter
  @Setter
  private String url;

  @Getter
  @Setter
  private String room;

  @Getter
  @Setter
  private Boolean failOnError;

  @Getter
  @Setter
  private String message;

  @Getter
  @Setter
  private String error;

  @Getter
  @Setter
  // HUBOT_NOTIFY_FAILED_BUILDS sends messages automatically when builds fail
  private boolean notifyFailedBuild;

  @Getter
  @Setter
  // if multibranch and HUBOT_AUTO_ROOM=true then messages will be sent to a room name that matches the github organisation
  private boolean autoRoom;

  @Getter
  @Setter
  // User that triggered the build
  private String buildUser;

  @Getter
  @Setter
  // URL of the build job
  private String buildUrl;

  @Getter
  @Setter
  // Used in combination with HUBOT_AUTO_ROOM
  private String channelPrefix = "bot";

  public HubotConfig (EnvVars envVars) throws HubotConfigException {
    this (envVars, null);
  }
  /**
   * Returns configuration for hubot using either environment variables or overriding step parameters
   *
   * @param envVars Global Jenkins environment variables
   * @param step If
   * @throws HubotConfigException
   */
  public HubotConfig (EnvVars envVars, BasicHubotStep step) throws HubotConfigException {

    if (Util.fixEmpty(envVars.get("HUBOT_NOTIFY_FAILED_BUILDS")) != null) {
      notifyFailedBuild = Boolean.parseBoolean(envVars.get("HUBOT_NOTIFY_FAILED_BUILDS"));
    } else if (step == null){
      // if this isn't a step and we're not after automatic failed build messages lets return straight away
      return;
    }

    if (step != null && Util.fixEmpty(step.getUrl()) != null){
      url = step.getUrl();
    } else if (Util.fixEmpty(envVars.get("HUBOT_URL")) != null) {
      url = envVars.get("HUBOT_URL");
    } else{
      // if that's null then error
      throw new HubotConfigException("Hubot: HUBOT_URL or step parameter equivalent is empty or null.");
    }

    if (step != null && Util.fixEmpty(step.getRoom()) != null ){
      room = step.getRoom();
    } else if (Util.fixEmpty(envVars.get("HUBOT_DEFAULT_ROOM")) != null) {
      room = envVars.get("HUBOT_DEFAULT_ROOM");
    } else{
      // if that's null then error
      throw new HubotConfigException("Hubot: HUBOT_DEFAULT_ROOM or step parameter equivalent is empty or null.");
    }

    if (step != null){
      if (Util.fixEmpty(step.getMessage()) != null) {
        message = step.getMessage();
      } else{
        // if that's null then error
        throw new HubotConfigException("Hubot: Message is empty or null.");
      }
    }

    buildUrl = envVars.get("BUILD_URL");

    if (Util.fixEmpty(envVars.get("HUBOT_AUTO_ROOM")) != null) {
      autoRoom = Boolean.parseBoolean(envVars.get("HUBOT_AUTO_ROOM"));
    }

    if (Util.fixEmpty(envVars.get("HUBOT_CHANNEL_PREFIX")) != null) {
      channelPrefix = envVars.get("HUBOT_CHANNEL_PREFIX");
    }
  }
}
