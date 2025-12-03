package com.example.cloudlabs.service.impl;

import com.example.cloudlabs.dto.RecognizeCarLabelsResponse;
import com.example.cloudlabs.service.RecognizeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecognizeServiceImpl implements RecognizeService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${recognize.token}")
    private String token;

    @Override
    public RecognizeCarLabelsResponse recognizeCarLabels(MultipartFile multipartFile) throws IOException {
        String url = UriComponentsBuilder
                .fromUriString("https://smarty.mail.ru/api/v1/objects/detect")
                .queryParam("oauth_token", token)
                .queryParam("oauth_provider", "mcs")
                .toUriString();

        Map<String, Object> meta = Map.of(
                "mode", new String[]{"car_number"},
                "images", new Object[]{ Map.of("name", "file") }
        );
        String metaJson = objectMapper.writeValueAsString(meta);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", getFilePart(multipartFile));
        body.add("meta", getMetaPart(metaJson));

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return objectMapper.readValue(response.getBody(), RecognizeCarLabelsResponse.class);
        } else {
            throw new RuntimeException("Ошибка API: " + response.getStatusCode() + " / " + response.getBody());
        }
    }

    @Override
    public String createImage(InputStream imageStream, RecognizeCarLabelsResponse response) throws IOException {
        byte[] png = annotateImage(imageStream, response);
        String b64 = Base64.getEncoder().encodeToString(png);
        return "data:image/png;base64," + b64;
    }

    private HttpEntity<ByteArrayResource> getFilePart(MultipartFile file) throws IOException {
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @NotNull
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
        return new HttpEntity<>(fileResource, fileHeaders);
    }

    private HttpEntity<String> getMetaPart(String metaJson) {
        HttpHeaders metaHeaders = new HttpHeaders();
        metaHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(metaJson, metaHeaders);
    }

    private byte[] annotateImage(InputStream imageStream, RecognizeCarLabelsResponse response) throws IOException {
        BufferedImage img = ImageIO.read(imageStream);
        if (img == null) throw new IOException("Не удалось прочитать изображение");

        BufferedImage annotated = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = annotated.createGraphics();
        try {
            g.drawImage(img, 0, 0, null);

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int baseFontPx = Math.max(12, img.getWidth() / 60);
            Font font = new Font("SansSerif", Font.BOLD, baseFontPx);
            g.setFont(font);

            float strokeWidth = Math.max(1.0f, img.getWidth() / 800f);
            g.setStroke(new BasicStroke(strokeWidth));

            if (response != null && response.getBody() != null && response.getBody().getCarNumberLabels() != null) {
                for (RecognizeCarLabelsResponse.CarNumberLabel label : response.getBody().getCarNumberLabels()) {
                    java.util.List<RecognizeCarLabelsResponse.Label> labels = label.getLabels();
                    if (labels == null) continue;

                    for (RecognizeCarLabelsResponse.Label lbl : labels) {
                        java.util.List<Integer> coord = lbl.getCoord();
                        if (coord == null || coord.size() < 4) continue;
                        int x1 = coord.get(0);
                        int y1 = coord.get(1);
                        int x2 = coord.get(2);
                        int y2 = coord.get(3);

                        int left = Math.min(x1, x2);
                        int right = Math.max(x1, x2);
                        int top = Math.min(y1, y2);
                        int bottom = Math.max(y1, y2);
                        int width = Math.max(1, right - left);
                        int height = Math.max(1, bottom - top);

                        Color edge = new Color(255, 0, 0);

                        g.setColor(edge);
                        g.setStroke(new BasicStroke(Math.max(1.0f, strokeWidth)));
                        g.drawRect(left, top, width, height);

                        String typeLabel = "";
                        double prob = Double.NaN;
                        if (lbl.getTypesProb() != null && !lbl.getTypesProb().isEmpty()) {
                            RecognizeCarLabelsResponse.TypeProb typeProb = lbl.getTypesProb().stream()
                                    .max(Comparator.comparingDouble(RecognizeCarLabelsResponse.TypeProb::getProb)).get();
                            typeLabel = typeProb.getType();
                            prob = typeProb.getProb();
                        }

                        String caption = getTextWithProb(lbl, typeLabel, prob);

                        FontRenderContext frc = g.getFontRenderContext();
                        Rectangle2D textBounds = g.getFont().getStringBounds(caption, frc);
                        int pad = Math.max(4, baseFontPx / 3);
                        int textWidth = (int) Math.ceil(textBounds.getWidth()) + pad * 2;
                        int textHeight = (int) Math.ceil(textBounds.getHeight()) + pad;

                        int textY = top - textHeight;
                        if (textY < 0) {
                            textY = top + 2;
                        }

                        Color bg = new Color(0, 0, 0, 140);
                        g.setColor(bg);
                        g.fillRect(left, textY, Math.min(textWidth, img.getWidth() - left), textHeight);

                        g.setColor(Color.WHITE);
                        int textBaseline = textY + textHeight - pad / 2;
                        g.drawString(caption, left + pad, textBaseline);
                    }
                }
            }
            g.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(annotated, "png", outputStream);
            return outputStream.toByteArray();
        } finally {
            g.dispose();
        }
    }

    private static String getTextWithProb(RecognizeCarLabelsResponse.Label lbl, String typeLabel, double prob) {
        String mainText = lbl.getEng() != null && !lbl.getEng().isBlank() ? lbl.getEng() : lbl.getRus();
        String textWithProb = mainText != null ? mainText : "";
        if (typeLabel != null && !typeLabel.isEmpty()) {
            textWithProb = textWithProb + " [" + typeLabel + " " + String.format("%.2f", prob) + "]";
        } else {
            textWithProb = textWithProb + " [" + String.format("%.2f", prob) + "]";
        }
        return textWithProb;
    }
}
