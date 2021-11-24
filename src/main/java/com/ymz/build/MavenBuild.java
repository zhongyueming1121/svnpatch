package com.ymz.build;

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
public class MavenBuild {
    public static void main(String[] args) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("G:\\CODE\\TSvnPwd4java-master哈哈哈\\pom.xml"));
        request.setGoals(Collections.singletonList("package -Dmaven.test.skip=true"));
        String javaHome = System.getenv("JAVA_HOME");
        System.out.println(javaHome);
        request.setJavaHome(new File("C:\\Program Files\\Java\\jdk1.8.0_271\\"));
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File("D:\\apache-maven-3.6.3"));
        //invoker.setLogger((new PrintStreamLogger(System.out,InvokerLogger.INFO)));


        invoker.setLogger(new PrintStreamLogger(System.err, InvokerLogger.INFO) {

        });
        invoker.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String s) throws IOException {
                System.out.println(new String(s.getBytes("utf-8"),"utf-8"));
            }
        });

        try {
            invoker.execute(request);
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }

/*
        try {
            if (invoker.execute(request).getExitCode() == 0) {
                System.out.println("success");
            } else {
                System.err.println("error");
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }*/
    }
}
