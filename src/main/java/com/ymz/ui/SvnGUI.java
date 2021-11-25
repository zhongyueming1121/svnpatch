package com.ymz.ui;

import com.ymz.config.ConfigJsonModel;
import com.ymz.config.ConfigManager;
import com.ymz.config.ConfigModel;
import com.ymz.config.ConfigUser;
import com.ymz.svnauth.Program;
import com.ymz.svnpatch.MakeWarPatch;
import com.ymz.util.AllUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class SvnGUI {
    /**
     * 历史密码
     */
    private static HashMap<String, ConfigUser> configUserMap = new HashMap<>();
    private static DateTimePicker dateTimePickerStart = null;
    private static DateTimePicker dateTimePickerEnd = null;
    public static JProgressBar progressBar;
    private static Executor executor = Executors.newFixedThreadPool(1);
    private static MyJTextArea outputTextArea;
    private static JScrollPane scrollPane;
    private static String defaultItem = "";
    private static String defaultPwd = "******";
    private static ConfigModel currentConfig;
    private static HashMap<String, JTextField> componentMap = new HashMap<>();
    private static HashMap<String, JComboBox<String>> comboBoxMap = new HashMap<>();
    private static HashMap<String, JRadioButton> comRadioMap = new HashMap<>();
    public static LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<>(600);

    /**
     * 初始化配置文件
     */
    private static volatile ConfigJsonModel configJsonModel;

    static {
        // 读配置
        configJsonModel = ConfigManager.loadConfig();
        if (configJsonModel == null) {
            configJsonModel = new ConfigJsonModel();
        }
        currentConfig = configJsonModel.getLastUseConfig() == null ? new ConfigModel() : configJsonModel.getLastUseConfig();
    }

    public static void main(String[] args) {
        mainRun();
    }

    /**
     * 主方法
     */
    private static void mainRun() {
        //统一设置字体
        InitGlobalFont(new Font("黑体", Font.PLAIN, 14));
        JFrame frame = new JFrame();
        frame.setSize(1024, 768);
        frame.setTitle("快速增量打包工具V1.0");
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        SvnGUI svnGUI = new SvnGUI();
        svnGUI.placeComponents(panel);
        // 时间选择器
        Date startDate = currentConfig.getStartDate() == 0L ? null : new Date(currentConfig.getStartDate());
        Date endDate = currentConfig.getStartDate() == 0L ? null : new Date(currentConfig.getEndDate());
        dateTimePickerStart = svnGUI.timeComponent(120, 220, 200, 30, startDate);
        dateTimePickerEnd = svnGUI.timeComponent(420, 220, 200, 30, endDate);
        frame.add(dateTimePickerStart);
        frame.add(dateTimePickerEnd);
        // 其他组件
        frame.add(panel);
        // 进度条
        svnGUI.showLogBar(frame, panel);
        frame.setVisible(true);
    }

    private void showLogBar(JFrame mainFrame, JPanel panel) {
        JLabel infoLabel = new JLabel("信息：");
        infoLabel.setBounds(20, 330, 200, 30);
        infoLabel.setForeground(Color.RED);
        outputTextArea = new MyJTextArea("", 5, 20);
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setFont(new Font("宋体", Font.PLAIN, 12));
        scrollPane = new JScrollPane(outputTextArea);
        scrollPane.setBounds(20, 360, 940, 240);

        progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(20, 620, 940, 30);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        panel.add(infoLabel);
        panel.add(progressBar);
        panel.add(scrollPane);
        mainFrame.add(panel);
        outInfo();
        executor.execute(this::showLog);
    }

    private void outInfo() {
        outputTextArea.append("[1] checkout版本默认最新版本\n");
        outputTextArea.append("[2] ant编译命令需以ant开头，例如：ant release\n");
        outputTextArea.append("[3] maven home格式，例如：D:\\apache-maven-3.6.3\n");
        outputTextArea.append("--------------------------------------------------\n");
        outputTextArea.append("//      ┏┛ ┻━━┛ ┻┓\n");
        outputTextArea.append("//      ┃　　　　　　 ┃ \n");
        outputTextArea.append("//      ┃　　　━　　  ┃\n");
        outputTextArea.append("//      ┃　┳┛　 ┗┳　┃\n");
        outputTextArea.append("//      ┃　　　　　　 ┃\n");
        outputTextArea.append("//      ┃　　　┻　　　┃\n");
        outputTextArea.append("//      ┃　　　　　　 ┃  \n");
        outputTextArea.append("//      ┗━┓　　　┏━┛ \n");
        outputTextArea.append("//        ┃　　　┃   神兽保佑     \n");
        outputTextArea.append("//        ┃　　　┃   代码无BUG！  \n");
        outputTextArea.append("//        ┃　　　┗━━━━━┓     \n");
        outputTextArea.append("//        ┃　　　　　　　      ┣┓  \n");
        outputTextArea.append("//        ┃　　　　          ┏┛  \n");
        outputTextArea.append("//        ┗━┓ ┓ ┏━┳ ┓ ┏━┛     \n");
        outputTextArea.append("//          ┃ ┫ ┫   ┃ ┫ ┫      \n");
        outputTextArea.append("//          ┗━┻━┛   ┗━┻━┛      \n");
        outputTextArea.append("--------------------------------------------------\n");
    }

    /**
     * 统一设置字体
     */
    private static void InitGlobalFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys();
             keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }

    /**
     * 时间控件
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @param initDate
     * @return
     */
    private DateTimePicker timeComponent(int x, int y, int w, int h, Date initDate) {
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setFormats(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM));
        dateTimePicker.setTimeFormat(DateFormat.getTimeInstance(DateFormat.MEDIUM));
        dateTimePicker.setDate(initDate);
        dateTimePicker.setBounds(x, y, w, h);
        return dateTimePicker;
    }

    /**
     * 主界面
     *
     * @param panel
     */
    private void placeComponents(JPanel panel) {
        panel.setLayout(null);
        // 项目路径
        targetVersionComponent(panel);
        // url
        urlComponent(panel);
        // 账号密码
        authComponent(panel);
        // 版本
        versionComponent(panel);

        componentCmd(panel);

        componentLogFrom(panel);

        addButton(panel);

    }

    private void addButton(JPanel panel) {
        // 创建登录按钮
        JButton patchBuildButton = new JButton("生成增量包");
        patchBuildButton.setBounds(20, 680, 200, 30);
        panel.add(patchBuildButton);
        patchBuildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("你按下了" + patchBuildButton.getText());
                progressBar.setValue(10);
                ConfigModel config = getConfig();
                //log.info("config:{}", config.toString());
                String checkInput = checkInput(config);
                if (!"success".equals(checkInput)) {
                    log.error(checkInput);
                    JOptionPane.showMessageDialog(panel, checkInput);
                    return;
                }
                progressBar.setValue(20);
                // 打增量包
                log.info("开始打增量包");
                try {
                    boolean success = new MakeWarPatch().startMake(config, false);
                    if (success) {
                        SvnGUI.progressBar.setValue(100);
                    }
                    //JOptionPane.showMessageDialog(panel, success ? "打包完成" : "打包失败");
                } catch (Exception ee) {
                    log.error("打包失败", ee);
                    JOptionPane.showMessageDialog(panel, "打包失败");
                }
                ConfigManager.writeConfig(getConfigJsonModel(config));
                // 隐藏密码
                String pwd = componentMap.get("pwd").getText();
                if (StringUtils.isNotBlank(pwd)) {
                    componentMap.get("pwd").setText(defaultPwd);
                }
            }
        });

        // 创建登录按钮
        JButton warBuildButton = new JButton("生成全量包");
        warBuildButton.setBounds(260, 680, 200, 30);
        panel.add(warBuildButton);
        warBuildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("你按下了" + warBuildButton.getText());
                ConfigModel config = getConfig();
                //log.info("config:{}", config.toString());
                String checkInput = checkInput(config);
                if (!"success".equals(checkInput)) {
                    log.error(checkInput);
                    JOptionPane.showMessageDialog(panel, checkInput);
                    return;
                }
                // 打全量包
                config.setStartVersion("0");
                config.setStartVersion("-1");
                log.info("开始打全量包");
                try {
                    boolean success = new MakeWarPatch().startMake(config, true);
                    if (success) {
                        SvnGUI.progressBar.setValue(100);
                    }
                    JOptionPane.showMessageDialog(panel, success ? "打包完成" : "打包失败");
                } catch (Exception ee) {
                    log.error("打包失败", ee);
                    JOptionPane.showMessageDialog(panel, "打包失败");
                }
                ConfigManager.writeConfig(getConfigJsonModel(config));
                // 隐藏密码
                String pwd = componentMap.get("pwd").getText();
                if (StringUtils.isNotBlank(pwd)) {
                    componentMap.get("pwd").setText(defaultPwd);
                }
            }
        });
    }

    private void componentCmd(JPanel panel) {
        // 编译命令
        JLabel optHis = new JLabel("编译命令：");
        optHis.setBounds(335, 260, 100, 30);
        panel.add(optHis);
        JComboBox<String> cmdHisComboBox = new JComboBox<>();
        cmdHisComboBox.setBounds(420, 260, 200, 30);
        cmdHisComboBox.setEditable(true);
        cmdHisComboBox.setName("cmd");
        LinkedList<String> list = configJsonModel.getCmdHistories() == null ? new LinkedList<>() : configJsonModel.getCmdHistories();
        if (list.isEmpty()) {
            cmdHisComboBox.addItem(defaultItem);
        }
        list.forEach(cmdHisComboBox::addItem);
        // 自定义悬浮框
        cmdHisComboBox.setRenderer(new

                JComboBoxRenderer());
        cmdHisComboBox.setSelectedIndex(0);
        comboBoxMap.put(cmdHisComboBox.getName(), cmdHisComboBox);

        //itemListener
        ItemListener optHisComboBoxListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (ItemEvent.SELECTED == arg0.getStateChange()) {
                    String selectedItem = arg0.getItem().toString();
                    if (selectedItem.equals(defaultItem)) {
                        return;
                    }
                    log.info("new selected item : {}", selectedItem);
                }
            }

        };
        cmdHisComboBox.addItemListener(optHisComboBoxListener);
        panel.add(cmdHisComboBox);
        // maven home 或者war包地址
        JLabel mavenHis = new JLabel("maven home：");
        mavenHis.setBounds(640, 260, 100, 30);
        panel.add(mavenHis);
        JComboBox<String> mavenHisComboBox = new JComboBox<>();
        mavenHisComboBox.setBounds(740, 260, 200, 30);
        mavenHisComboBox.setEditable(true);
        mavenHisComboBox.setName("mavenHome");
        LinkedList<String> list2 = configJsonModel.getMavenHistories() == null ? new LinkedList<>() : configJsonModel.getMavenHistories();
        if (list2.isEmpty()) {
            mavenHisComboBox.addItem(defaultItem);
        }
        list2.forEach(mavenHisComboBox::addItem);
        // 自定义悬浮框
        mavenHisComboBox.setRenderer(new

                JComboBoxRenderer());
        mavenHisComboBox.setSelectedIndex(0);
        comboBoxMap.put(mavenHisComboBox.getName(), mavenHisComboBox);

        //itemListener
        ItemListener mavenHisComboBoxListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (ItemEvent.SELECTED == arg0.getStateChange()) {
                    String selectedItem = arg0.getItem().toString();
                    if (selectedItem.equals(defaultItem)) {
                        return;
                    }
                    log.info("new selected item : {}", selectedItem);
                }
            }
        };
        mavenHisComboBox.addItemListener(mavenHisComboBoxListener);
        panel.add(mavenHisComboBox);

        // 文件类型
        JLabel localFileOrBuildLabel = new JLabel("文件类型：");
        localFileOrBuildLabel.setBounds(20, 260, 100, 30);
        panel.add(localFileOrBuildLabel);
        // 创建两个单选按钮
        JRadioButton localWarRadio = new JRadioButton("本地war包");
        localWarRadio.setName("localWar");
        JRadioButton buildWarRadio = new JRadioButton("在线编译");
        buildWarRadio.setName("onlineBuild");
        localWarRadio.setBounds(120, 260, 100, 30);
        buildWarRadio.setBounds(220, 260, 100, 30);
        localWarRadio.setContentAreaFilled(false);
        buildWarRadio.setContentAreaFilled(false);
        localWarRadio.setFocusPainted(false);
        buildWarRadio.setFocusPainted(false);
        // 创建按钮组，把两个单选按钮添加到该组
        ButtonGroup btnGroup = new ButtonGroup();
        btnGroup.add(localWarRadio);
        btnGroup.add(buildWarRadio);
        // 设置第一个单选按钮选中
        buildWarRadio.setSelected(true);
        panel.add(localWarRadio);
        panel.add(buildWarRadio);
        // 设置按钮监听
        localWarRadio.addActionListener(e -> {
            mavenHis.setText("war包地址：");
            cmdHisComboBox.setEnabled(false);
        });
        buildWarRadio.addActionListener(e -> {
            mavenHis.setText("maven home：");
            cmdHisComboBox.setEnabled(true);
        });
        comRadioMap.put(localWarRadio.getName(), localWarRadio);
        comRadioMap.put(buildWarRadio.getName(), buildWarRadio);
    }

    private void componentLogFrom(JPanel panel) {
        // svn/git单选按钮
        JLabel logLabel = new JLabel("文件来源：");
        logLabel.setBounds(20, 300, 100, 30);
        panel.add(logLabel);
        // 创建两个单选按钮
        JRadioButton svnLogRadio = new JRadioButton("提交记录");
        svnLogRadio.setName("svnLog");
        JRadioButton patchFileRadio = new JRadioButton("Patch文件");
        patchFileRadio.setName("patchFile");
        svnLogRadio.setBounds(120, 300, 100, 30);
        patchFileRadio.setBounds(220, 300, 100, 30);
        svnLogRadio.setContentAreaFilled(false);
        patchFileRadio.setContentAreaFilled(false);
        svnLogRadio.setFocusPainted(false);
        patchFileRadio.setFocusPainted(false);
        // todo 暂时屏蔽
        patchFileRadio.setEnabled(false);
        // 创建按钮组，把两个单选按钮添加到该组
        ButtonGroup logBtnGroup = new ButtonGroup();
        logBtnGroup.add(svnLogRadio);
        logBtnGroup.add(patchFileRadio);
        // 设置第一个单选按钮选中
        svnLogRadio.setSelected(true);
        panel.add(svnLogRadio);
        panel.add(patchFileRadio);
        comRadioMap.put(svnLogRadio.getName(), svnLogRadio);
        comRadioMap.put(patchFileRadio.getName(), patchFileRadio);

        JLabel patchPathLabel = new JLabel("Patch文件路径：");
        patchPathLabel.setBounds(335, 300, 120, 30);
        panel.add(patchPathLabel);
        // 命令输入框
        JTextField patchPathText = new JTextField(200);
        patchPathText.setName("patchPath");
        patchPathText.setBounds(460, 300, 400, 30);
        patchPathText.setText("");
        // todo 暂时屏蔽
        patchPathText.setEnabled(false);
        panel.add(patchPathText);
        componentMap.put(patchPathText.getName(), patchPathText);
    }

    private String checkInput(ConfigModel configModel) {
        ConfigUser user = configModel.getUser();
        String url = configModel.getUrl();
        String startVersion = configModel.getStartVersion();
        String endVersion = configModel.getEndVersion();
        long startDate = configModel.getStartDate();
        long endDate = configModel.getEndDate();
        if (StringUtils.isBlank(url)) {
            return "svn/git地址不能为空";
        }
        if (user == null) {
            return "账号密码不能为空";
        }
        String userName = user.getUserName();
        String password = user.getPassword();
        if (StringUtils.isBlank(userName)) {
            return "账号不能为空";
        }
        if (StringUtils.isBlank(password) || StringUtils.isBlank(AllUtils.aesDecrypt(password))) {
            return "密码不能为空";
        }
        if (startVersion.length() > 0 && !AllUtils.isInteger(startVersion) && (!startVersion.contains(",") && !startVersion.contains("-"))) {
            return "版本号格式错误";
        }
        if (((StringUtils.isBlank(startVersion) && StringUtils.isBlank(endVersion))) && (0 == startDate || endDate == 0)) {
            return "版本和时间不能同时为空";
        }
        return "success";
    }

    /**
     * 获取输入的数据
     *
     * @return
     */
    private ConfigModel getConfig() {
        String version = componentMap.get("checkout_version").getText();
        String url = comboBoxMap.get("url").getEditor().getItem().toString();
        String userName = comboBoxMap.get("userName").getEditor().getItem().toString();
        String cmd = comboBoxMap.get("cmd").getEditor().getItem().toString();
        String mavenHome = comboBoxMap.get("mavenHome").getEditor().getItem().toString();
        String pwd = componentMap.get("pwd").getText();
        String startVersion = componentMap.get("startVersion").getText();
        //String endVersion = componentMap.get("endVersion").getText();
        Date dateStart = dateTimePickerStart.getDate();
        Date dateEnd = dateTimePickerEnd.getDate();
        ConfigModel configModel = new ConfigModel();
        configModel.setUrl(StringUtils.isBlank(url) ? "" : url);
        configModel.setCmd(StringUtils.isBlank(cmd) ? "" : cmd);
        configModel.setMavenHome(StringUtils.isBlank(mavenHome) ? "" : mavenHome);
        configModel.setStartVersion(StringUtils.isBlank(startVersion) ? "" : startVersion);
        //configModel.setEndVersion(StringUtils.isBlank(endVersion) ? "" : endVersion);
        configModel.setCheckoutVersion(StringUtils.isNotBlank(version) ? Integer.parseInt(version) : -1);
        configModel.setStartDate(dateStart == null ? 0L : dateStart.getTime());
        configModel.setEndDate(dateEnd == null ? 0L : dateEnd.getTime());
        ConfigUser configUser = new ConfigUser();
        if (defaultPwd.equals(pwd)) {
            configUser = configUserMap.get(userName);
        } else {
            configUser.setUserName(userName);
            // 加密
            configUser.setPassword(AllUtils.aesEncrypt(pwd));
        }
        configModel.setUser(configUser);

        // 单选
        JRadioButton svn = comRadioMap.get("svn");
        JRadioButton localWar = comRadioMap.get("localWar");
        JRadioButton svnLog = comRadioMap.get("svnLog");
        configModel.setSvnOrGit(svn.isSelected() ? 1 : 0);
        configModel.setLocalWar(localWar.isSelected() ? 1 : 0);
        configModel.setLogFrom(svnLog.isSelected() ? 1 : 0);

        JTextField patchPath = componentMap.get("patchPath");
        configModel.setPatchFilePath(StringUtils.isBlank(patchPath.getText()) ? "" : patchPath.getText());
        return configModel;
    }

    private ConfigJsonModel getConfigJsonModel(ConfigModel config) {
        if (config == null) {
            return null;
        }
        configJsonModel.setLastUseConfig(config);
        LinkedList<String> urlHistories = configJsonModel.getUrlHistories() == null ?
                new LinkedList<>() : configJsonModel.getUrlHistories();
        if (StringUtils.isNotBlank(config.getUrl())) {
            urlHistories.remove(config.getUrl());
            urlHistories.addFirst(config.getUrl());
        }
        LinkedList<ConfigUser> userHistories = configJsonModel.getUserHistories() == null ?
                new LinkedList<>() : configJsonModel.getUserHistories();
        if (config.getUser() != null && StringUtils.isNotBlank(config.getUser().getUserName())) {
            ConfigUser rm = null;
            for (ConfigUser userHistory : userHistories) {
                if (userHistory.getUserName().equals(config.getUser().getUserName())) {
                    rm = userHistory;
                    break;
                }
            }
            userHistories.remove(rm);
            userHistories.addFirst(config.getUser());
        }
        LinkedList<String> cmdHistories = configJsonModel.getCmdHistories() == null ?
                new LinkedList<>() : configJsonModel.getCmdHistories();
        if (StringUtils.isNotBlank(config.getCmd())) {
            cmdHistories.remove(config.getCmd());
            cmdHistories.addFirst(config.getCmd());
        }
        LinkedList<String> mavenHistories = configJsonModel.getMavenHistories() == null ?
                new LinkedList<>() : configJsonModel.getMavenHistories();
        if (StringUtils.isNotBlank(config.getMavenHome())) {
            mavenHistories.remove(config.getMavenHome());
            mavenHistories.addFirst(config.getMavenHome());
        }

        configJsonModel.setUrlHistories(urlHistories);
        configJsonModel.setUserHistories(userHistories);
        configJsonModel.setCmdHistories(cmdHistories);
        configJsonModel.setMavenHistories(mavenHistories);
        return configJsonModel;

    }

    private void versionComponent(JPanel panel) {
        JLabel startRevisionLabel = new JLabel("版本号（多个用,号，范围使用-号）：");
        startRevisionLabel.setBounds(20, 180, 250, 30);
        panel.add(startRevisionLabel);
        JTextField startRevisionText = new JTextField(220);
        startRevisionText.setName("startVersion");
        startRevisionText.setBounds(270, 180, 400, 30);
        startRevisionText.setText(currentConfig.getStartVersion());
        panel.add(startRevisionText);
        componentMap.put(startRevisionText.getName(), startRevisionText);

        JLabel startTime = new JLabel("开始时间：");
        startTime.setBounds(20, 220, 100, 30);
        panel.add(startTime);


        JLabel endTime = new JLabel("结束时间：");
        endTime.setBounds(335, 220, 100, 30);
        panel.add(endTime);
    }

    private void authComponent(JPanel panel) {
        JLabel userPwdLabel = new JLabel("svn/git密码：");
        userPwdLabel.setBounds(20, 140, 120, 30);
        panel.add(userPwdLabel);
        JTextField userPwdText = new JTextField(220);
        userPwdText.setName("pwd");
        userPwdText.setBounds(120, 140, 400, 30);
        userPwdText.setText(defaultPwd);
        panel.add(userPwdText);
        componentMap.put(userPwdText.getName(), userPwdText);

        JLabel userHis = new JLabel("svn/git账户：");
        userHis.setBounds(20, 100, 120, 30);
        panel.add(userHis);
        JComboBox<String> userHisComboBox = new JComboBox<>();
        userHisComboBox.setBounds(120, 100, 600, 30);
        LinkedList<ConfigUser> configUserList = configJsonModel.getUserHistories() == null ? new LinkedList<>() : configJsonModel.getUserHistories();
        if (configUserList.isEmpty()) {
            userHisComboBox.addItem(defaultItem);
        }
        configUserList.forEach(v -> {
            configUserMap.put(v.getUserName(), v);
            userHisComboBox.addItem(v.getUserName());
        });
        // 自定义悬浮框
        userHisComboBox.setRenderer(new JComboBoxRenderer());
        userHisComboBox.setSelectedIndex(0);
        userHisComboBox.setEditable(true);
        userHisComboBox.setName("userName");
        comboBoxMap.put(userHisComboBox.getName(), userHisComboBox);

        //itemListener
        ItemListener userHisComboBoxListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (ItemEvent.SELECTED == arg0.getStateChange()) {
                    String selectedItem = arg0.getItem().toString();
                    if (selectedItem.equals(defaultItem)) {
                        return;
                    }
                    ConfigUser configUser = configUserMap.get(selectedItem);
                    if (configUser != null) {
                        userPwdText.setText(defaultPwd);
                    }
                }
            }
        };
        userHisComboBox.addItemListener(userHisComboBoxListener);
        panel.add(userHisComboBox);

        // 从项目中读取
        JLabel loadAuth = new JLabel("尝试从本机中读取：");
        loadAuth.setBounds(580, 140, 160, 30);
        panel.add(loadAuth);

        JButton loadPad = new JButton("读取");
        loadPad.setBounds(740, 140, 100, 30);
        panel.add(loadPad);
        loadPad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("你按下了" + loadPad.getText());
                // 读取密码
                try {
                    HashMap<String, String> svnAuth = Program.getSvnAuth();
                    String userName = svnAuth.get("userName");
                    String password = svnAuth.get("password");
                    if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
                        userHisComboBox.getEditor().setItem(userName);
                        userPwdText.setText(password);
                    } else {
                        userPwdText.setText("");
                    }
                } catch (Exception ee) {
                    // ignore
                    log.error("读取账号密码失败",ee);
                }

            }
        });
    }

    private void urlComponent(JPanel panel) {
        JLabel urlHis = new JLabel("svn/git地址：");
        urlHis.setBounds(20, 60, 120, 30);
        panel.add(urlHis);
        JComboBox<String> urlHisComboBox = new JComboBox<>();
        urlHisComboBox.setBounds(120, 60, 600, 30);
        LinkedList<String> list = configJsonModel.getUrlHistories() == null ? new LinkedList<>() : configJsonModel.getUrlHistories();
        if (list.isEmpty()) {
            urlHisComboBox.addItem(defaultItem);
        }
        list.forEach(urlHisComboBox::addItem);
        // 自定义悬浮框
        urlHisComboBox.setRenderer(new JComboBoxRenderer());
        urlHisComboBox.setSelectedIndex(0);
        urlHisComboBox.setName("url");
        urlHisComboBox.setEditable(true);
        comboBoxMap.put(urlHisComboBox.getName(), urlHisComboBox);

        //itemListener
        ItemListener urlHisComboBoxListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
            }
        };
        urlHisComboBox.addItemListener(urlHisComboBoxListener);
        panel.add(urlHisComboBox);

        // svn/git单选按钮
        JLabel svnOrGitLabel = new JLabel("svn/git：");
        svnOrGitLabel.setBounds(240, 20, 100, 30);
        panel.add(svnOrGitLabel);
        // 创建两个单选按钮
        JRadioButton svnRadio = new JRadioButton("svn");
        svnRadio.setName("svn");
        JRadioButton gitRadio = new JRadioButton("git");
        gitRadio.setName("git");
        // todo git先屏蔽
        gitRadio.setEnabled(false);
        svnRadio.setBounds(320, 20, 50, 30);
        gitRadio.setBounds(390, 20, 50, 30);
        svnRadio.setContentAreaFilled(false);
        gitRadio.setContentAreaFilled(false);
        svnRadio.setFocusPainted(false);
        gitRadio.setFocusPainted(false);
        // 创建按钮组，把两个单选按钮添加到该组
        ButtonGroup svnGitBtnGroup = new ButtonGroup();
        svnGitBtnGroup.add(svnRadio);
        svnGitBtnGroup.add(gitRadio);
        // 设置第一个单选按钮选中
        svnRadio.setSelected(true);
        panel.add(svnRadio);
        panel.add(gitRadio);
        comRadioMap.put(svnRadio.getName(), svnRadio);
        comRadioMap.put(gitRadio.getName(), gitRadio);
    }

    /**
     * 目标版本
     *
     * @param panel
     */
    private void targetVersionComponent(JPanel panel) {
        JLabel patchPathLabel = new JLabel("checkout版本：");
        patchPathLabel.setBounds(20, 20, 100, 30);
        panel.add(patchPathLabel);
        // 命令输入框
        JTextField patchPathText = new JTextField(200);
        patchPathText.setName("checkout_version");
        patchPathText.setBounds(120, 20, 100, 30);
        patchPathText.setText("");
        panel.add(patchPathText);
        componentMap.put(patchPathText.getName(), patchPathText);
    }

    /**
     * 显示日志
     */
    private void showLog() {
        for (; ; ) {
            try {
                outputTextArea.append(logQueue.take());
                outputTextArea.append("\n");
                //使垂直滚动条自动向下滚动
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

}

