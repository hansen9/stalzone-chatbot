package com.example.stalzone_chatbot.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

// Pure unit test — no Spring context, no DB, no network.
// InfoBlockParser only needs an ObjectMapper, which we construct directly.
// This test starts in milliseconds.
class InfoBlockParserTest {

    private InfoBlockParser parser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        parser = new InfoBlockParser(objectMapper);
    }

    // -------------------------------------------------------------------------
    // Happy path — real fixture from the GitHub data repo
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("parseInfoBlocks: parses 9A-91 fixture and returns non-empty block list")
    void parseInfoBlocks_realFixture_returnsBlocks() throws Exception {
        JsonNode infoBlocksNode = loadInfoBlocksFromFixture("0r2g1.json");

        List<InfoBlockParser.InfoBlock> blocks = parser.parseInfoBlocks(infoBlocksNode);

        // The fixture has 11 infoBlock entries (list, damage, text types mixed)
        // We only care that it parsed without throwing and returned something
        assertThat(blocks).isNotEmpty();
    }

    @Test
    @DisplayName("extractAllNumericStats: extracts damage stat correctly from 9A-91")
    void extractAllNumericStats_realFixture_extractsDamage() throws Exception {
        JsonNode infoBlocksNode = loadInfoBlocksFromFixture("0r2g1.json");
        List<InfoBlockParser.InfoBlock> blocks = parser.parseInfoBlocks(infoBlocksNode);

        Map<String, Double> stats = parser.extractAllNumericStats(blocks);

        // Assert on the damage stat using its stable translation key
        // Value from fixture: 33.9
        assertThat(stats).containsKey("core.tooltip.stat_name.damage_type.direct");
        assertThat(stats.get("core.tooltip.stat_name.damage_type.direct"))
                .isCloseTo(33.9, within(0.001));
    }

    @Test
    @DisplayName("extractAllNumericStats: extracts rate of fire from 9A-91")
    void extractAllNumericStats_realFixture_extractsRateOfFire() throws Exception {
        JsonNode infoBlocksNode = loadInfoBlocksFromFixture("0r2g1.json");
        List<InfoBlockParser.InfoBlock> blocks = parser.parseInfoBlocks(infoBlocksNode);

        Map<String, Double> stats = parser.extractAllNumericStats(blocks);

        // Value from fixture: 700.0
        assertThat(stats).containsKey("weapon.tooltip.weapon.info.rate_of_fire");
        assertThat(stats.get("weapon.tooltip.weapon.info.rate_of_fire"))
                .isCloseTo(700.0, within(0.001));
    }

    @Test
    @DisplayName("extractAllNumericStats: extracts weight from 9A-91")
    void extractAllNumericStats_realFixture_extractsWeight() throws Exception {
        JsonNode infoBlocksNode = loadInfoBlocksFromFixture("0r2g1.json");
        List<InfoBlockParser.InfoBlock> blocks = parser.parseInfoBlocks(infoBlocksNode);

        Map<String, Double> stats = parser.extractAllNumericStats(blocks);

        // Value from fixture: 2.29
        assertThat(stats).containsKey("core.tooltip.info.weight");
        assertThat(stats.get("core.tooltip.info.weight"))
                .isCloseTo(2.29, within(0.001));
    }

    @Test
    @DisplayName("extractNumericStat: finds a specific stat by key")
    void extractNumericStat_existingKey_returnsValue() throws Exception {
        JsonNode infoBlocksNode = loadInfoBlocksFromFixture("0r2g1.json");
        List<InfoBlockParser.InfoBlock> blocks = parser.parseInfoBlocks(infoBlocksNode);

        Optional<Double> result = parser.extractNumericStat(
                blocks,
                "weapon.tooltip.weapon.info.rate_of_fire"
        );

        assertThat(result).isPresent();
        assertThat(result.get()).isCloseTo(700.0, within(0.001));
    }

    @Test
    @DisplayName("extractNumericStat: returns empty for a key that doesn't exist")
    void extractNumericStat_missingKey_returnsEmpty() throws Exception {
        JsonNode infoBlocksNode = loadInfoBlocksFromFixture("0r2g1.json");
        List<InfoBlockParser.InfoBlock> blocks = parser.parseInfoBlocks(infoBlocksNode);

        Optional<Double> result = parser.extractNumericStat(blocks, "this.key.does.not.exist");

        // Optional.empty() — not an exception, not null, just absent
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("extractAllNumericStats: key-value and text elements are ignored, only numeric extracted")
    void extractAllNumericStats_ignoresNonNumericElements() throws Exception {
        JsonNode infoBlocksNode = loadInfoBlocksFromFixture("0r2g1.json");
        List<InfoBlockParser.InfoBlock> blocks = parser.parseInfoBlocks(infoBlocksNode);

        Map<String, Double> stats = parser.extractAllNumericStats(blocks);

        // key-value elements (rank, category, ammo type) should NOT appear in stats map
        assertThat(stats).doesNotContainKey("core.tooltip.info.rank");
        assertThat(stats).doesNotContainKey("weapon.tooltip.weapon.info.ammo_type");
    }

    // -------------------------------------------------------------------------
    // Edge cases — your observation: some list blocks have empty elements arrays
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("parseInfoBlocks: blocks with empty elements arrays don't crash")
    void parseInfoBlocks_blocksWithEmptyElements_handledGracefully() throws Exception {
        // The 9A-91 fixture has several list blocks with "elements": []
        // This test verifies they're parsed without NPE or exception
        JsonNode infoBlocksNode = loadInfoBlocksFromFixture("0r2g1.json");

        List<InfoBlockParser.InfoBlock> blocks = parser.parseInfoBlocks(infoBlocksNode);

        // If we get here without exception, empty-element blocks are handled.
        // Also verify extracting stats from them produces an empty map, not null.
        Map<String, Double> stats = parser.extractAllNumericStats(blocks);
        assertThat(stats).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Null / bad input — parser must never throw on garbage input
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("parseInfoBlocks: null input returns empty list, not exception")
    void parseInfoBlocks_nullInput_returnsEmptyList() {
        List<InfoBlockParser.InfoBlock> blocks = parser.parseInfoBlocks(null);

        assertThat(blocks).isEmpty();
    }

    @Test
    @DisplayName("parseInfoBlocks: non-array JsonNode returns empty list")
    void parseInfoBlocks_nonArrayNode_returnsEmptyList() throws Exception {
        // Pass a JSON object instead of an array — should not throw
        JsonNode objectNode = objectMapper.readTree("{\"type\": \"not-an-array\"}");

        List<InfoBlockParser.InfoBlock> blocks = parser.parseInfoBlocks(objectNode);

        assertThat(blocks).isEmpty();
    }

    @Test
    @DisplayName("parseInfoBlocks: empty array returns empty list")
    void parseInfoBlocks_emptyArray_returnsEmptyList() throws Exception {
        JsonNode emptyArray = objectMapper.readTree("[]");

        List<InfoBlockParser.InfoBlock> blocks = parser.parseInfoBlocks(emptyArray);

        assertThat(blocks).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Helper — loads the infoBlocks node from a fixture file in test/resources
    // -------------------------------------------------------------------------

    private JsonNode loadInfoBlocksFromFixture(String filename) throws Exception {
        // getResourceAsStream looks in src/test/resources on the classpath
        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
        assertThat(stream)
                .as("Fixture file '%s' not found in test/resources", filename)
                .isNotNull();

        JsonNode root = objectMapper.readTree(stream);
        JsonNode infoBlocks = root.path("infoBlocks");

        assertThat(infoBlocks.isMissingNode())
                .as("'infoBlocks' field missing from fixture '%s'", filename)
                .isFalse();

        return infoBlocks;
    }
}
