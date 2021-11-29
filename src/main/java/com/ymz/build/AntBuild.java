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
    public boolean executeAntBuild(File buildFile, String targetName) {
        Project project = new Project();
        if (StringUtils.isBlank(targetName)) {
            log.error("Ant targetName不能为空");
            return false;
        }
        try {
            log.info("开始ant编译war包：{}",targetName);
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
            log.info("ant编译war包成功");
            return true;
        } catch (BuildException e) {
            String errorMsg = e.getMessage();
            log.error("Ant执行异常:{}", errorMsg, e);
            if(errorMsg!=null && errorMsg.contains("Unable to find a javac compiler")){
                String javaHome = System.getProperty("java.home");
                log.error("=========================================");
                log.error("请尝试将jdk目录下lib中的 tools.jar 复制到jre目录下/lib/ext目录中.");
                log.error("本机可能的jre目录：{}",javaHome);
                log.error("=========================================");
            }
            project.fireBuildFinished(e);
        }
        return false;
    }
}
