package com.project.demo.controller;

import com.project.demo.entity.Upload;
import com.project.demo.service.UploadService;

import com.project.demo.controller.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

/**
 * (Upload)表控制层
 *
 */
@RestController
@RequestMapping("upload")
public class UploadController extends BaseController<Upload, UploadService> {
    /**
     * 服务对象
     */
    @Autowired
    public UploadController(UploadService service) {
        setService(service);
    }

    /**
     * 处理上传文件的GET请求，返回文件内容
     */
    @GetMapping("/{filename}")
    public ResponseEntity<FileSystemResource> getFile(@PathVariable String filename) throws IOException {
        String filePath = System.getProperty("user.dir") + "\\target\\classes\\static\\upload\\";
        File file = new File(filePath + filename);
        if (file.exists()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new FileSystemResource(file));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}


