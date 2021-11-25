package com.ymz.build;

import junit.framework.TestCase;

import java.io.File;

/**
 * @author ymz
 * @date 2021/11/25 20:56
 */
public class AntBuildTest extends TestCase {

    public void testExecuteAntBuild() {
        File buildFile = new File("G:\\Code\\test\\build.xml");
        String targetName = "release-test";
        AntBuild.executeAntBuild(buildFile,targetName);
    }
}