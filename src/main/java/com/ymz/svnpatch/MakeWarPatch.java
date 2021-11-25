package com.ymz.svnpatch;

import com.ymz.build.BuildFileUtil;
import com.ymz.build.MavenBuild;
import com.ymz.config.ConfigModel;
import com.ymz.util.AllUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ymz
 * @date 2021/08/19 19:20
 */
@Slf4j
public class MakeWarPatch {
    /**
     * 开始打增量包
     *
     * @param config
     */
    public static void startMake(ConfigModel config) {
        String unzipWarDstDir = "";
        String cmd = config.getCmd();
        String url = config.getUrl();
        String mavenHome = config.getMavenHome();
        String user = config.getUser().getUserName();
        String pwd = AllUtils.aesDecrypt(config.getUser().getPassword());
        String codePath = AllUtils.getCodePath();
        log.info("pwd:{}", pwd);
        log.info("svn:{}", url);
        // 拉取代码和编译解压
        unzipWarDstDir = checkoutAndUnzipWar(config, cmd, url, mavenHome, user, pwd, codePath);
        log.info("unzipWarDstDir:{}", unzipWarDstDir);
        // 过滤删除代码
        filterAndDel(config, unzipWarDstDir, url, user, pwd);
        // 压缩增量包
        String zipPath = unzipWarDstDir + File.separator + StringUtils.substringAfterLast(unzipWarDstDir, File.separator) + ".zip";
        log.info("zipPath:{}", zipPath);
        AllUtils.zipFileInFolder(unzipWarDstDir, zipPath);
    }

    /**
     * 拉取代码和编译解压
     *
     * @param config
     * @param cmd
     * @param url
     * @param mavenHome
     * @param user
     * @param pwd
     * @param codePath
     * @return
     */
    public static String checkoutAndUnzipWar(ConfigModel config, String cmd, String url, String mavenHome, String user, String pwd, String codePath) {
        String unzipWarDstDir;// 本地war包
        int localWar = config.getLocalWar();
        String projectName = StringUtils.substringAfterLast(url, "/");
        String warDstPath = AllUtils.getPatchPath() + File.separator + projectName;
        if (localWar == 1) {
            String warPath = config.getMavenHome();
            File warFile = new File(warPath);
            unzipWarDstDir = unzipWarFile(warFile, new File(warDstPath));
        } else {
            boolean isMaven = StringUtils.isNotBlank(cmd) && !cmd.contains("ant");
            // 开始拉取代码
            SvnPatch svnPatch = new SvnPatch();
            String outPath = svnPatch.checkOutByVersion(config.getCheckoutVersion() + "", url, codePath, user, pwd);
            // 开始编译代码
            if (isMaven) {
                // maven
                MavenBuild.buildWithMaven(outPath, mavenHome, cmd);
            } else {
                // ant

            }
            // 解压war包
            File warFile = BuildFileUtil.foundWarFile(outPath);
            assert warFile != null;
            unzipWarDstDir = unzipWarFile(warFile, new File(warDstPath));
        }
        return unzipWarDstDir;
    }

    public static void filterAndDel(ConfigModel config, String unzipWarDstDir, String url, String user, String pwd) {
        SvnPatch svnPatch = new SvnPatch();
        log.info("目标地址:{}", unzipWarDstDir);
        String deleteFilePath = unzipWarDstDir + File.separator + "del_files.log";
        log.info("版本删除文件日志路径:{}", deleteFilePath);
        List<Integer> versions = new ArrayList<>();
        String versionStr = config.getStartVersion();
        boolean versionRange = false;
        if (StringUtils.isNotBlank(versionStr)) {
            if (versionStr.contains(",")) {
                versions = Arrays.stream(versionStr.split(",")).mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
                log.info("版本号:{}", versions.toString());
            } else if (versionStr.contains("-")) {
                String startVersion = versionStr.split("-")[0];
                String endVersion = versionStr.split("-")[1];
                log.info("版本号:{}-{}", startVersion, endVersion);
                versionRange = true;
            }
        }
        Date startDate = null;
        Date endDate = null;
        List<String> svnRepositoryHistory = svnPatch.getSvnRepositoryHistory(url, user, pwd, versions, versionRange, startDate, endDate);
        log.info("svnRepositoryHistory:{}",svnRepositoryHistory.toString());
        PatchFileFilter patchFileFilter = new PatchFileFilter(true, deleteFilePath);
        patchFileFilter.filterAndDel(svnRepositoryHistory, unzipWarDstDir);
        log.info("处理完毕，输出文件夹位置：{}", unzipWarDstDir);
    }

    /**
     * 解压war包
     *
     * @param warFile
     */
    private static String unzipWarFile(File warFile, File dstPath) {
        if (dstPath.exists() && dstPath.isDirectory()) {
            try {
                FileUtils.forceDelete(dstPath);
            } catch (IOException e) {
                log.error("del unzipWarDstPath error", e);
            }
        } else {
            dstPath.mkdir();
        }
        log.info("开始解压war包{}到{}", warFile.getPath(), dstPath.getPath());
        boolean unzipWar = AllUtils.unzipWar(warFile, dstPath.getPath());
        log.info("解压war包{}", unzipWar ? "成功" : "失败");
        return dstPath.getPath();
    }
}
