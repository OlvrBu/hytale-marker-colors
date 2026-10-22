package io.github.obut.markercolors;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MarkerColorJsonTest {

    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID C = UUID.fromString("00000000-0000-0000-0000-00000000000c");

    @Test
    void roundTrips() {
        Map<UUID, Map<UUID, Integer>> colors = Map.of(A, Map.of(B, 0x00F482, C, 0x1880FF), B, Map.of(A, 0xFFFFFF));
        MarkerColorJson.Result result = MarkerColorJson.parse(MarkerColorJson.write(colors));
        assertEquals(colors, result.colors());
        assertEquals(0, result.skipped());
    }

    @Test
    void writesHexSortedAndSkipsEmptyViewers() {
        String json = MarkerColorJson.write(Map.of(A, Map.of(B, 0x00F482), C, Map.of()));
        assertEquals("""
                {
                  "00000000-0000-0000-0000-00000000000a": {
                    "00000000-0000-0000-0000-00000000000b": "#00F482"
                  }
                }
                """, json);
    }

    @Test
    void skipsMalformedEntries() {
        String json = """
                {
                  "not-a-uuid": { "00000000-0000-0000-0000-00000000000b": "#00F482" },
                  "00000000-0000-0000-0000-00000000000a": {
                    "00000000-0000-0000-0000-00000000000b": "#00F482",
                    "00000000-0000-0000-0000-00000000000c": "green",
                    "bad": "#FFFFFF"
                  },
                  "00000000-0000-0000-0000-00000000000c": 5
                }
                """;
        MarkerColorJson.Result result = MarkerColorJson.parse(json);
        assertEquals(Map.of(A, Map.of(B, 0x00F482)), result.colors());
        assertEquals(4, result.skipped());
    }

    @Test
    void rejectsNonObjectFiles() {
        assertThrows(IllegalArgumentException.class, () -> MarkerColorJson.parse("[1, 2]"));
        assertThrows(IllegalArgumentException.class, () -> MarkerColorJson.parse("{ unclosed"));
    }
}
