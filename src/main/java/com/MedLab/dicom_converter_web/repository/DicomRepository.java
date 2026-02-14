package com.MedLab.dicom_converter_web.repository;

import com.MedLab.dicom_converter_web.domain.DicomEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DicomRepository extends JpaRepository<DicomEntity, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO dicom_info (file_path, upload_date, patient_name, patient_id, study_date, modality,study_uid)" +
            "VALUES(:path, NOW(), :name, :pId, :sDate, :mod, :sUid)", nativeQuery = true)
     // 중요! VALUES 안의 값은 반드시 위 컬럼 순서와 1:1로 대응해야 함.
    // 중요! @Param의 이름을 쿼리 내부의 :path와 일치시켜야 함.
    void insertDicomWithMeta(@Param("path") String path,
                           @Param("name") String name,
                           @Param("pId") String pId,
                           @Param("sDate") String sDate,
                           @Param("mod") String mod,
                           @Param("sUid") String sUid);
}

