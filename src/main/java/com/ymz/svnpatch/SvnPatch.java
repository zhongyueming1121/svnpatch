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
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 获取增量文件列表
 *
 * @author: ymz
 * @date: 2021-08-18 21:32
 **/
@Slf4j
public class SvnPatch {
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(2));
            /**
             * 最大log数：10万
             */
    private static final int maxLoadFileNum = 5000;
    /**
     * 开始结束反转
     */
    private static boolean upturn = false;
    protected static volatile boolean stop = true;


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
        log.info("开始获取svn提交记录");
        // 得到历史记录
        List<String> history = new ArrayList<>();
        try {
            // 时间没有等于关系，这边需要稍微加减1秒
            if (startDate != null && endDate != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(startDate);
                c.add(Calendar.SECOND, -1);
                startDate = c.getTime();
                Calendar c2 = Calendar.getInstance();
                c2.setTime(endDate);
                c2.add(Calendar.SECOND, 1);
                endDate = c2.getTime();
            }
            SVNLogClient logClient = getLogClient(user, password);
            history = loadSvnLogHistory(logClient, url, versions, versionRange, startDate, endDate);
        } catch (Exception e) {
            log.error("getSvnRepositoryHistory error", e);
        }
        return history;
    }

    /**
     * 加载log
     *
     * @param svnLogClient
     * @param url
     * @param versions
     * @param versionRange 是否范围版本
     * @param startDate
     * @param endDate
     * @return
     * @throws SVNException
     */
    private List<String> loadSvnLogHistory(SVNLogClient svnLogClient, String url, List<Integer> versions, boolean versionRange, Date startDate, Date endDate) throws SVNException {
        List<String> history = new ArrayList<>(1000);
        boolean stopOnCopy = false;
        boolean discoverChangedPaths = true;
        boolean includeMergedRevisions = false;
        SVNURL repositoryURL = SVNURL.parseURIEncoded(url);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (versions.isEmpty() && startDate == null && endDate == null) {
            log.error("未选过滤范围");
            return history;
        }
        if (versions.isEmpty()) {
            svnLogClient.doLog(repositoryURL, new String[]{""}, SVNRevision.HEAD, SVNRevision.parse("{" + sdf.format(startDate) + "}"), SVNRevision.parse("{" + sdf.format(endDate) + "}"),
                    stopOnCopy, discoverChangedPaths, includeMergedRevisions, maxLoadFileNum, null, svnlogentry -> {
                        filterLog(startDate, endDate, history, 0, 0, svnlogentry);
                    });
        }
        if (versionRange) {
            Integer startVersion = versions.get(upturn ? 1 : 0);
            Integer endVersion = versions.get(upturn ? 0 : 1);
            svnLogClient.doLog(repositoryURL, new String[]{""}, SVNRevision.HEAD, SVNRevision.parse(startVersion + ""), SVNRevision.parse(endVersion + ""),
                    stopOnCopy, discoverChangedPaths, includeMergedRevisions, maxLoadFileNum, null, svnlogentry -> {
                        filterLog(startDate, endDate, history, startVersion, endVersion, svnlogentry);
                    });
        } else {
            for (Integer version : versions) {
                svnLogClient.doLog(repositoryURL, new String[]{""}, SVNRevision.HEAD, SVNRevision.parse(version + ""), SVNRevision.parse(version + ""),
                        stopOnCopy, discoverChangedPaths, includeMergedRevisions, maxLoadFileNum, null, svnlogentry -> {
                            filterLog(startDate, endDate, history, version, version, svnlogentry);
                        });
            }
        }
        return history;
    }

    /**
     * 过滤日志
     *
     * @param startDate
     * @param endDate
     * @param history
     * @param startVersion
     * @param endVersion
     * @param svnlogentry
     */
    private void filterLog(Date startDate, Date endDate, List<String> history, Integer startVersion, Integer endVersion, SVNLogEntry svnlogentry) {
        if (svnlogentry == null) {
            return;
        }
        log.info("filterLog-->startVersion：{}，endVersion：{}，startDate：{}，endDate：{}", startVersion, endVersion, startDate, endDate);
        Map<String, SVNLogEntryPath> changedPathsMap = new HashMap<>();
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
                history.add(changedPathsMap.get(key).toString());
            }
        }

    }

    /**
     * 过滤类型
     *
     * @param startVersion
     * @param endVersion
     * @param startDate
     * @param endDate
     * @return
     */
    private int getHandleType(int startVersion, int endVersion, Date startDate, Date endDate) {
        int type = 0;
        if (startVersion == 0 && endVersion == 0) {
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
     * @param url      项目url
     * @param user     账号
     * @param password 密码
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
            String dirName = StringUtils.substringAfterLast(url, "/");
            String basePath = AllUtils.getCodePath();
            File deleteDir = new File(basePath + File.separator + dirName);
            if (deleteDir.exists() && deleteDir.isDirectory()) {
                AllUtils.deleteFileOrDirectory(deleteDir);
                if(deleteDir.exists()) {
                    FileUtils.forceDelete(deleteDir);
                }
            }
            SVNClientManager ourClientManager;
            //初始化支持svn://协议的库。 必须先执行此操作。
            DAVRepositoryFactory.setup();
            //相关变量赋值
            SVNURL repositoryURL = SVNURL.parseURIEncoded(url);
            DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
            //实例化客户端管理类
            ourClientManager = SVNClientManager.newInstance(options, name, pwd);
            File dstPath = new File(basePath + File.separator + dirName);
            //通过客户端管理类获得updateClient类的实例。
            SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
            updateClient.setIgnoreExternals(false);
            //执行check out 操作，返回工作副本的版本号。
            stop = false;
            log.info("开始拉取代码，请稍后...");
            executor.execute(()->{
                for (int i = 0; i < 300; i++) {
                    if(!stop) {
                        log.info("正在拉取代码...");
                    } else {
                        break;
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            long workingVersion = updateClient.doCheckout(repositoryURL, dstPath, SVNRevision.HEAD,
                    StringUtils.isBlank(version) ? SVNRevision.HEAD : SVNRevision.parse(version), SVNDepth.INFINITY, true);
            stop = true;
            executor.shutdownNow();
            log.info("把版本：" + workingVersion + " check out 到目录：" + dstPath + "中成功。");
            return dstPath.getPath();
        } catch (Exception e) {
            log.error("拉取代码失败", e);
        }
        return null;
    }

    /**
     * 获取操作客户端
     *
     * @param name
     * @param pwd
     * @return
     */
    private SVNLogClient getLogClient(String name, String pwd) {
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        SVNClientManager svnClientManager = SVNClientManager.newInstance(options, name, pwd);
        return svnClientManager.getLogClient();
    }
}
