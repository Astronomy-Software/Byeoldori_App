import rasterio
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import numpy as np

# GeoTIFF 파일 경로
cropped_tif = "D:/광공해지도/광공해지도_전처리/korea_lightpollution.tif"

# 파일 열기 및 데이터 읽기
with rasterio.open(cropped_tif) as src:
    data = src.read(1)

# 0 이하 값 마스킹
masked_data = np.ma.masked_less_equal(data, 0)

# gamma 보정 (낮은 값을 더 강조해서 보이게 하는 밝기 보정 기법)
gamma = 0.8  # 0.3~0.6 사이 권장
normalized = masked_data / masked_data.max()     # 0~1로 정규화(감마 보정은 0~1범위에서 적용되기 때문에)
gamma_corrected = np.power(normalized, gamma)    # 감마 보정(어두운 걸 더 밝게보이도록 보정)
gamma_scaled = gamma_corrected * masked_data.max()  # 원래 scale로 복원

# turbo 컬러맵 + 배경 마스킹 설정
cmap = plt.get_cmap('turbo').copy()
cmap.set_bad(color='black')  #마스킹된 값은 검정색으로 표시(0이하 값)

# 시각화
plt.figure(figsize=(10, 8))
plt.imshow(gamma_scaled, cmap=cmap, vmin=0, vmax=30)  # vmax 줄이면 밝기 강조
plt.colorbar(label="Light Pollution Value (Gamma corrected)")
plt.title("Light Pollution over South Korea (Brightened Low Values)")
plt.axis('off')
plt.show()
