package com.ymz.build;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * maven打包
 *
 * @author: ymz
 * @date: 2021-08-24 23:19
 **/
@Slf4j
public class MavenBuild {

    /**
     * maven打包
     * @param codePath
     * @param mavenHome
     * @param cmd
     * @return
     */
    public static boolean buildWithMaven(String codePath, String mavenHome, String cmd) {
        String javaHome = System.getenv("JAVA_HOME");
        log.info("JAVA_HOME:{}", javaHome);
        File pomFile = BuildFileUtil.foundPomFile(codePath);
        assert pomFile != null;
        log.info("pom path:{}", pomFile.getPath());
        return buildWithMaven(pomFile.getPath(), javaHome, mavenHome, cmd);
    }

    /**
     * maven打包
     * @param pomPath
     * @param javaHome
     * @param mavenHome
     * @param cmd
     * @return
     */
    private static boolean buildWithMaven(String pomPath, String javaHome, String mavenHome, String cmd) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(pomPath));
        request.setGoals(Collections.singletonList(cmd));
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
