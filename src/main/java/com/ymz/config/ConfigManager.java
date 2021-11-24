package com.ymz.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ymz.util.AllUtils;
import com.ymz.ui.SvnGUI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

/**
 * 配置文件管理
 *
 * @author: ymz
 * @date: 2021-08-22 20:23
 **/
@Slf4j
public class ConfigManager {
    private static String configDirName = "config";
    private static String configFileName = "config.json";

    /**
     * 读取配置
     *
     * @return
     */
    public static ConfigJsonModel loadConfig() {
        String configPath = getConfigPath();
        StringBuilder jsonBuilder = new StringBuilder();
        try {
            Files.lines(Paths.get(configPath), StandardCharsets.UTF_8).forEach(jsonBuilder::append);
        } catch (IOException e) {
            log.error("读取配置文件失败", e);
        }
        Gson gson = new Gson();
        ConfigJsonModel configJsonModel = gson.fromJson(jsonBuilder.toString(), ConfigJsonModel.class);
        if (configJsonModel == null) {
            configJsonModel = new ConfigJsonModel();
        }
        return configJsonModel;
    }

    /**
     * 保存数据到文件
     *
     * @param configJsonModel
     */
    public static void writeConfig(ConfigJsonModel configJsonModel) {
        if (configJsonModel == null) {
            return;
        }
        LinkedList<String> urlHistories = configJsonModel.getUrlHistories();
        LinkedList<ConfigUser> userHistories = configJsonModel.getUserHistories();
        LinkedList<String> cmdHistories = configJsonModel.getCmdHistories();
        LinkedList<String> mavenPathHistories = configJsonModel.getMavenHistories();
        for (; ; ) {
            if (urlHistories.size() > 10) {
                urlHistories.removeLast();
            } else {
                break;
            }
        }
        for (; ; ) {
            if (userHistories.size() > 10) {
                userHistories.removeLast();
            } else {
                break;
            }
        }
        for (; ; ) {
            if (cmdHistories.size() > 10) {
                cmdHistories.removeLast();
            } else {
                break;
            }
        }
        for (; ; ) {
            if (mavenPathHistories.size() > 10) {
                mavenPathHistories.removeLast();
            } else {
                break;
            }
        }
        configJsonModel.setUrlHistories(urlHistories);
        configJsonModel.setUserHistories(userHistories);
        configJsonModel.setCmdHistories(cmdHistories);
        configJsonModel.setMavenHistories(mavenPathHistories);
        ConfigModel lastUseConfig = configJsonModel.getLastUseConfig();
        lastUseConfig.setStartDate(null);
        lastUseConfig.setEndDate(null);
        lastUseConfig.setStartVersion("");
        lastUseConfig.setEndVersion("");
        configJsonModel.setLastUseConfig(lastUseConfig);

        String json = toPrettyFormat(configJsonModel);
        try {
            //log.info("写入配置：{}", json);
            String configPath = getConfigPath();
            FileUtils.deleteQuietly(new File(configPath));
            Files.write(Paths.get(configPath), json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException e) {
            log.error("保存配置文件失败", e);
        }

    }

    /**
     * 格式化输出json
     *
     * @param object
     * @return
     */
    private static String toPrettyFormat(Object object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }

    /**
     * 获取文件路径
     *
     * @return
     */
    private static String getConfigPath() {
        String path = SvnGUI.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
            path = StringUtils.substringBeforeLast(path, File.separator);
            path = path + configDirName + File.separator + configFileName;
            path = AllUtils.replaceFileSeparatorToLinux(path);
            if (path.startsWith("/")) {
                path = StringUtils.substringAfter(path, "/");
            }
            //log.info("配置文件路径：{}", path);
        } catch (UnsupportedEncodingException e) {
            log.error("获取配置文件路径失败", e);
        }
        return path;
    }
}
