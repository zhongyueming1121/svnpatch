package com.ymz.build;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * maven打包
 *
 * @author: ymz
 * @date: 2021-08-24 23:19
 **/
@Slf4j
public class MavenBuild {
    private static final List<String> PUBLISH_GOALS = Arrays.asList("clean", "package", "-Dmaven.test.skip=true");

    /**
     * maven打包
     *
     * @param codePath
     * @param mavenHome
     * @param cmd
     * @return
     */
    public boolean buildWithMaven(String codePath, String mavenHome, String cmd) {
        if (!cmd.contains("Dmaven.test.skip") && !cmd.contains("DskipTests")) {
            cmd = cmd + " -Dmaven.test.skip=true ";
        }
        if (!cmd.contains("clean")) {
            cmd = "clean " + cmd;
        }
        String javaHome = System.getenv("JAVA_HOME");
        log.info("JAVA_HOME:{}", javaHome);
        File pomFile = BuildFileUtil.searchPomFile(codePath);
        assert pomFile != null;
        log.info("pom path:{}", pomFile.getPath());
        log.info("开始maven编译war包，cmd:{}", cmd);
        return buildWithMaven(pomFile.getPath(), javaHome, mavenHome, Arrays.asList(cmd.split(" ")));
    }

    /**
     * maven打包
     *
     * @param pomPath
     * @param javaHome
     * @param mavenHome
     * @param cmd
     * @return
     */
    private boolean buildWithMaven(String pomPath, String javaHome, String mavenHome, List<String> cmd) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(pomPath));
        request.setGoals(cmd);
        request.setJavaHome(new File(javaHome));
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(mavenHome));
        try {
            InvocationResult execute = invoker.execute(request);
            if (execute.getExitCode() != 0) {
                throw new IllegalStateException("Build failed.");
            }
        } catch (MavenInvocationException e) {
            log.error("Build failed.", e);
            return false;
        }
        log.info("Build success.");
        return true;
    }
}
