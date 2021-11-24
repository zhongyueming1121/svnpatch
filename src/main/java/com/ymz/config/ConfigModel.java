package com.ymz.config;

/**
 * 配置实体，默认为空字符串
 *
 * @author: ymz
 * @date: 2021-08-22 20:36
 **/
public class ConfigModel {
    /**
     * 项目目标地址
     */
    private String targetPath = "";
    /**
     * url
     */
    private String url = "";
    /**
     * 用户名
     */
    private ConfigUser user = new ConfigUser();
    /**
     * 开始版本
     */
    private String startVersion = "";
    /**
     * 编译命令
     */
    private String cmd = "";
    /**
     * maven home
     */
    private String mavenHome = "";
    /**
     * 结束版本
     */
    private String endVersion = "";
    /**
     * 开始时间
     */
    private Long startDate = 0L;
    /**
     * 结束时间
     */
    private Long endDate = 0L;

    private int svnOrGit = 0;

    private int localWar = 0;

    private int logFrom = 0;

    private String patchFilePath = "";

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ConfigUser getUser() {
        return user;
    }

    public void setUser(ConfigUser user) {
        this.user = user;
    }

    public String getStartVersion() {
        return startVersion;
    }

    public void setStartVersion(String startVersion) {
        this.startVersion = startVersion;
    }

    public String getEndVersion() {
        return endVersion;
    }

    public void setEndVersion(String endVersion) {
        this.endVersion = endVersion;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getMavenHome() {
        return mavenHome;
    }

    public void setMavenHome(String mavenHome) {
        this.mavenHome = mavenHome;
    }

    public int getSvnOrGit() {
        return svnOrGit;
    }

    public void setSvnOrGit(int svnOrGit) {
        this.svnOrGit = svnOrGit;
    }

    public int getLocalWar() {
        return localWar;
    }

    public void setLocalWar(int localWar) {
        this.localWar = localWar;
    }

    public int getLogFrom() {
        return logFrom;
    }

    public void setLogFrom(int logFrom) {
        this.logFrom = logFrom;
    }

    public String getPatchFilePath() {
        return patchFilePath;
    }

    public void setPatchFilePath(String patchFilePath) {
        this.patchFilePath = patchFilePath;
    }

    @Override
    public String toString() {
        return "ConfigModel{" +
                "targetPath='" + targetPath + '\'' +
                ", url='" + url + '\'' +
                ", user=" + user +
                ", startVersion='" + startVersion + '\'' +
                ", cmd='" + cmd + '\'' +
                ", mavenHome='" + mavenHome + '\'' +
                ", endVersion='" + endVersion + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", svnOrGit=" + svnOrGit +
                ", localWar=" + localWar +
                ", logFrom=" + logFrom +
                ", patchFilePath='" + patchFilePath + '\'' +
                '}';
    }
}
