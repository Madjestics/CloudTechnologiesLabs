package com.example.cloudlabs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Ответ от VK API на запрос распознавания номеров машин
 */
@Data
public class RecognizeCarLabelsResponse {
    private int status;
    private Body body;
    private boolean htmlencoded;
    @JsonProperty("last_modified")
    private long lastModified;

    @Data
    public static class Body {
        @JsonProperty("car_number_labels")
        private List<CarNumberLabel> carNumberLabels;
    }

    @Data
    public static class CarNumberLabel {
        private int status;
        private String name;
        private List<Label> labels;
    }

    @Data
    public static class Label {
        private String eng;
        private String rus;
        private double prob;
        private List<Integer> coord;
        @JsonProperty("types_prob")
        private List<TypeProb> typesProb;
    }

    @Data
    public static class TypeProb {
        private String type;
        private double prob;
    }
}
