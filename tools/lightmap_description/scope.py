import rasterio #위성이미지 데이터를 다루는 라이브러리리
from rasterio.windows import from_bounds #지정한 좌표 영역에 맞는 windows생성
from rasterio.enums import Resampling

# 파일 경로
input_tif = "D:/광공해지도/광공해지도_전처리/VNL_npp_2024_global_vcmslcfg_v2_c202502261200.average_masked.dat.tif"
output_tif = "D:/광공해지도/광공해지도_전처리/korea_lightpollution.tif"

# 대한민국 대략 경계
min_lon, min_lat = 124.5, 33.0
max_lon, max_lat = 131.5, 39.5

# 열기
with rasterio.open(input_tif) as src:
    # 범위에 맞는 윈도우 계산(픽셀 좌표 영역)
    window = from_bounds(min_lon, min_lat, max_lon, max_lat, src.transform)

    # 데이터 잘라서 읽기
    clipped_data = src.read(1, window=window) #밴드 1은 흑백데이터

    # 프로파일 수정
    clipped_transform = src.window_transform(window) #지도 상에서 어떤 위치에 있는지를 알려주는 좌표계 변환 정보보
    clipped_profile = src.profile.copy() #원본 이미지(메타데이터)를 복사해서 자른 영역에 맞게 정보 수정
    clipped_profile.update({
        "height": clipped_data.shape[0], #잘라낸 이미지의 세로 픽셀 수
        "width": clipped_data.shape[1],
        "transform": clipped_transform #잘라낸 영역이 지도에서 어디에 위치하는지
    })

    # 결과 저장
    with rasterio.open(output_tif, "w", **clipped_profile) as dst:
        dst.write(clipped_data, 1)

print(f"대한민국 영역 잘라내기 완료: {output_tif}")