package org.ouanu.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("org.ouanu.manager")
public class ManagerApplication {

	public static void main(String[] args) {

		SpringApplication.run(ManagerApplication.class, args);
//		try {
////			File file = new File("C:\\Users\\Administrator\\Desktop\\netflix.apk");
////			ApkManifestReader.readManifest("C:\\Users\\Administrator\\Desktop\\崩溃.txt");
//			boolean apkFile = ApkManifestReader.isApkFile(new File("C:\\Users\\Administrator\\Desktop\\崩溃.txt"));
//			System.out.println("The file is Apk: " + apkFile);
//		} catch (Exception e) {
//			System.out.println("Error parsing APK: " + e.getMessage());
//		}

	}

}
