package com.ymz.build;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;

/**
 * ant方式打包
 *
 * @author: ymz
 * @date: 2021-08-24 23:19
 **/
@Slf4j
public class AntBuild {
    /**
     * 执行Ant打包任务
     *
     * @param buildFile buildFile路径
     * @param targetName target名称
     * @return
     */
    public static boolean executeAntBuild(File buildFile, String targetName) {
        Project project = new Project();
        if (StringUtils.isBlank(targetName)) {
            log.error("Ant targetName不能为空");
            return false;
        }
        try {
            DefaultLogger consoleLogger = new DefaultLogger();
            consoleLogger.setErrorPrintStream(System.err);
            consoleLogger.setOutputPrintStream(System.out);
            consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
            project.addBuildListener(consoleLogger);
            project.fireBuildStarted();
            project.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            helper.parse(project, buildFile);
            project.executeTarget(targetName);
            project.fireBuildFinished(null);
            return true;
        } catch (BuildException e) {
            project.fireBuildFinished(e);
            log.error("Ant执行异常:{}" + e.getMessage(), e);
        }
        return false;
    }
}
