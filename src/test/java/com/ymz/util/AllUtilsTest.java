package com.ymz.util;

import org.junit.Test;

import java.io.File;
import java.util.List;

public class AllUtilsTest {

    @Test
    public void foundFile() {
        String fileName = "AntBuild.java";
        String filePath = "G:\\CODE\\svnpatch";
        List<File> files = AllUtils.foundFile(new File(filePath), fileName);
        System.out.println(files.toString());
    }
}
