package org.ouanu.manager.command;

import org.ouanu.manager.model.Application;

public record ApplicationCreateCommand(
        String packageName,
        String label,
        String versionName,
        Long versionCode,
        String appNames,
        Long size,
        String fileName
) {

    @Override
    public String packageName() {
        return packageName;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String versionName() {
        return versionName;
    }

    @Override
    public Long versionCode() {
        return versionCode;
    }

    @Override
    public String appNames() {
        return appNames;
    }

    @Override
    public Long size() {
        return size;
    }

    @Override
    public String fileName() {
        return fileName;
    }

    public Application toEntity() {
        return Application.builder()
                .packageName(packageName)
                .label(label)
                .versionName(versionName)
                .versionCode(versionCode)
                .appNames(appNames)
                .size(size)
                .fileName(fileName)
                .build();
    }
}
