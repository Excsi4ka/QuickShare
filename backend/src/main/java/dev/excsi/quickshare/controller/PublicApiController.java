package dev.excsi.quickshare.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/")
public class PublicApiController {

    @GetMapping("/download/{fileHash}")
    public String getDownloadLink(@PathVariable String fileHash) {
        return "";
    }
}
