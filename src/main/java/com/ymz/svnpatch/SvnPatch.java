package com.ymz.svnpatch;

import com.ymz.util.AllUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;

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
     * @param url          svn地址
     * @param user         用户名
     * @param password     密码
     * @param versions     版本集合
     * @param startVersion 开始版本，0是不限制
     * @param endVersion   结束版本，-1是不限制
     * @param startDate    开始时间，null是不限制
     * @param endDate      结束时间，null是不限制
     * @return
     */
    public List<String> getSvnRepositoryHistory(String url, String user, String password, List<Integer> versions,
                                                int startVersion, int endVersion, Date startDate, Date endDate) {
        if (repository == null) {
            repository = doCreateSVNRepository(url, user, password.toCharArray());
        }
        // 得到历史记录
        List<String> history = new ArrayList<>();
        try {
            history = svnLogHistory(versions, startVersion, endVersion, startDate, endDate);
        } catch (Exception e) {
            log.error("getSvnRepositoryHistory error", e);
        }
        return history;
    }

    /**
     * 加载log 最大10万行
     *
     * @param versions
     * @param startVersion
     * @param endVersion
     * @param startDate
     * @param endDate
     * @return
     * @throws SVNException
     */
    private List<String> svnLogHistory(List<Integer> versions, int startVersion, int endVersion, Date startDate, Date endDate) throws SVNException {
        List<String> history = new ArrayList<>(10000);
        if (repository == null) {
            return history;
        }
        // String[] 为过滤的文件路径前缀，为空表示不进行过滤
        // strictNode 设为 false 时，日志会追溯到每个文件的开始
        repository.log(new String[]{""}, startVersion, endVersion, true, false, maxLoadFileNum, svnlogentry -> {
            if (svnlogentry == null) {
                return;
            }
            Map<String, SVNLogEntryPath> changedPathsMap = new HashMap<>();
            List<String> svnFilePaths = new ArrayList<>();
            int handleType = getHandleType(versions, startVersion, endVersion, startDate, endDate);
            if (handleType == 0) {
                // 没有限制
                changedPathsMap = svnlogentry.getChangedPaths();
                log.error("加载svn日志没加条件限制！！！最大可加载10万条！！！");
            }
            if (handleType == 1) {
                // 只过滤版本
                if (versions.isEmpty()) {
                    if (svnlogentry.getRevision() >= startVersion && svnlogentry.getRevision() <= endVersion) {
                        changedPathsMap = svnlogentry.getChangedPaths();
                    }
                } else {
                    // 非起始版本的情况
                    if (versions.contains((int) svnlogentry.getRevision())) {
                        changedPathsMap = svnlogentry.getChangedPaths();
                    }
                }
            }
            if (handleType == 2) {
                // 只过滤时间
                if (svnlogentry.getDate().getTime() >= startDate.getTime() && svnlogentry.getDate().getTime() <= endDate.getTime()) {
                    changedPathsMap = svnlogentry.getChangedPaths();
                }
            }
            if (handleType == 3) {
                // 既过滤版本又过滤时间
                if (versions.isEmpty()) {
                    if (svnlogentry.getRevision() >= startVersion && svnlogentry.getRevision() <= endVersion
                            && svnlogentry.getDate().getTime() >= startDate.getTime() && svnlogentry.getDate().getTime() <= endDate.getTime()) {

                        changedPathsMap = svnlogentry.getChangedPaths();
                    }
                } else {
                    // 非起始版本的情况
                    if (versions.contains((int) svnlogentry.getRevision())
                            && svnlogentry.getDate().getTime() >= startDate.getTime() && svnlogentry.getDate().getTime() <= endDate.getTime()) {

                        changedPathsMap = svnlogentry.getChangedPaths();
                    }
                }
            }
            if (changedPathsMap != null) {
                for (String key : changedPathsMap.keySet()) {
                    svnFilePaths.add(changedPathsMap.get(key) + "");
                    log.info(changedPathsMap.get(key) + "");
                }
                history.addAll(svnFilePaths);
            }
        });
        return history;
    }

    private int getHandleType(List<Integer> versions, int startVersion, int endVersion, Date startDate, Date endDate) {
        int type = 0;
        if (startVersion == 0 && endVersion == -1 && versions.isEmpty()) {
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
     * @param version
     * @param url
     * @param path
     * @param name
     * @param pwd
     * @return
     */
    public String checkOutByVersion(String version, String url, String path, String name, String pwd) {
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
            File delteDir = new File(path + File.separator + dirName);
            if (delteDir.exists() && delteDir.isDirectory()) {
                FileUtils.forceDelete(delteDir);
            }
            path = path.replace("/", File.separator);
            File dstPath = new File(path);
            //通过客户端管理类获得updateClient类的实例。
            SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
            updateClient.setIgnoreExternals(false);
            //执行check out 操作，返回工作副本的版本号。
            long workingVersion = updateClient.doExport(repositoryURL, dstPath, SVNRevision.HEAD, SVNRevision.parse(version), "", true, SVNDepth.fromRecurse(false));
            log.info("把版本：" + workingVersion + " check out 到目录：" + dstPath + "中。");
            return dstPath.getPath();
        } catch (Exception e) {
            log.error("拉取代码失败", e);
        }
        return "";
    }
}
