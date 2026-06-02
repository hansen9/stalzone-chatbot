package com.example.stalcraft_chatbot.ingestion;

// This is the heart of your ingestion — NOT a throw-away utility
@Component
public class InfoBlockParser {

    // Extract a specific stat by its stable key
    // e.g. key = "stalker.artefact_properties.factor.bullet_dmg_factor"
    public Optional<Double> extractNumericStat(List<InfoBlock> blocks, String statKey) {
        return blocks.stream()
            .filter(b -> b.getType().equals("list"))
            .flatMap(b -> b.getElements().stream())
            .filter(e -> e.getType().equals("numeric"))
            .filter(e -> statKey.equals(e.getName().getKey()))
            .map(Element::getValue)
            .findFirst();
    }

    // Extract ALL numeric stats as a flat map — useful for comparator tool
    // Map<statKey, value>  e.g. {"bullet_dmg_factor" -> 231.0, "weight" -> 30.0}
    public Map<String, Double> extractAllNumericStats(List<InfoBlock> blocks) {
        return blocks.stream()
            .filter(b -> b.getType().equals("list"))
            .flatMap(b -> b.getElements().stream())
            .filter(e -> e.getType().equals("numeric"))
            .filter(e -> e.getName().getKey() != null) // some elements use "text" type names
            .collect(Collectors.toMap(
                e -> e.getName().getKey(),
                Element::getValue,
                (a, b) -> a // keep first on collision — same key can appear in multiple blocks
            ));
    }
}