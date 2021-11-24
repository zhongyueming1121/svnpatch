package com.ymz.build;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * @author: w3sun
 * @date: 2018/12/18 20:44
 * @description:
 */
public class MavenInvokerBuilder {
  private static final List<String> PUBLISH_GOALS = Arrays.asList("clean", "package",
      "-DskipTests");
  private StringBuilder output = new StringBuilder();
  ;
  /**
   * define a field for the Invoker instance.
   **/
  private final Invoker invoker;

  /**
   * now, instantiate the invoker in the class constructor
   **/
  public MavenInvokerBuilder(File localRepositoryDir) {
    this.invoker = new DefaultInvoker();
    this.invoker.setLocalRepositoryDirectory(localRepositoryDir);
  }

  /**
   * this method will be called repeatedly, and fire off new builds...
   */
  public void build() throws MavenInvocationException {
    InvocationRequest request = new DefaultInvocationRequest();
    request.setGoals(PUBLISH_GOALS);
    setOutput(request);
    InvocationResult result = this.invoker.execute(request);
    if (result.getExitCode() != 0) {
      if (result.getExecutionException() != null) {
        throw new MavenInvocationException("Verify the pom.xml error!",
            result.getExecutionException());
      }
    }
  }

  /**
   * Set output handler by InvocationRequest.
   *
   * @param request InvocationRequest
   */
  private void setOutput(InvocationRequest request) {
    request.setOutputHandler(new InvocationOutputHandler() {
      @Override
      public void consumeLine(String line) {
        output.append(line)
            .append(System.lineSeparator());
      }
    });
  }

  /**
   * Set output handler  by Invoker.
   *
   * @param invoker Invoker
   */
  private void setOutput(Invoker invoker) {
    invoker.setOutputHandler(new InvocationOutputHandler() {
      @Override
      public void consumeLine(String line) {
        output.append(line)
            .append(System.lineSeparator());
      }
    });
  }

  public String getOutput() {
    return output.toString();
  }

  public static void main(String[] args) {
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(new File("G:\\CODE\\TSvnPwd4java-master哈哈哈\\pom.xml"));
    request.setGoals(Collections.singletonList("package"));
    request.setJavaHome(new File("C:\\Program Files\\Java\\jdk1.8.0_271\\"));
    Invoker invoker = new DefaultInvoker();
    invoker.setMavenHome(new File("D:\\apache-maven-3.6.3"));
    try {
      invoker.execute(request);
    } catch (MavenInvocationException e) {
      e.printStackTrace();
    }
  }
}