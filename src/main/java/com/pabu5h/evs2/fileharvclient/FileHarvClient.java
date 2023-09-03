package com.pabu5h.evs2.fileharvclient;

//import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

@Service
public class FileHarvClient {
    private final Logger logger = Logger.getLogger(FileHarvClient.class.getName());

    @Value("${fileharv.path}")
    private String fileHarvPath;
    @Value("${fileharv.ept.upload}")
    private String fileHarvEptUpload;

    @Autowired
    private RestTemplate restTemplate;

//    public FileHarvClient(Logger logger) {
//        this.logger = logger;
//    }
    public void uploadFile(File file, String folder) throws Exception {
        if(folder == null){
            throw new Exception("FileHarvClient: folder name cannot be null");
        }
        String endpoint = fileHarvPath + fileHarvEptUpload;
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("file", new FileSystemResource(new File("/path/to/your/file")));
        body.add("file", new FileSystemResource(file));
        body.add("folder", folder);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("File "+ file.getName() +" uploaded successfully");
        } else {
            System.out.println("Error uploading file: " + response.getBody());
        }
    }

    public void moveLogFiles(String srcFolder, String desFolder, String currentLogFile) throws Exception {
        if (srcFolder == null) {
            srcFolder = "logs";
        }
        String endpoint = fileHarvPath + fileHarvEptUpload;
        try {
            File logsDir = new File(srcFolder);
//            File[] lf = logsDir.listFiles();
//            if(lf != null){
//                logger.info("list of files in logsDir: {}", logsDir.getAbsoluteFile());
//                for(File f : lf){
//                    logger.info("File: {}", f.getAbsoluteFile());
//                }
//            }
            for (File logFile : Objects.requireNonNull(logsDir.listFiles())) {
                if(logger != null) {
                    logger.info("logFile: " + logFile.getName());
                }
                String fileName = logFile.getName();
                if (fileName.endsWith(".log")) {
                    if (currentLogFile != null ) {
                        if (fileName.equals(currentLogFile)) {
                            continue;
                        }
                    }
                    uploadFile(logFile, desFolder);
                    if(logger != null) {
                        logger.info("Uploaded log file: " + logFile.getName() + " to " + desFolder);
                    }
                    logFile.delete();
                }
            }
        } catch (Exception e) {
            if(logger != null) {
                logger.info("Error in moving log files! : " + e.getMessage());
            }
        }
    }

}
