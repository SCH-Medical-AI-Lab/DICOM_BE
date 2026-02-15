package com.MedLab.dicom_converter_web.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity //자바 객체(Class)를 관계형 데이터베이스의 테이블과 매핑하여 JPA(Java Persistence API)가 관리하도록 지정하는 역할
@Table(name = "dicom_info") //Table 이름
public class DicomEntity {
    @Id //데이터베이스 테이블의 기본 키(Primary Key, PK)를 매핑하는 애노테이션
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 512)
    private String filePath;
    private LocalDateTime uploadDate;

    private String pngPath;
    private String conversionStatus;
    private LocalDateTime convertedAt;

    private String patientName;
    private String patientId;
    private String studyDate; //촬영 날짜
    private String modality; // 장비 종류 (CT, MR, US등)

    private String studyUid;
    // 1:N 관계 (하나의 DICOM은 여러 변환 결과를 가질 수 있음)
    // 하나의 변환된 chest.dicom는 수많은 chest.png 가질 수 있다. -> 1:N
    // cascade = CascadeType.ALL을 하면 삭제할 때 외래키로 연결된 테이블의 데이터를 같이 삭제시켜 줌.
    @OneToMany(mappedBy = "dicom", cascade = CascadeType.ALL)
    private List<ConversionEntity> conversions = new ArrayList<>();
}
