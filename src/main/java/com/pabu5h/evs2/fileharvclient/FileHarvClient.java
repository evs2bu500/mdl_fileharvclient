package com.pabu5h.evs2.fileharvclient;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

@Service
public class FileHarvClient {

    @Value("${fileharv.path}")
    private String fileHarvPath;
    @Value("${fileharv.ept.upload}")
    private String fileHarvEptUpload;

    @Autowired
    private RestTemplate restTemplate;

    private final Logger logger;

    public FileHarvClient(Logger logger) {
        this.logger = logger;
    }

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
            for (File logFile : logsDir.listFiles()) {
                if(logger != null) {
                    logger.info("Moving log file: {} from Dir: {}", logFile.getName(), logsDir.getName());
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
                        logger.info("Uploaded log file: {} to desDir: {}", logFile.getName(), desFolder);
                    }
                    logFile.delete();
                }
            }
        } catch (Exception e) {
            if(logger != null) {
                logger.info("Error in moving log files! : {}", e.getMessage());
            }
        }
    }

}
