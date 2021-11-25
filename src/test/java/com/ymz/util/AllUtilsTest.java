package com.ymz.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AllUtilsTest {

    @Test
    public void foundFile() {
       /* String fname = "*iController.java";
        long t1 = System.currentTimeMillis();
        List<File> files = new ArrayList<>();
        AllUtils.findFiles("G:\\CODE\\cloudeyes-maven", fname, files);
        long t2 = System.currentTimeMillis();
        System.out.println(files.toString());
        System.out.println(t2 - t1);*/
    }

    @Test
    public void zip() {
        /*String srcPath = "G:\\CODE\\svnpatch";
        String dstPath = "G:\\CODE\\svnpatch\\";
        String zipPath = srcPath + File.separator + StringUtils.substringAfterLast(srcPath, File.separator) + ".zip";
        System.out.println(zipPath);
        AllUtils.zipFileInFolder(srcPath, zipPath);*/
    }
}
