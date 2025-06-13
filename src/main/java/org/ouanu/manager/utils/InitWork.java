package org.ouanu.manager.utils;

import org.ouanu.manager.apk.ApkManifestReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

@Component
public class InitWork {

    @Value("${file.upload-dir}")
    private String uploadDirPath;

    public void checkInit() {
        Handler handler = new ConsoleHandler();
        try {
            handler.setEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        Logger logger = Logger.getLogger(ApkManifestReader.class.getName());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);

        checkAppDir(logger);
    }

    public void checkAppDir(Logger logger) {
        File file = new File(uploadDirPath);
        if (!file.exists() || !file.isDirectory()) {
            if (!file.mkdirs()) {
                throw new RuntimeException("Cannot create an upload directory.");
            }
            logger.info("Upload directory does exists.");
        }
        throw new RuntimeException("Cannot create an upload directory.");
    }


}
