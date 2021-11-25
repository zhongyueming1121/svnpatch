package com.ymz.svnpatch;

import com.ymz.util.AllUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.util.*;

/**
 * 获取增量文件列表
 *
 * @author: ymz
 * @date: 2021-08-18 21:32
 **/
@Slf4j
public class SvnPatch {

    private static SVNRepository repository = null;
    /**
     * 最大log数：10万
     */
    private static final int maxLoadFileNum = 100000;


    /**
     * 获得svn历史记录
     *
     * @param url       svn地址
     * @param user      用户名
     * @param password  密码
     * @param versions  版本集合
     * @param startDate 开始时间，null是不限制
     * @param endDate   结束时间，null是不限制
     * @return
     */
    public List<String> getSvnRepositoryHistory(String url, String user, String password, List<Integer> versions, boolean versionRange, Date startDate, Date endDate) {
        if (repository == null) {
            repository = doCreateSVNRepository(url, user, password.toCharArray());
        }
        // 得到历史记录
        List<String> history = new ArrayList<>();
        try {
            history = svnLogHistory(versions, versionRange, startDate, endDate);
        } catch (Exception e) {
            log.error("getSvnRepositoryHistory error", e);
        }
        return history;
    }

    /**
     * 加载log 最大10万行
     *
     * @param versions
     * @param versionRange 是否版本范围
     * @param startDate
     * @param endDate
     * @return
     * @throws SVNException
     */
    private List<String> svnLogHistory(List<Integer> versions, boolean versionRange, Date startDate, Date endDate) throws SVNException {
        List<String> history = new ArrayList<>(10000);
        if (repository == null) {
            return history;
        }
        if (versions.isEmpty()) {
            repository.log(new String[]{""}, 0, -1, true, false, maxLoadFileNum, svnlogentry -> {
                filterLog(startDate, endDate, history, 0, -1, svnlogentry);
            });
        }
        if (versionRange) {
            Integer startVersion = versions.get(0);
            Integer endVersion = versions.get(1);
            // String[] 为过滤的文件路径前缀，为空表示不进行过滤
            // strictNode 设为 false 时，日志会追溯到每个文件的开始
            repository.log(new String[]{""}, startVersion, endVersion, true, false, maxLoadFileNum, svnlogentry -> {
                filterLog(startDate, endDate, history, startVersion, endVersion, svnlogentry);
            });
        } else {
            for (Integer version : versions) {
                repository.log(new String[]{""}, version, version, true, false, maxLoadFileNum, svnlogentry -> {
                    filterLog(startDate, endDate, history, version, version, svnlogentry);
                });
            }
        }
        return history;
    }

    private void filterLog(Date startDate, Date endDate, List<String> history, Integer startVersion, Integer endVersion, SVNLogEntry svnlogentry) {
        if (svnlogentry == null) {
            return;
        }
        log.info("filterLog-->startVersion：{}，endVersion：{}，startDate：{}，endDate：{}", startVersion, endVersion, startDate, endDate);
        Map<String, SVNLogEntryPath> changedPathsMap = new HashMap<>();
        List<String> svnFilePaths = new ArrayList<>();
        int handleType = getHandleType(startVersion, endVersion, startDate, endDate);
        if (handleType == 0) {
            // 没有限制
            changedPathsMap = svnlogentry.getChangedPaths();
            log.error("加载svn日志没加条件限制！！！最大可加载10万条！！！");
        }
        if (handleType == 1) {
            // 只过滤版本
            log.info("只过滤版本");
            if (svnlogentry.getRevision() >= startVersion && svnlogentry.getRevision() <= endVersion) {
                changedPathsMap = svnlogentry.getChangedPaths();
            }
        }
        if (handleType == 2) {
            // 只过滤时间
            log.info("只过滤时间");
            if (svnlogentry.getDate().getTime() >= startDate.getTime() && svnlogentry.getDate().getTime() <= endDate.getTime()) {
                changedPathsMap = svnlogentry.getChangedPaths();
            }
        }
        if (handleType == 3) {
            // 既过滤版本又过滤时间
            log.info("既过滤版本又过滤时间");
            if (svnlogentry.getRevision() >= startVersion && svnlogentry.getRevision() <= endVersion
                    && svnlogentry.getDate().getTime() >= startDate.getTime() && svnlogentry.getDate().getTime() <= endDate.getTime()) {
                changedPathsMap = svnlogentry.getChangedPaths();
            }
        }
        if (changedPathsMap != null) {
            for (String key : changedPathsMap.keySet()) {
                svnFilePaths.add(changedPathsMap.get(key) + "");
                log.info(changedPathsMap.get(key) + "");
            }
            history.addAll(svnFilePaths);
        }
    }

    private int getHandleType(int startVersion, int endVersion, Date startDate, Date endDate) {
        int type = 0;
        if (startVersion == 0 && endVersion == -1) {
            type += 0;
        } else {
            type += 1;
        }
        if (startDate != null && endDate != null) {
            type += 10;
        } else {
            type += 0;
        }
        return AllUtils.biannary2Decimal(type);
    }

    /**
     * 连接svn，并登陆
     *
     * @param url
     * @return
     */
    private synchronized SVNRepository doCreateSVNRepository(String url, String user, char[] password) {
        SVNRepository repository = null;
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(user, password);
            repository.setAuthenticationManager(authManager);
        } catch (SVNException e) {
            log.error("doCreateSVNRepository error", e);
        }
        return repository;
    }

    /**
     * 根据提交版本拉文件
     *
     * @param version
     * @param url
     * @param name
     * @param pwd
     * @return
     */
    public String checkOutByVersion(String version, String url, String name, String pwd) {
        try {
            SVNClientManager ourClientManager;
            //初始化支持svn://协议的库。 必须先执行此操作。
            DAVRepositoryFactory.setup();
            //相关变量赋值
            SVNURL repositoryURL = SVNURL.parseURIEncoded(url);
            DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
            //实例化客户端管理类
            ourClientManager = SVNClientManager.newInstance(options, name, pwd);
            String dirName = StringUtils.substringAfterLast(url, "/");
            String basePath = AllUtils.getCodePath();
            File delteDir = new File(basePath + File.separator + dirName);
            if (delteDir.exists() && delteDir.isDirectory()) {
                FileUtils.forceDelete(delteDir);
            }
            File dstPath = new File(basePath + File.separator + dirName);
            //通过客户端管理类获得updateClient类的实例。
            SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
            updateClient.setIgnoreExternals(false);
            //执行check out 操作，返回工作副本的版本号。
            long workingVersion = updateClient.doCheckout(repositoryURL, dstPath, SVNRevision.HEAD,
                    StringUtils.isBlank(version) ? SVNRevision.HEAD : SVNRevision.parse(version), SVNDepth.INFINITY, true);
            log.info("把版本：" + workingVersion + " check out 到目录：" + dstPath + "中成功。");
            return dstPath.getPath();
        } catch (Exception e) {
            log.error("拉取代码失败", e);
        }
        return null;
    }


    /**
     * recursively checks out a working copy from url into wcDir
     *
     * @param clientManager
     * @param url           a repository location from where a Working Copy will be checked out
     * @param revision      the desired revision of the Working Copy to be checked out
     * @param destPath      the local path where the Working Copy will be placed
     * @param depth         checkout的深度，目录、子目录、文件
     * @return
     * @throws SVNException
     */
    public static long checkout(SVNClientManager clientManager, SVNURL url,
                                SVNRevision revision, File destPath, SVNDepth depth) {

        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        /*
         * sets externals not to be ignored during the checkout
         */
        updateClient.setIgnoreExternals(false);
        /*
         * returns the number of the revision at which the working copy is
         */
        try {
            return updateClient.doCheckout(url, destPath, revision, revision, depth, false);
        } catch (SVNException e) {
            log.error("拉取代码失败", e);
        }
        return 0;
    }
}
