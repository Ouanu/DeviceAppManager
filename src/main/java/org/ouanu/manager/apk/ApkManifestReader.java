package org.ouanu.manager.apk;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.IconFace;
import org.ouanu.manager.model.Application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class ApkManifestReader {

    public static boolean isApkFile(File file) {
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        try(ApkFile ignored = new ApkFile(file)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String readPackageName(File file) {
        if (!file.exists() || !file.isFile()) {
            return "";
        }
        try(ApkFile apkFile = new ApkFile(file)) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            return apkMeta.getPackageName();
        } catch (IOException e) {
            System.out.println("Apk Reader -------- " + e.getMessage());
            return "";
        }
    }

    public static Application readApplicationInfo(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        try(ApkFile apkFile = new ApkFile(file)) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            Handler handler = new ConsoleHandler();
            handler.setEncoding("UTF-8");
            Logger logger = Logger.getLogger(ApkManifestReader.class.getName());
            logger.addHandler(handler);
            logger.setUseParentHandlers(false);

            StringBuilder sb = new StringBuilder();
            Set<String> labels = new HashSet<>();
            for (Locale locale : apkFile.getLocales()) {
                apkFile.setPreferredLocale(locale);
                ApkMeta apkMeta1 = apkFile.getApkMeta();
                if (labels.add(apkMeta1.getLabel())) {
                    if (locale.getLanguage().isEmpty()) {
                        sb.append("default");
                    } else {
                        sb.append(locale.getLanguage());
                    }
                    sb.append("=").append(apkMeta1.getLabel()).append(",");
                }
            }

            List<IconFace> allIcons = apkFile.getAllIcons();
            IconFace maxIcon = null;

            if (!allIcons.isEmpty()) {
                for (IconFace icon : allIcons) {
                    if (maxIcon == null) {
                        maxIcon = icon;
                        continue;
                    }
                    if (maxIcon.getData().length < icon.getData().length) {
                        maxIcon = icon;
                    }
                    logger.info(apkMeta.getPackageName() + " " + "icon byte size: " + icon.getData().length);
                }
            }

            File iconFile = new File("./icons", apkMeta.getPackageName() + ".jpg");
            if (maxIcon != null) {
                FileOutputStream fos = new FileOutputStream(iconFile);
                fos.write(maxIcon.getData());
                fos.flush();
                fos.close();
            }

            Application build = Application.builder()
                    .packageName(apkMeta.getPackageName())
                    .label(apkMeta.getLabel())
                    .versionCode(apkMeta.getVersionCode())
                    .versionName(apkMeta.getVersionName())
                    .size(file.length())
                    .fileName(file.getName())
                    .iconName(iconFile.getName())
                    .uploadTime(LocalDateTime.now())
                    .appNames(sb.toString())
                    .build();

            System.out.println("version code = " + build.getVersionCode() );
            System.out.println("app names = " + build.getAppNames() );
            return build;
        } catch (IOException e) {
            System.out.println("Read application info: " + e.getMessage());
            return null;
        }
    }
    public static void readManifest(String apkPath) {
        File file = new File(apkPath);
        if (!file.exists() || !file.isFile()) {
            return;
        }
        try(ApkFile apkFile = new ApkFile(file)) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            System.out.println("App Name: " + apkMeta.getLabel());
            System.out.println("Package Name: " + apkMeta.getPackageName());
            System.out.println("Version Code: " + apkMeta.getVersionCode());
            System.out.println("Version Name: " + apkMeta.getVersionName());
            System.out.println("Min SDK Version: " + apkMeta.getMinSdkVersion());
            System.out.println("Target SDK Version: " + apkMeta.getTargetSdkVersion());
            System.out.println("App Size: " + file.length() / 1024f / 1024f + "MB");
            System.out.println("Shared User Label: " + apkMeta.getInstallLocation());
            Set<Locale> locales = apkFile.getLocales();
            Handler handler = new ConsoleHandler();
            handler.setEncoding("UTF-8");
            Logger logger = Logger.getLogger(ApkManifestReader.class.getName());
            logger.addHandler(handler);
            logger.setUseParentHandlers(false);
//            for (Locale locale : locales) {
//                apkFile.setPreferredLocale(locale);
//                ApkMeta localeMeta = apkFile.getApkMeta();
//                logger.info(locale.getLanguage() + "=" + localeMeta.getLabel());
//            }
        } catch (IOException e) {
            System.out.println("Apk Reader -------- " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


}
