import pydicom
from pydicom.pixel_data_handlers.util import apply_voi_lut # VOI LUT 적용 (영상을 선명하게)
import numpy as np
from PIL import Image
import sys

from pydicom.pixels import convert_color_space


def convert_dicom_to_png(dicom_path, png_path):
    try:
        ds = pydicom.dcmread(dicom_path)
        ds.decode() # 압축 해제
        pixel_array = ds.pixel_array

        # 멀티 프레임 처리 (XA는 여러 장의 프레임이 있을 수 있음)
        # 만약 3차원 배열 이라면 첫 번째 프레임만 추출
        if len(pixel_array.shape) == 3:
            pixel_array = pixel_array[0]
        elif len(pixel_array.shape) == 4:
            pixel_array = pixel_array[0]

        # [US 대응] YBR 색상 체계를 RGB로 변환 (안 하면 색이 이상하게 나옴)
        if "PhotometricInterpretation" in ds and "YBR" in ds.PhotometricInterpretation:
            pixel_array = convert_color_space(pixel_array, ds.PhotometricInterpretation,"RGB")

        # 픽셀 값 정규화 (0-255 범위로 변환)
        pixel_array = pixel_array.astype(float)
        # 값 너무 크거나 작을 때를 대비한 클리핑 및 정규화
        p_min, p_max = np.min(pixel_array), np.max(pixel_array)
        if p_max > p_min:
            rescaled = ((pixel_array - p_min) / (p_max - p_min)) * 255
        else:
            rescaled = pixel_array

        final_image = np.uint8(rescaled)
        img = Image.fromarray(final_image)

        # 파일 저장
        img.save(png_path)

        # 자바에게 성공을 알리는 정확한 메시지 출력
        print(f"SUCCESS|{png_path}")
        sys.exit(0)  # '정상 종료'를 명확히 알림 (Code 0)

    except Exception as e:
        print(f"ERROR|{str(e)}")
        sys.exit(1)  # '에러 발생'을 알림 (Code 1)

if __name__ == "__main__":
    if len(sys.argv) > 2:
        convert_dicom_to_png(sys.argv[1], sys.argv[2])