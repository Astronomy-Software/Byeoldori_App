import rasterio
from rasterio.transform import rowcol

# 서울의 위도·경도
seoul_lat = 37.5665
seoul_lon = 126.9780

# 대한민국 광공해 GeoTIFF 열기
with rasterio.open("D:/광공해지도/광공해지도_전처리/korea_lightpollution.tif") as src:
    # 위도·경도 → 이미지 좌표 (row, col)
    row, col = rowcol(src.transform, seoul_lon, seoul_lat)

    # 해당 픽셀 값 읽기
    value = src.read(1)[row, col]

    print(f"📍 서울의 광공해 지수 값: {value}")