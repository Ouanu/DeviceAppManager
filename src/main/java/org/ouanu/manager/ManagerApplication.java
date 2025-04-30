package org.ouanu.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileNotFoundException;

@SpringBootApplication
public class ManagerApplication {

	public static void main(String[] args) throws FileNotFoundException {
		SpringApplication.run(ManagerApplication.class, args);

		File file = new File("./package");
		if (!file.exists() || !file.isDirectory()) {
            boolean result = file.mkdirs();
			if (!result) {
				throw new FileNotFoundException("package dir cannot be made.");
			}
        }
	}

}
