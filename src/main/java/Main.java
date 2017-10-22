import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
	log.info("Starting file backup manager");

	// Getting properties from properties file
	Properties properties = getProperties();
	if (properties == null) {
	    log.error("Couldn't load application properties");
	    return;
	}

	// Folders
	String backupFolderPath = properties.getProperty("to");
	String fileFolderPath = properties.getProperty("from");

	// File Names
	String fileName = properties.getProperty("fileToBackupName");
	String backupFileName = properties.getProperty("backupFileName");

	// Date vars
	DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMddHHmmss");

	// Files to manipulate
	File fileToBackup = new File(StringUtils.join(fileFolderPath, fileName));
	File backupFile = new File(StringUtils.join(backupFolderPath, new DateTime().toString(dtf), backupFileName));

	if (!fileToBackup.exists()) {
	    log.error("The file to backup doesn't exists");
	    return;
	}

	// Do the copy
	log.info("Creating backup...");
	Files.copy(fileToBackup.toPath(), backupFile.toPath());

	// Deletes old backup files
	log.info("Deleting old backups...");
	File backupFolder = new File(backupFolderPath);
	File[] filesInBackupFolder = backupFolder.listFiles();
	File[] listOfBackupFiles = Arrays.asList(filesInBackupFolder)//
		.stream()//
		.filter(file -> StringUtils.endsWith(file.getName(), backupFileName))//
		.sorted()//
		.toArray(File[]::new);

	if (listOfBackupFiles.length > 3) {
	    for (int i = 0; i < listOfBackupFiles.length - 3; i++) {
		if (listOfBackupFiles[i].delete()) {
		    log.info("This file has been deleted : " + listOfBackupFiles[i]);
		} else {
		    log.error("Probleme while deleting : " + listOfBackupFiles[i]);
		}
	    }
	} else {
	    log.info("... No backup file deleted !");
	}

	log.info("Done !");
    }

    /**
     * Get properties from config file
     * 
     * @return Properties of app
     */
    private static Properties getProperties() {
	Properties properties = null;
	try {
	    properties = new Properties();
	    properties.load(new FileInputStream(new File("src\\main\\resources\\config.properties")));
	} catch (IOException e) {
	    log.error("Error while loading config file", e);
	}

	return properties;
    }

}
