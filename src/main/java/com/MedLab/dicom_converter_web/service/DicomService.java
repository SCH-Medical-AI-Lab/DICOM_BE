package com.MedLab.dicom_converter_web.service;

import com.MedLab.dicom_converter_web.domain.DicomEntity;
import com.MedLab.dicom_converter_web.repository.DicomRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DicomService {
    private final DicomRepository dicomRepository; // Constructor Dependency Injection

    private final String uploadDir = "E:/Medical Dicom Project/storage";

    // 파일을 저장하고 DB에 기록하는 핵심 로직
    //리포지토리에 정의한 Native Query를 호출하는 방식
    @Transactional // @Transactional이 붙은 메서드 내 모든 작업은 성공 시 commit, 예외 발생 시 rollback 처리되어, 데이터가 일부만 반영되는 것을 방지합니다.
    public Long uploadAndSaveDicom(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String savedName = String.valueOf(UUID.randomUUID()) + "_" + originalFilename;
        Path savePath = Paths.get(uploadDir, savedName); // + 또는 , 가 있는데 +는 둘을 붙여버리고 , 는 OS에 맞는 구분자를 넣어준다.

        // 2. 물리적 폴더에 파일 저장
        File targetFile = new File(uploadDir);
        if (!targetFile.exists()) { //폴더에 없으면 생성.
            targetFile.mkdirs(); //mkdirs 폴더 생성인데, 상위까지 디렉토리까지 자동으로 만듦. 훨씬 안전 mkdir은 해당 디렉토리만 만들고 상위경로가 맞지 않으면 실패함.
        }
        // System.out.println("실제 저장 경로 : " + savePath.toAbsolutePath());
        file.transferTo(savePath);

        // 3. 초기값 설정 (DICOM이 아닐 경우 대비)
        String pName = "Unknown", pId ="Unknown", sDate = "", mod = "", sUid = "";

        // 4. DICOM 데이터 추출
        try (DicomInputStream dis = new DicomInputStream(savePath.toFile())) {
            Attributes attributes = dis.readDataset(-1,-1);
            pName = attributes.getString(Tag.PatientName, "Unknown");
            pId = attributes.getString(Tag.PatientID, "Unknown");
            sDate = attributes.getString(Tag.StudyDate, "");
            mod = attributes.getString(Tag.Modality, "");
            sUid = attributes.getString(Tag.StudyInstanceUID, "");
        } catch (Exception e) {
            System.out.println("DICOM 파일이 아니거나 정보를 읽을 수 없음" + e.getMessage());
        }

        //4. DicomEntity 객체 생성 및 save() 호출
        DicomEntity dicomEntity = new DicomEntity();
        dicomEntity.setFilePath(savePath.toString());
        dicomEntity.setPatientName(pName);
        dicomEntity.setPatientId(pId);
        dicomEntity.setStudyDate(sDate);
        dicomEntity.setModality(mod);
        dicomEntity.setStudyUid(sUid);
        dicomEntity.setUploadDate(LocalDateTime.now()); // 현재 시간 저장

        // JPA 의 save는 저장된 후 DB의 ID가 채워진 객체를 반환
        DicomEntity savedEntity = dicomRepository.save(dicomEntity);

        // 5. 실제 DB에 저장된 ID 반환
        return savedEntity.getId();
    }

    public void convertDicomToPng(Long id) throws Exception {
        // 1. DB에서 파일 경로 가져오기
        DicomEntity entity = dicomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 파일을 찾을 수 없습니다. ID: " + id));

        // 있으면 파일 경로로 파일 가져오기
        File dicomFile = new File(entity.getFilePath());

        // 2. DICOM 읽기 준비
        Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
        if (!iter.hasNext()) {
            throw new RuntimeException("DICOM 리더를 찾을 수 없습니다. 라이브러리 설정을 확인해 보세요");
        }
        ImageReader reader = iter.next();

        try(ImageInputStream imageInputStream = ImageIO.createImageInputStream(dicomFile)) {
            reader.setInput(imageInputStream, false);

            // 3. DICOM의 첫 번째 프레임을 읽어 이미지로 변환
            BufferedImage bufferedImage = reader.read(0);

            // 4. 저장할 PNG 경로 설정 (기존 파일명에서 확장자만 변경)
            String pngPath = entity.getFilePath().replaceAll("(?i)\\.dcm$", ".png"); // 파일 확장자가 .DCM / ,dcm이든 모두 .png로 바꿈.
            File outputPng = new File(pngPath);

            // 5. PNG 파일로 실제 저장
            ImageIO.write(bufferedImage,"png", outputPng);
            System.out.println("변환 완료! 저장 경로: " + pngPath);
        }
    }
}
