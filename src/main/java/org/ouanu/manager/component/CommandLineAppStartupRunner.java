package org.ouanu.manager.component;

import org.ouanu.manager.iface.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {

    private final StorageService storageService;

    @Autowired
    public CommandLineAppStartupRunner(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void run(String... args) {
        // 应用启动时初始化存储服务
        storageService.init();
    }
}