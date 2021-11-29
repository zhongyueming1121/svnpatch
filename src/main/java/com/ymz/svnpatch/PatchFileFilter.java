package com.ymz.svnpatch;

import com.ymz.util.AllUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 增量文件过滤器
 *
 * @author: ymz
 * @date: 2021-08-18 23:21
 **/
@Slf4j
public class PatchFileFilter {
    /**
     * 是否maven项目
     */
    private boolean mavenProject = false;
    private String deleteFilePath = "E:\\jh\\del_eyes.txt";
    private static String line = System.getProperty("line.separator");

    /**
     * 过滤类
     * @param mavenProject 暂时无用，备用
     * @param deleteFilePath
     */
    public PatchFileFilter(boolean mavenProject,String deleteFilePath) {
        this.mavenProject = mavenProject;
        this.deleteFilePath = deleteFilePath;
    }

    /**
     * 过滤并删除文件
     *
     * @param history
     * @param rootPath
     */
    public void filterAndDel(List<String> history, String rootPath) {
        // 转换为war包内地址(linux 斜杠)
        Set<String> fileInWarPath = transferLinuxPathInWar(history);
        log.debug("fileInWarPath:{}",fileInWarPath.toString());
        if(fileInWarPath.isEmpty()){
            return;
        }
        File rootFilePath = new File(rootPath);
        log.debug("rootFilePath:{}",rootFilePath.getPath());
        List<File> allFiles = AllUtils.getAllFiles(rootFilePath);
        String parentName = StringUtils.substringAfterLast(rootFilePath.getPath(), File.separator);
        log.debug("parentName:{}",parentName);
        for (File file : allFiles) {
            // 判断是否spring.components文件
            if(file.getPath().endsWith("spring.components") || file.getPath().endsWith("build_version.properties")
                    || file.getPath().endsWith("MANIFEST.MF")){
                log.info("保留特殊文件:{}",file.getPath());
                continue;
            }
            // 转为linux斜杠
            String pathLinux = AllUtils.replaceFileSeparatorToLinux(file.getPath());
            // 本地文件在war包内地址
            String localFilePathInWar = StringUtils.substringAfter(pathLinux,"/" + parentName);
            // 替换class为java
            localFilePathInWar = AllUtils.classToJava(localFilePathInWar);
            // 判断要不要保留
            if(!fileInWarPath.contains(localFilePathInWar)){
                FileUtils.deleteQuietly(file);
                log.debug("del: {}",localFilePathInWar);
            } else {
                log.debug("保留文件warpath:{}",localFilePathInWar);
                log.info("保留文件:{}",file.getPath());
            }
        }
        log.info("清除空文件夹");
        AllUtils.clearEmptyDir(rootFilePath);
    }

    /**
     * 转换svn地址为war包地址
     *
     * @param svnRepositoryHistory
     * @return
     */
    public Set<String> transferLinuxPathInWar(List<String> svnRepositoryHistory) {
        List<String> returnPathInWar = new ArrayList<>();
        // svn已被删除的文件，需要特殊处理
        List<String> deletes = new ArrayList<>();
        for (int i = 0; i < svnRepositoryHistory.size(); i++) {
            String svnInfo = svnRepositoryHistory.get(i);
            String svnInfoRaw = svnRepositoryHistory.get(i);
            // 处理斜杠为linux,方便处理
            //svnInfo = AllUtils.replaceFileSeparatorToLinux(svnInfo);
            // 处理svn已被删除的代码或者文件
            handleRealDelFile(svnRepositoryHistory, deletes, i, svnInfoRaw);
            // 去除from信息
            svnInfo = replaceInfo(svnInfo);
            if (svnInfo.endsWith("/")) {
                // 最后一个斜杠结尾，说明不是文件
                continue;
            }
            String afterLast = StringUtils.substringAfterLast(svnInfo, "/");
            if (StringUtils.isBlank(afterLast) || !afterLast.contains(".")) {
                // 最后一个斜杠结尾的为空或者不包含点，说明不是文件
                continue;
            }
            String filePathInWar = svnPathToPathInWar(svnInfo);
            if (StringUtils.isNotBlank(filePathInWar)) {
                returnPathInWar.add(filePathInWar);
            }
        }
        // 将被删除的集合文件输出到文件中
        if (!deletes.isEmpty()) {
            writeListToFile(deletes);
        }
        HashSet<String> pathSet = new HashSet<>(returnPathInWar);
        return pathSet;
    }

    /**
     * 保存真正被删除的文件
     * @param svnRepositoryHistory
     * @param deletes
     * @param i
     * @param svnInfo
     */
    private void handleRealDelFile(List<String> svnRepositoryHistory, List<String> deletes, int i, String svnInfo) {
        if (svnInfo.startsWith("D /")) {
            // svn已被删除的代码或者文件，转换成A / 在后面版本中查找是否增加
            String addSvn = StringUtils.substringAfter(svnInfo, "D /");
            addSvn = "A /" + addSvn;
            boolean needDel = true;
            for (int j = i; j < svnRepositoryHistory.size(); j++) {
                String svn = svnRepositoryHistory.get(j);
                if (svn.contains(addSvn)) {
                    needDel = false;
                    break;
                }
            }
            if (needDel) {
                deletes.add(svnInfo);
            }
        }
    }

    /**
     * 处理复制或者移动后log带的原分支信息
     *
     * @param svn
     * @return
     */
    private static String replaceInfo(String svn) {
        // 处理类似这种
        //"A /00Developing/02Code/branches/cloudEyes/safedogConsole_privateV4.3.1/cloudeyes-web/src/main/resources/env/release/spring-kafka.xml
        //(from /00Developing/02Code/branches/cloudEyes/safedogConsole_privateV4.3.0/cloudeyes-web/src/main/resources/env/release/spring-kafka.xml:58651)";
        if(!svn.contains("(from ")){
            return svn;
        }
        log.debug("replaceInfo befor:{}",svn);
        svn = StringUtils.substringBefore(svn,"(from ").trim();
        log.debug("replaceInfo after:{}",svn);
        return svn;
    }

    /**
     * svn路径转换成war包内的路径
     *
     * @param svnFileInfo
     * @return
     */
    public String svnPathToPathInWar(String svnFileInfo) {
        String localPath = "";
        // 跳过pom.xml
        if (svnFileInfo.contains("pom.xml")) {
            return localPath;
        }
        // 代码
        if (svnFileInfo.contains("/src/main/java/") && svnFileInfo.endsWith(".java")) {
            localPath = "/WEB-INF/classes/" + StringUtils.substringAfter(svnFileInfo, "/java/");
            return localPath;
        } else {
            // 代码
            if (svnFileInfo.contains("/src/") && svnFileInfo.endsWith(".java")) {
                localPath = "/WEB-INF/classes/" + StringUtils.substringAfter(svnFileInfo, "/src/");
                return localPath;
            }
        }
        // 配置
        if (svnFileInfo.contains("/src/main/resources/") && !svnFileInfo.endsWith(".java")) {
            localPath = "/WEB-INF/classes/" + StringUtils.substringAfter(svnFileInfo, "/src/main/resources/");
            return localPath;
        } else {
            // 配置
            if (svnFileInfo.contains("/config/") && !svnFileInfo.endsWith(".java")) {
                localPath = "/WEB-INF/classes/" + StringUtils.substringAfter(svnFileInfo, "/config/");
                return localPath;
            }
        }
        // web资源
        if (svnFileInfo.contains("/webapp/") && !svnFileInfo.endsWith(".java")) {
            localPath = "/" + StringUtils.substringAfter(svnFileInfo, "/webapp/");
            return localPath;
        } else {
            // web资源
            if (svnFileInfo.contains("/web/") && !svnFileInfo.endsWith(".java")) {
                localPath = "/" + StringUtils.substringAfter(svnFileInfo, "/web/");
                return localPath;
            }
        }
        return localPath;
    }

    /**
     * 写文件
     *
     * @param listStr
     */
    public void writeListToFile(List<String> listStr) {
        try {
            File file = new File(deleteFilePath);
            file.createNewFile();
            FileUtils.write(file, "", false);
            for (String str : listStr) {
                FileUtils.write(file, str + line, true);
            }
        } catch (Exception e) {
            log.error("writeListToFile error", e);
        }
    }
}
