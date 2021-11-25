package com.ymz.svnpatch;

import com.ymz.config.ConfigModel;
import com.ymz.svnauth.Program;
import com.ymz.util.AllUtils;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;

/**
 * @author ymz
 * @date 2021/11/25 10:34
 */
public class MakeWarPatchTest extends TestCase {

    public void testStartMake() {

    }

    public void testCheckoutAndUnzipWar() {
        ConfigModel config = new ConfigModel();
        config.setCheckoutVersion(63379);
        config.setLocalWar(0);
        String unzipWarDstDir = "";
        String cmd = "clean package -DskipTests";
        String url = "http://172.16.31.3/svn/NF7400_CC/00Developing/02Code/branches/test/testV4.3.0.210910-20210927";
        String mavenHome = "E:\\Program Files\\apache-maven-3.6.1";
        HashMap<String, String> svnAuth = Program.getSvnAuth();
        String user = svnAuth.get("userName");
        String pwd = svnAuth.get("password");
        String codePath = AllUtils.getCodePath();
        unzipWarDstDir = MakeWarPatch.checkoutAndUnzipWar(config,cmd,url,mavenHome,user,pwd,codePath);
        System.out.println(unzipWarDstDir);
    }

    public void testFilterAndDel() {
        ConfigModel config = new ConfigModel();
        config.setStartVersion("62866,62716");
        String url = "http://172.16.31.3/svn/NF7400_CC/00Developing/02Code/branches/test/testV4.3.0.210910-20210927";
        HashMap<String, String> svnAuth = Program.getSvnAuth();
        String user = svnAuth.get("userName");
        String pwd = svnAuth.get("password");
        String unzipWarDstDir  = "E:\\jh\\patch\\testV4.3.0.210910-20210927";
        MakeWarPatch.filterAndDel(config,unzipWarDstDir,url,user,pwd);
        // 压缩增量包
        String zipPath = unzipWarDstDir + File.separator + StringUtils.substringAfterLast(unzipWarDstDir, File.separator) + ".zip";
        AllUtils.zipFileInFolder(unzipWarDstDir, zipPath);
    }
}