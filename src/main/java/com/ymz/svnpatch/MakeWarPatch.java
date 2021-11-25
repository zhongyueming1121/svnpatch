package com.ymz.svnpatch;

import com.ymz.build.AntBuild;
import com.ymz.build.BuildFileUtil;
import com.ymz.build.MavenBuild;
import com.ymz.config.ConfigModel;
import com.ymz.ui.SvnGUI;
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
    public boolean startMake(ConfigModel config, boolean full) {
        String unzipWarDstDir = "";
        String cmd = config.getCmd();
        String url = config.getUrl();
        String mavenHome = config.getMavenHome();
        String user = config.getUser().getUserName();
        String pwd = AllUtils.aesDecrypt(config.getUser().getPassword());
        String codePath = AllUtils.getCodePath();
        log.debug("pwd:{}", pwd);
        log.info("svn:{}", url);
        int localWar = config.getLocalWar();
        String projectName = StringUtils.substringAfterLast(url, "/");
        String warDstPath = AllUtils.getPatchPath() + File.separator + projectName;
        if (localWar == 1) {
            // 全量包
            if (full) {
                return true;
            }
            String warPath = config.getMavenHome();
            File warFile = new File(warPath);
            unzipWarDstDir = unzipWarFile(warFile, new File(warDstPath));
        } else {
            // 拉代码
            String checkoutPath = checkoutAndBuild(cmd, url, user, pwd, codePath);
            if (StringUtils.isBlank(checkoutPath)) {
                return false;
            }
            SvnGUI.progressBar.setValue(50);
            // 编译项目
            boolean buildProject = buildProject(cmd, mavenHome, checkoutPath);
            if(!buildProject) {
                return false;
            }
            SvnGUI.progressBar.setValue(60);
            // 查找war包
            File warFile = BuildFileUtil.searchWarFile(checkoutPath);
            if (warFile == null) {
                log.error("war包不存在");
                return false;
            }
            // 全量包
            if (full) {
                log.info("war包路径:{}", warFile.getPath());
                return true;
            }
            // 解压war包
            unzipWarDstDir = unzipWarFile(warFile, new File(warDstPath));
        }
        if(unzipWarDstDir == null){
            log.error("解压war包失败");
            return false;
        } else {
            SvnGUI.progressBar.setValue(70);
        }
        log.info("unzipWarDstDir:{}", unzipWarDstDir);
        // 过滤删除代码
        filterClass(config, unzipWarDstDir, url, user, pwd);
        SvnGUI.progressBar.setValue(80);
        // 压缩增量包
        String zipPath = unzipWarDstDir + File.separator + StringUtils.substringAfterLast(unzipWarDstDir, File.separator) + ".zip";
        log.info("zipPath:{}", zipPath);
        File zipFile = AllUtils.zipFileInFolder(unzipWarDstDir, zipPath);
        SvnGUI.progressBar.setValue(90);
        return zipFile != null;
    }

    /**
     * 拉取代码和编译解压
     *
     * @param cmd
     * @param url
     * @param user
     * @param pwd
     * @return
     */
    private String checkoutAndBuild(String checkoutVersion, String cmd, String url, String user, String pwd) {
        // 开始拉取代码
        SvnPatch svnPatch = new SvnPatch();
        String checkoutPath = svnPatch.checkOutByVersion(checkoutVersion, url, user, pwd);
        return checkoutPath;
    }

    /**
     * 编译项目
     *
     * @param cmd
     * @param mavenHome
     * @param checkoutPath
     */
    private boolean buildProject(String cmd, String mavenHome, String checkoutPath) {
        boolean isMaven = StringUtils.isNotBlank(cmd) && !cmd.contains("ant");
        // 开始编译代码
        if (isMaven) {
            // maven
            return new MavenBuild().buildWithMaven(checkoutPath, mavenHome, cmd);
        } else {
            // ant
            File buildXml = BuildFileUtil.searchAntFile(checkoutPath);
            cmd = cmd.replaceFirst("ant", "").trim();
            String[] split = cmd.split(" ");
            for (String target : split) {
                return new AntBuild().executeAntBuild(buildXml, target);
            }
        }
        return false;
    }

    private void filterClass(ConfigModel config, String unzipWarDstDir, String url, String user, String pwd) {
        SvnPatch svnPatch = new SvnPatch();
        log.info("目标地址:{}", unzipWarDstDir);
        String deleteFilePath = unzipWarDstDir + File.separator + "del_files.log";
        log.info("体检记录中删除的文件记录:{}", deleteFilePath);
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
        Date startDate = (config.getStartDate() == null || config.getStartDate() == 1) ? null : new Date(config.getStartDate());
        Date endDate = (config.getEndDate() == null || config.getEndDate() == 1) ? null : new Date(config.getEndDate());
        List<String> svnRepositoryHistory = svnPatch.getSvnRepositoryHistory(url, user, pwd, versions, versionRange, startDate, endDate);
        log.info("svnRepositoryHistory:{}", svnRepositoryHistory.toString());
        PatchFileFilter patchFileFilter = new PatchFileFilter(true, deleteFilePath);
        patchFileFilter.filterAndDel(svnRepositoryHistory, unzipWarDstDir);
        log.info("处理完毕，输出文件夹位置：{}", unzipWarDstDir);
    }

    /**
     * 解压war包
     *
     * @param warFile
     */
    private String unzipWarFile(File warFile, File dstPath) {
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
        return unzipWar ? dstPath.getPath(): null;
    }
}
