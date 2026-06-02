package com.example.stalcraft_chatbot.ingestion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// This is the heart of your ingestion — NOT a throw-away utility
@Component
public class InfoBlockParser {

    // Extract a specific stat by its stable key
    // e.g. key = "stalker.artefact_properties.factor.bullet_dmg_factor"
    public Optional<Double> extractNumericStat(List<InfoBlock> blocks, String statKey) {
        return blocks.stream()
            .filter(b -> "list" .equals(b.getType()))
            .flatMap(b -> b.getElements().stream())
            .filter(e -> "numeric".equals(e.getType()))
            .filter(e -> e.getName() != null && statKey.equals(e.getName().getKey()))
            .map(Element::getValue)
            .findFirst();
    }

    // Extract ALL numeric stats as a flat map — useful for comparator tool
    // Map<statKey, value>  e.g. {"bullet_dmg_factor" -> 231.0, "weight" -> 30.0}
    public Map<String, Double> extractAllNumericStats(List<InfoBlock> blocks) {
        return blocks.stream()
            .filter(b -> "list".equals(b.getType()))
            .flatMap(b -> b.getElements().stream())
            .filter(e -> "numeric".equals(e.getType()))
            .filter(e -> e.getName() != null && e.getName().getKey() != null)
            .collect(Collectors.toMap(
                e -> e.getName().getKey(),
                Element::getValue,
                (a, b) -> a // keep first on collision — same key can appear in multiple blocks
            ));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoBlock {
        private String type;
        private InfoBlockTitle title;
        private List<Element> elements;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public InfoBlockTitle getTitle() {
            return title;
        }

        public void setTitle(InfoBlockTitle title) {
            this.title = title;
        }

        public List<Element> getElements() {
            return elements;
        }

        public void setElements(List<Element> elements) {
            this.elements = elements;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoBlockTitle {
        private String type;
        private String text;
        private String key;
        private Map<String, String> lines;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Map<String, String> getLines() {
            return lines;
        }

        public void setLines(Map<String, String> lines) {
            this.lines = lines;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Element {
        private String type;
        private ElementName key;
        private ElementName name;
        private Object value;
        private Formatted formatted;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public ElementName getKey() {
            return key;
        }

        public void setKey(ElementName key) {
            this.key = key;
        }

        public ElementName getName() {
            return name;
        }

        public void setName(ElementName name) {
            this.name = name;
        }

        public Double getValue() {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return null;
        }

        @JsonProperty("value")
        public void setValue(Object value) {
            this.value = value;
        }

        public Formatted getFormatted() {
            return formatted;
        }

        public void setFormatted(Formatted formatted) {
            this.formatted = formatted;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ElementName {
        private String type;
        private String key;
        private Map<String, String> lines;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Map<String, String> getLines() {
            return lines;
        }

        public void setLines(Map<String, String> lines) {
            this.lines = lines;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Formatted {
        private Map<String, String> value;
        private String nameColor;
        private String valueColor;

        public Map<String, String> getValue() {
            return value;
        }

        public void setValue(Map<String, String> value) {
            this.value = value;
        }

        public String getNameColor() {
            return nameColor;
        }

        public void setNameColor(String nameColor) {
            this.nameColor = nameColor;
        }

        public String getValueColor() {
            return valueColor;
        }

        public void setValueColor(String valueColor) {
            this.valueColor = valueColor;
        }
    }
}