package com.ymz.config;

import java.util.LinkedList;

/**
 * 配置文件模型
 *
 * @author: ymz
 * @date: 2021-08-22 20:33
 **/
public class ConfigJsonModel {
    private ConfigModel lastUseConfig;
    private LinkedList<String> urlHistories;
    private LinkedList<String> cmdHistories;
    private LinkedList<String> mavenHistories;
    private LinkedList<ConfigUser> userHistories;
    private String maccode;

    public ConfigModel getLastUseConfig() {
        return lastUseConfig;
    }

    public void setLastUseConfig(ConfigModel lastUseConfig) {
        this.lastUseConfig = lastUseConfig;
    }

    public LinkedList<String> getUrlHistories() {
        return urlHistories;
    }

    public void setUrlHistories(LinkedList<String> urlHistories) {
        this.urlHistories = urlHistories;
    }

    public LinkedList<ConfigUser> getUserHistories() {
        return userHistories;
    }

    public void setUserHistories(LinkedList<ConfigUser> userHistories) {
        this.userHistories = userHistories;
    }

    public LinkedList<String> getCmdHistories() {
        return cmdHistories;
    }

    public void setCmdHistories(LinkedList<String> cmdHistories) {
        this.cmdHistories = cmdHistories;
    }

    public LinkedList<String> getMavenHistories() {
        return mavenHistories;
    }

    public void setMavenHistories(LinkedList<String> mavenHistories) {
        this.mavenHistories = mavenHistories;
    }

    public String getMaccode() {
        return maccode;
    }

    public void setMaccode(String maccode) {
        this.maccode = maccode;
    }
}
