package org.ouanu.manager.apk;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
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
