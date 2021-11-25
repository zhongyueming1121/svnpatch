package com.ymz.build;

import com.ymz.util.AllUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 打包相关配置
 *
 * @author: ymz
 * @date: 2021-08-24 23:20
 **/
@Slf4j
public class BuildFileUtil {

    /**
     * 在文件夹中查找编译pom
     *
     * @param rootPath
     * @return
     */
    public static File searchPomFile(String rootPath) {
        List<File> files = new ArrayList<>();
        AllUtils.findFiles(rootPath, "pom.xml", files);
        for (File file : files) {
            try {
                String fileXml = FileUtils.readFileToString(file, "utf-8");
                // 暂时假定包含模块定义的为可以运行编译的pom
                if (fileXml.contains("<modules>")) {
                    return file;
                }
            } catch (IOException e) {
                log.error("读取pom文件错误", e);
            }
        }
        return files.get(0);
    }

    /**
     * 在文件夹中查找ant build.xml
     *
     * @param rootPath
     * @return
     */
    public static File searchAntFile(String rootPath) {
        List<File> files = new ArrayList<>();
        AllUtils.findFiles(rootPath, "build.xml", files);
        for (File file : files) {
            try {
                String fileXml = FileUtils.readFileToString(file, "utf-8");
                // 暂时假定包含<target>为ant build.xml文件
                if (fileXml.contains("</target>")) {
                    return file;
                }
            } catch (IOException e) {
                log.error("读取build文件错误", e);
            }
        }
        return files.get(0);
    }

    /**
     * 在文件夹中查找war包
     *
     * @param rootPath
     * @return
     */
    public static File searchWarFile(String rootPath) {
        List<File> files = new ArrayList<>();
        AllUtils.findFiles(rootPath, "*.war", files);
        return files.get(0);
    }


}
