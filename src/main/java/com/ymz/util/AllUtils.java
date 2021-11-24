package com.ymz.util;

import com.ymz.ui.SvnGUI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author: ymz
 * @date: 2021-08-18 21:57
 **/
@Slf4j
public class AllUtils {
    /**
     * 加解密密钥, 外部可以
     */
    public static final String AES_DATA_SECURITY_KEY = "4%Da}!@g5LG)oTvJ";
    /**
     * 算法/加密模式/填充方式
     */
    private static final String AES_PKCS5P = "AES/ECB/PKCS5Padding";

    /**
     * 加密
     *
     * @param str 需要加密的字符串
     * @param key 密钥
     * @return
     * @throws Exception
     */
    private static String encrypt(String str, String key) {
        if (StringUtils.isEmpty(key)) {
            throw new RuntimeException("key不能为空");
        }
        try {
            if (str == null) {
                return null;
            }
            // 判断Key是否为16位
            if (key.length() != 16) {
                return null;
            }
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            // "算法/模式/补码方式"
            Cipher cipher = Cipher.getInstance(AES_PKCS5P);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            return base64Encode(encrypted);
        } catch (Exception ex) {
            return null;
        }

    }

    /**
     * 解密
     *
     * @param str 需要解密的字符串
     * @param key 密钥
     * @return
     */
    private static String decrypt(String str, String key) {
        if (StringUtils.isEmpty(key)) {
            throw new RuntimeException("key不能为空");
        }
        try {
            if (str == null) {
                return null;
            }
            // 判断Key是否为16位
            if (key.length() != 16) {
                return null;
            }
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(AES_PKCS5P);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            // 先用base64解密
            byte[] encrypted = base64DecodeByte(str);
            try {
                byte[] original = cipher.doFinal(encrypted);
                String originalString = new String(original, StandardCharsets.UTF_8);
                return originalString;
            } catch (Exception e) {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * base 64 加密
     *
     * @param bytes 待编码的byte[]
     * @return 编码后的base 64 code
     */
    public static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * base 64 加密
     *
     * @param bytes 待编码的byte[]
     * @return 编码后的base 64 code
     */
    public static byte[] base64EncodeByte(byte[] bytes) {
        return Base64.getEncoder().encode(bytes);
    }

    /**
     * base 64 解密
     *
     * @param base64Code 待解码的base 64 code
     * @return 解码后的byte[]
     */
    public static byte[] base64DecodeByte(String base64Code) {
        return Base64.getDecoder().decode(base64Code);
    }

    /**
     * 加密
     *
     * @param str 需要加密的字符串
     * @return
     * @throws Exception
     */
    public static String aesEncrypt(String str) {
        return encrypt(str, AES_DATA_SECURITY_KEY);
    }

    /**
     * 解密
     *
     * @param str 需要解密的字符串
     * @return
     */
    public static String aesDecrypt(String str) {
        return decrypt(str, AES_DATA_SECURITY_KEY);
    }

    /**
     * 将二进制转换为10进制
     *
     * @param bi
     * @return
     */
    public static Integer biannary2Decimal(int bi) {

        String binStr = bi + "";
        int sum = 0;
        int len = binStr.length();
        for (int i = 1; i <= len; i++) {
            int dt = Integer.parseInt(binStr.substring(i - 1, i));
            sum += (int) Math.pow(2, len - i) * dt;
        }
        return sum;
    }

    /**
     * @param str
     * @return
     */
    public static String classToJava(String str) {
        String javaName = str;
        if (str.contains("$")) {
            javaName = StringUtils.substringBefore(str, "$");
            if (str.contains(".class")) {
                javaName = javaName + ".class";
            }
        }
        return javaName.replaceAll("\\.class", "\\.java");
    }

    /**
     * 转换斜杠
     *
     * @param str
     * @return
     */
    public static String replaceFileSeparatorToLinux(String str) {
        str = str.replaceAll("\\\\", "/");
        return str;
    }

    /**
     * 递归获取所有文件
     *
     * @param folder
     * @return
     */
    public static List<File> getAllFiles(File folder) {
        List<File> result = new ArrayList<>();
        if (folder.isFile()) {
            result.add(folder);
        }
        File[] subFolders = folder.listFiles();
        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isFile()) {
                    result.add(file);
                } else {
                    result.addAll(getAllFiles(file));
                }
            }
        }
        return result;
    }

    /**
     * 递归获取所有符合名称的文件
     *
     * @param folder
     * @return
     */
    public static List<File> foundFile(File folder, String fileName) {
        List<File> result = new ArrayList<>();
        if (folder.isFile()) {
            result.add(folder);
        }
        File[] subFolders = folder.listFiles();
        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isFile()) {
                    if (file.getName().endsWith(fileName)) {
                        result.add(file);
                    }
                } else {
                    result.addAll(getAllFiles(file));
                }
            }
        }
        return result;
    }

    /**
     * 删除空文件夹
     *
     * @param dir
     */
    public static void clearEmptyDir(File dir) {
        File[] dirs = dir.listFiles();
        if (dirs != null) {
            for (File file : dirs) {
                if (file.isDirectory()) {
                    clearEmptyDir(file);
                }
            }
            if (dir.isDirectory()) {
                dir.delete();
            }
        }
    }

    /**
     * description 获取CPU序列号
     */
    public static String getCpuId() {
        try {
            // linux，windows命令
            String[] linux = {"dmidecode", "-t", "processor", "|", "grep", "'ID'"};
            String[] windows = {"wmic", "cpu", "get", "ProcessorId"};
            // 获取系统信息
            String property = System.getProperty("os.name").toLowerCase();
            Process process = Runtime.getRuntime().exec(property.contains("window") ? windows : linux);
            process.getOutputStream().close();
            Scanner sc = new Scanner(process.getInputStream(), "utf-8");
            sc.next();
            return sc.next();
        } catch (Exception e) {
            log.warn("get cpu id error:{}", e.getMessage());
        }
        return null;
    }

    /**
     * description 获取Mac地址
     */
    public static String getMacAddress() {
        try {
            // 获取本地IP对象
            InetAddress ia = InetAddress.getLocalHost();
            InetAddress[] inetAddressArr = InetAddress.getAllByName(ia.getHostName());
            for (int i = 0; i < inetAddressArr.length; i++) {
                if (inetAddressArr[i].getHostAddress() != null) {
                    String ip = inetAddressArr[i].getHostAddress();
                    if (!(ip.endsWith(".1") || ip.endsWith(".0") || ip.endsWith(".255"))) {
                        ia = inetAddressArr[i];
                        break;
                    }
                }
            }
            // 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
            // 下面代码是把mac地址拼装成String
            StringBuilder sb = new StringBuilder();
            // 解析mac地址
            parseMac(mac, sb);
            // 把字符串所有小写字母改为大写成为正规的mac地址并返回
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            log.warn("get mac address error:{}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取主机名
     *
     * @return
     */
    public static String getHostName() {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            String host = ia.getHostName();
            return host;
        } catch (Exception e) {
            log.warn("get host name error:{}", e.getMessage());
        }
        return null;
    }

    /**
     * description 解析Mac地址
     */
    private static void parseMac(byte[] mac, StringBuilder sb) {
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            // mac[i] & 0xFF 是为了把byte转化为正整数
            String s = Integer.toHexString(mac[i] & 0xFF);
            sb.append(s.length() == 1 ? 0 + s : s);
        }
    }

    /**
     * 获取jar包运行路径
     *
     * @return
     */
    public static String getJarPath() {
        String path = SvnGUI.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
            path = StringUtils.substringBeforeLast(path, File.separator);
            path = AllUtils.replaceFileSeparatorToLinux(path);
            if (path.startsWith("/")) {
                path = StringUtils.substringAfter(path, "/");
            }
            log.info("程序路径：{}", path);
        } catch (UnsupportedEncodingException e) {
            log.error("获取程序路径失败", e);
        }
        return path;
    }

    /**
     * 复制文件到指定目录
     *
     * @param srcFile
     * @param destPath
     */
    public static void copyFile(File srcFile, String destPath) {
        try {
            FileUtils.copyFile(srcFile, new File(destPath));
        } catch (IOException e) {
            log.error("复制文件失败", e);
        }
    }

    /**
     * 解压war包到指定目录
     *
     * @param war
     * @param destPath
     * @return
     */
    public static boolean unzipWar(File war, String destPath) {
        return unZipWar(war.toPath(), Paths.get(destPath));
    }

    /**
     * 解压war文件
     *
     * @param warPath   war文件
     * @param unZipPath 解压到的路径
     */
    private static boolean unZipWar(Path warPath, Path unZipPath) {
        String warName = warPath.toFile().getName();
        log.debug("warName: " + warName);
        log.debug("unZipPath: " + unZipPath.toString());
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(warPath.toFile()));
             ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.JAR, bis)) {
            if (Files.exists(unZipPath)) {
                Files.delete(unZipPath);
            }
            Files.createDirectories(unZipPath);
            JarArchiveEntry entry;
            while ((entry = (JarArchiveEntry) ais.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    Path currentPath = Paths.get(unZipPath.toString(), entry.getName());
                    if (!Files.exists(currentPath.getParent())) {
                        Files.createDirectories(currentPath);
                    } else if (!Files.exists(currentPath)) {
                        Files.createDirectory(currentPath);
                    }
                } else {
                    Path currentPath = unZipPath.resolve(entry.getName());
                    if (!Files.exists(currentPath.getParent())) {
                        Files.createDirectories(currentPath.getParent());
                    }
                    try (OutputStream os = Files.newOutputStream(currentPath)) {
                        IOUtils.copy(ais, os);
                    }
                }
            }
        } catch (ArchiveException | IOException e) {
            log.error("unpack war error:{}", warName, e);
            return false;
        }
        return true;
    }

    /**
     * 创建 war 文件
     *
     * @param sourceDir
     */
    public static boolean zipWar(Path sourceDir) {
        sourceDir = sourceDir.toAbsolutePath();
        String rootDir = sourceDir.toString();
        log.debug("rootDir: " + rootDir);
        rootDir = StringUtils.appendIfMissing(rootDir, File.separator);
        Path warPath = sourceDir.getParent().resolve(StringUtils.appendIfMissing(sourceDir.getFileName().toString(), ".war"));
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(warPath));
             ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.JAR, bos)) {
            Iterator<File> files = FileUtils.iterateFiles(sourceDir.toFile(), null, true);
            log.debug("rootDir: " + rootDir);
            while (files.hasNext()) {
                File file = files.next();
                ZipArchiveEntry entry = new ZipArchiveEntry(file, StringUtils.remove(file.getAbsolutePath(), rootDir));
                aos.putArchiveEntry(entry);
                try (InputStream is = new FileInputStream(file)) {
                    IOUtils.copy(is, aos);
                }
                aos.closeArchiveEntry();
            }
            aos.flush();
            log.info("压缩war包成功：{}",warPath.toString());
        } catch (IOException | ArchiveException e) {
            log.error("pack war error", e);
            return false;
        }
        return true;
    }


    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }



}
