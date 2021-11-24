package com.ymz.svnpatch;

import com.ymz.config.ConfigModel;
import com.ymz.util.AllUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
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

    /*public static void startMakeWar() {
        boolean isMaven = false;
        String rootPath = "E:\\jh\\cloudeyes";
        String deleteFilePath = "E:\\jh\\del_eyes.txt";
        String url = "http://172.16.31.3/svn/NF7400_CC/00Developing/02Code/branches/cloudEyes/safedogCloudEyes_privateV4.0.6-20210525";
        String user = "zhongxm";
        String pwd = "cRIFAmuRMGMoeuQfKIVL";
        int startVersion = 52849;
        int endVersion = 59054;
        Date startDate = null;
        Date endDate = null;
        SvnPatch svnPatch = new SvnPatch();
        ArrayList<Integer> versions = new ArrayList<>();
        List<String> svnRepositoryHistory = svnPatch.getSvnRepositoryHistory(url, user, pwd, versions, startVersion, endVersion, startDate, endDate);
        PatchFileFilter patchFileFilter = new PatchFileFilter(isMaven, deleteFilePath);
        patchFileFilter.filterAndDel(svnRepositoryHistory, rootPath);
        log.info("处理完毕，输出文件夹位置：{}", rootPath);
    }*/

    /**
     * 开始打增量包
     *
     * @param config
     */
    public static void startMakeWar(ConfigModel config) {
        String cmd = config.getCmd();
        boolean isMaven = StringUtils.isNotBlank(cmd) && cmd.contains("mvn");
        String rootPath = config.getTargetPath();
        log.info("目标地址:{}", rootPath);
        String deleteFilePath = rootPath + File.separator + "del_files.log";
        log.info("版本删除文件日志路径:{}", deleteFilePath);
        String url = config.getUrl();
        log.info("svn:{}", url);
        String user = config.getUser().getUserName();
        String pwd = AllUtils.aesDecrypt(config.getUser().getPassword());
        log.info("pwd:{}", pwd);
        List<Integer> versions = new ArrayList<>();
        String versionStr = config.getStartVersion();
        int startVersion = 0;
        int endVersion = 0;
        if (versionStr.contains(",")) {
            versions = Arrays.stream(versionStr.split(",")).mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
            log.info("版本号:{}", versions.toString());
        } else if (versionStr.contains("-")) {
            startVersion = Integer.parseInt(versionStr.split("-")[0]);
            endVersion = Integer.parseInt(versionStr.split("-")[1]);
            log.info("版本号:{}-{}", startVersion, endVersion);
        } else {
            startVersion = Integer.parseInt(config.getStartVersion());
            endVersion = startVersion == 0 ? Integer.parseInt(config.getEndVersion()) : startVersion;
            log.info("版本号:{}-{}", startVersion, endVersion);
        }
        Date startDate = null;
        Date endDate = null;
        SvnPatch svnPatch = new SvnPatch();
        if (1 == 1) {
            log.info("处理完毕，输出文件夹位置：{}", "aa");
            return;
        }
        List<String> svnRepositoryHistory = svnPatch.getSvnRepositoryHistory(url, user, pwd, versions, startVersion, endVersion, startDate, endDate);
        PatchFileFilter patchFileFilter = new PatchFileFilter(isMaven, deleteFilePath);
        patchFileFilter.filterAndDel(svnRepositoryHistory, rootPath);
        log.info("处理完毕，输出文件夹位置：{}", rootPath);
    }
}
