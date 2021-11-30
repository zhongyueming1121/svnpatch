package com.ymz.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ymz.constant.NameConstant;
import com.ymz.util.AllUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
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

    /**
     * 读取配置
     *
     * @return
     */
    public static ConfigJsonModel loadConfig() {
        String configPath = getConfigPath();
        StringBuilder jsonBuilder = new StringBuilder();
        try {
            File file = new File(configPath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            Files.lines(Paths.get(configPath), StandardCharsets.UTF_8).forEach(jsonBuilder::append);
        } catch (IOException e) {
            log.error("读取配置文件失败", e);
        }
        Gson gson = new Gson();
        ConfigJsonModel configJsonModel = gson.fromJson(jsonBuilder.toString(), ConfigJsonModel.class);
        if (configJsonModel == null) {
            configJsonModel = new ConfigJsonModel();
        } else {
            String macCodeMd5 = AllUtils.getMacCodeMd5();
            if (!macCodeMd5.equals(configJsonModel.getMaccode())) {
                configJsonModel = new ConfigJsonModel();
                ConfigManager.clearConfig();
            }
        }
        return configJsonModel;
    }

    /**
     * 清空配置文件
     */
    public static void clearConfig() {
        try {
            String configPath = getConfigPath();
            FileUtils.deleteQuietly(new File(configPath));
            Files.write(Paths.get(configPath), "".getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            log.error("保存配置文件失败", e);
        }

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
            if (urlHistories != null && urlHistories.size() > 10) {
                urlHistories.removeLast();
            } else {
                break;
            }
        }
        for (; ; ) {
            if (userHistories != null && userHistories.size() > 10) {
                userHistories.removeLast();
            } else {
                break;
            }
        }
        for (; ; ) {
            if (cmdHistories != null && cmdHistories.size() > 10) {
                cmdHistories.removeLast();
            } else {
                break;
            }
        }
        for (; ; ) {
            if (mavenPathHistories != null && mavenPathHistories.size() > 10) {
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
        configJsonModel.setMaccode(AllUtils.getMacCodeMd5());

        String json = toPrettyFormat(configJsonModel);
        try {
            log.debug("写入配置：{}", json);
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
        return AllUtils.getJarPath() + File.separator + NameConstant.config + File.separator + NameConstant.configFileName;
    }

}
