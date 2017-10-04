package org.thoughtslive.jenkins.plugins.hubot.util;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import hudson.Util;
import hudson.model.Cause;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;

import hudson.EnvVars;
import retrofit2.Response;

/**
 * Common utility functions.
 * 
 * @author Naresh Rayapati
 *
 */
public class Common {

  /**
   * Attaches the "/" at end of given url.
   * 
   * @param url url as a string.
   * @return url which ends with "/"
   */
  public static String sanitizeURL(String url) {
    if (!url.endsWith("/")) {
      url = url + "/";
    }
    return url;
  }

  /**
   * Write a message to the given print stream.
   * 
   * @param logger {@link PrintStream}
   * @param message to log.
   */
  public static void log(final PrintStream logger, final Object message) {
    if (logger != null) {
      logger.println(message);
    }
  }

  public static String getJobName(final EnvVars envVars) {
    return envVars.get("JOB_NAME");
  }

  /**
   * Returns build number from the given Environemnt Vars.
   * 
   * @param logger {@link PrintStream}
   * @param envVars {@link EnvVars}
   * @return build number of current job.
   */
  public static String getBuildNumber(final PrintStream logger, final EnvVars envVars) {
    String answer = envVars.get("BUILD_NUMBER");
    if (answer == null) {
      log(logger, "No BUILD_NUMBER!");
      return "1";
    }
    return answer;
  }

  /**
   * Converts Retrofit's {@link Response} to {@link ResponseData}
   * 
   * @param response instance of {@link Response}
   * @return an instance of {@link ResponseData}
   * @throws IOException
   */
  public static <T> ResponseData<T> parseResponse(final Response<T> response) throws IOException {
    final ResponseData<T> resData = new ResponseData<T>();
    resData.setSuccessful(response.isSuccessful());
    resData.setCode(response.code());
    resData.setMessage(response.message());
    if (!response.isSuccessful()) {
      final String errorMessage = response.errorBody().string();
      resData.setError(errorMessage);
    } else {
      resData.setData(response.body());
    }
    return resData;
  }

  /**
   * Builds error response from the given exception.
   * 
   * @param e instance of {@link Exception}
   * @return an instance of {@link ResponseData}
   */
  public static <T> ResponseData<T> buildErrorResponse(final Exception e) {
    final ResponseData<T> resData = new ResponseData<T>();
    final String errorMessage = getRootCause(e).getMessage();
    resData.setSuccessful(false);
    resData.setCode(-1);
    resData.setError(errorMessage);
    e.printStackTrace();
    return resData;
  }

  /**
   * Returns actual Cause from the given exception.
   * 
   * @param throwable
   * @return {@link Throwable}
   */
  public static Throwable getRootCause(Throwable throwable) {
    if (throwable.getCause() != null)
      return getRootCause(throwable.getCause());
    return throwable;
  }

  /**
   * Return the current build user.
   *
   * @param causes build causes.
   * @return user name.
   */
  public static String prepareBuildUser(List<Cause> causes, EnvVars envVars) {
    // if this is a PR build from the multibranch plugin then we have the CHANGE_AUTHOR env var set
    if (Util.fixEmpty(envVars.get("CHANGE_AUTHOR")) != null) {
      return envVars.get("CHANGE_AUTHOR");
    }
    String buildUser = "anonymous";
    if (causes != null && causes.size() > 0) {
      if (causes.get(0) instanceof Cause.UserIdCause) {
        buildUser = ((Cause.UserIdCause) causes.get(0)).getUserName();
      } else if (causes.get(0) instanceof Cause.UpstreamCause) {
        List<Cause> upstreamCauses = ((Cause.UpstreamCause) causes.get(0)).getUpstreamCauses();
        prepareBuildUser(upstreamCauses, envVars);
      }
    }
    return buildUser;
  }
}
