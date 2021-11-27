# svnpatch
windows下svn和git下快速增量包打包工具
# 功能
[1] checkout指定版本代码  
[2] 根据提交记录抽取文件打包成增量包  
[3] 支持maven和ant打包  
[4] svn支持自动读取本机账号密码  
[5] 支持配置文件记忆  
[6] GUI界面实时显示日志  

# 待支持功能
[1] git  
[2] 根据patch文件打增量包  

# 程序目录说明
svnpatch  
│  run.cmd #启动脚本  
│  svnpatch.jar #主程序   
│  
├─code #下载的代码目录  
├─config #配置文件目录  
│─rmsvnlog #svn中删除文件记录  
├─logs #日志目录  
└─patch #增量包目录  

# 主界面图
![image](https://github.com/zhongyueming1121/svnpatch/blob/main/doc/window.jpg)

# 使用run.cmd运行程序

![image](https://github.com/zhongyueming1121/svnpatch/blob/main/doc/run.jpg)
