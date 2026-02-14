package com.MedLab.dicom_converter_web.controller;
import com.MedLab.dicom_converter_web.domain.DicomEntity;
import com.MedLab.dicom_converter_web.repository.DicomRepository;
import com.MedLab.dicom_converter_web.service.DicomService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/dicom")
@RequiredArgsConstructor
public class DicomController {

    private final DicomService dicomService;
    private final DicomRepository dicomRepository;
    //파일 업로드(POST)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Long savedId = dicomService.uploadAndSaveDicom(file);

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedId);
            response.put("message","Upload Success");

            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }

    }

    //변환 실행(POST)
    @PostMapping("/convert/{id}")
    public ResponseEntity<?> convertFile(@PathVariable Long id) {
        try {
            dicomService.convertDicomToPng(id);

            Map<String, Object> response = new HashMap<>();
            response.put("id",id);
            response.put("status","PROCESSING");
            response.put("message","Conversion Started");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("변환 실패: " + e.getMessage());
        }

    }

    //전체 이력 조회 (GET)
    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        return ResponseEntity.ok("History List Placeholder");
    }

    //상세 조회(GET)
    @GetMapping("/history/{id}")
    public ResponseEntity<?> getDetail(@PathVariable int id) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("fileName", "test_image.dcm");
        response.put("status", "SUCCESS");

        //front-end 다운로드 버튼이 호출할 API 주소를 미리 알려줌.
        response.put("downloadUrl", "/api/dicom/download/" + id);
        return ResponseEntity.ok(response);
    }

    //파일 다운로드 엔드포인트
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            DicomEntity dicom = dicomRepository.findById(id)
                    .orElseThrow(()-> new RuntimeException("해당 ID의 파일을 찾을 수 없습니다." + id));
            Path path = Paths.get(dicom.getFilePath());
            Resource resource = new FileSystemResource(path);

            if (!resource.exists()) {
                return ResponseEntity.status(404).build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName().toString()+"\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }



    }

}
