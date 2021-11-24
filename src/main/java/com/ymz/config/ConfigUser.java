package com.ymz.config;


import org.apache.commons.codec.digest.DigestUtils;

import java.util.Objects;

/**
 * 账号密码实体
 *
 * @author: ymz
 * @date: 2021-08-22 20:44
 **/
public final class ConfigUser {

    public ConfigUser() {
    }

    public ConfigUser(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    private String userName = "";
    private String password = "";

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "ConfigUser{" +
                "userName='" + userName + '\'' +
                ", password='" + ((password == null || password.equals("")) ? password : DigestUtils.md5Hex(password)) + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigUser that = (ConfigUser) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, password);
    }
}
