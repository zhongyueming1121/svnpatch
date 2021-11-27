package com.ymz.build;

import junit.framework.TestCase;

import java.io.File;

/**
 * @author ymz
 * @date 2021/11/25 20:56
 */
public class AntBuildTest extends TestCase {

    public void testExecuteAntBuild() {
        File buildFile = new File("F:\\code\\build.xml");
        String targetName = "release";
        new AntBuild().executeAntBuild(buildFile,targetName);
    }
}