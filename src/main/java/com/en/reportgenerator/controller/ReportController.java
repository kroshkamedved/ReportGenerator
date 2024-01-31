package com.en.reportgenerator.controller;

import com.en.reportgenerator.dto.ReportDTO;
import com.en.reportgenerator.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@CrossOrigin("${allowed.request.origin}")
@RequestMapping("/report")
@RestController()
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping(path = "/pdf", consumes = {"application/json"})
    public ResponseEntity<ByteArrayResource> generatePDF(@RequestBody ReportDTO reportData) throws IOException {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-hhmm"));
        String filename = date + "exp" + reportData.getExperiment().id() + ".pdf";
        PDDocument document = reportService.generatePDF(reportData);
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        document.save(byteArray);
        document.close();
        return ResponseEntity.status(HttpStatus.OK)
                .allow(HttpMethod.POST)
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .body(new ByteArrayResource(byteArray.toByteArray()));
    }
}
