
### 데이터 다운받기(전지구 광공해 데이터-GeoTIFF형식)
- https://eogdata.mines.edu/products/vnl/
- Annual VNL V2(Go to Download V2.2)
- VNL_npp_2024_global_vcmslcfg_v2_c202502261200.average_masked.dat.tif.gz

### 데이터 전처리 과정 
- 다운받은 데이터를 gzip으로 압축 해제
- 대한민국 경계만큼 이미지 자름(latitude: 33.0 ~ 39.5, longitude: 124.5 ~ 131.5)->scope.py
- lightvalue.py로 위도/경도값 넣으면 광공해 지수 값 확인
- 시각화는 colormapping.py로 진행

### GeoTIFF데이터 형식
- 단순한 이미지가 아니라 지도 데이터임
- 이미지 픽셀의 크기뿐만 아니라 어느 위치에, 어떤 해상도로, 어떤 좌표계에서 찍혔는지가 필요함
- 잘라낸 데이터를 저장하는 것이 아니라, 정확한 위치 정보가 담긴 메타데이터도 함께 저장해야 함

- 항목 설명
픽셀 값	예: 광공해 밝기값, 식생지수, 적외선 등
좌표계 (CRS)	위도/경도인지, UTM인지 등 (예: EPSG:4326 = 위경도)
GeoTransform	픽셀 → 실제 위도/경도 위치로 변환하는 행렬
해상도 (픽셀 크기)	1픽셀이 몇 m 또는 몇 도에 해당하는지
경계 좌표 (Bounding box)	이미지가 지도상에서 어디를 덮는지 (min/max 위도·경도)


출처(Source): Earth Observation Group (EOG), Payne Institute for Public Policy, Colorado School of Mines  
 (https://eogdata.mines.edu/nighttime_light/annual/v22/2024/)
 데이터셋명: VIIRS Nighttime Lights Annual V2 (Version 2.2)
