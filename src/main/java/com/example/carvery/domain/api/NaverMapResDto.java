package com.example.carvery.domain.api;

import com.example.carvery.domain.carwash.CarWashUtils;
import com.example.carvery.domain.carwash.DTO.CarWashDto;
import com.example.carvery.domain.carwash.DTO.WashType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NaverMapResDto {
    private String id;
    private String name;
    private String tel;
    private List<String> category;
    private String address;
    private String thumUrl;
    private List<String> thumUrls;
    private Double x;
    private Double y;
    private BusinessStatus businessStatus;
    private String bizhourInfo;
    private String carWash;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BusinessStatus {
        private String requestTime;
        private Status status;
        private String businessHours;
        private String breakTime;
        private String lastOrder;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Status {
        private int code;
        private String text;
        private boolean emphasis;
        private String description;
        private String detailInfo;
    }

    // 세차 타입 정보를 문자열 리스트로 반환
    public List<String> getWashTypeInfos() {
        return WashType.fromCategoryList(category).stream()
                .map(WashType::getKorean)
                .collect(Collectors.toList());
    }

    public static List<NaverMapResDto> parseFirstPlace(String jsonData) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        if(Objects.equals(jsonData, "{}") || Objects.equals(jsonData, "[]")) {
            return new ArrayList<>();
        }

        // jsonData가 이미 list 형태의 JSON이므로 직접 파싱
        JsonNode placeList = objectMapper.readTree(jsonData);
        List<NaverMapResDto> result = new ArrayList<>();

        // placeList가 배열인지 확인
        if (!placeList.isArray()) {
            return new ArrayList<>();
        }

        for (JsonNode node : placeList) {
            NaverMapResDto dto = NaverMapResDto.builder()
                    .id(node.path("id").asText())
                    .name(node.path("name").asText())
                    .tel(node.path("tel").asText())
                    .category(objectMapper.convertValue(node.get("category"), new TypeReference<List<String>>() {}))
                    .address(node.path("address").asText())
                    .thumUrl(node.path("thumUrl").asText())
                    .thumUrls(objectMapper.convertValue(node.path("thumUrls"), new TypeReference<List<String>>() {}))
                    .x(node.path("x").asDouble())
                    .y(node.path("y").asDouble())
                    .businessStatus(objectMapper.convertValue(node.path("businessStatus"), BusinessStatus.class))
                    .bizhourInfo(node.path("bizhourInfo").asText())
                    .carWash(node.path("carWash").asText())
                    .build();
            System.out.println(dto.getCategory());
            result.add(dto);
        }

        return result;
    }

    public static CarWashDto NaverResConvertToDto(NaverMapResDto naverMapResDto, double userLat, double userLng) {
        CarWashUtils carWashUtils = new CarWashUtils();

        return CarWashDto.builder()
                .id(naverMapResDto.getId())
                .title(naverMapResDto.getName())
                .address(naverMapResDto.getAddress())
                .dist(carWashUtils.calculateDistance(userLat, userLng, naverMapResDto.getY(), naverMapResDto.getX()))
                .call(naverMapResDto.getTel() != null ? naverMapResDto.getTel() : "정보없음")
                .productImage(naverMapResDto.getThumUrl())
                .infos(naverMapResDto.getWashTypeInfos())
                .businessHours(naverMapResDto.getBusinessStatus() != null &&
                        naverMapResDto.getBusinessStatus().getStatus() != null ?
                        naverMapResDto.getBusinessStatus().getStatus().getDetailInfo() : "정보없음")
                .offDutyDay(naverMapResDto.getBizhourInfo())
                .lat(naverMapResDto.getY())
                .lon(naverMapResDto.getX())
                .build();
    }
}
