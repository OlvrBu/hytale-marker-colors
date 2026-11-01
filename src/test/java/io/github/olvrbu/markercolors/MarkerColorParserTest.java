package io.github.olvrbu.markercolors;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkerColorParserTest {

    @Test
    void parsesNamesAndHex() {
        assertEquals(OptionalInt.of(0x00F482), MarkerColorParser.parse("green"));
        assertEquals(OptionalInt.of(0x00F482), MarkerColorParser.parse(" Green "));
        assertEquals(OptionalInt.of(0x00FF00), MarkerColorParser.parse("#00ff00"));
        assertEquals(OptionalInt.of(0xABCDEF), MarkerColorParser.parse("ABCDEF"));
    }

    @Test
    void rejectsInvalidInput() {
        assertEquals(OptionalInt.empty(), MarkerColorParser.parse("grean"));
        assertEquals(OptionalInt.empty(), MarkerColorParser.parse("#fff"));
        assertEquals(OptionalInt.empty(), MarkerColorParser.parse("#00ff0g"));
        assertEquals(OptionalInt.empty(), MarkerColorParser.parse(""));
    }

    @Test
    void formatsAsHex() {
        assertEquals("#00FF00", MarkerColorParser.format(0x00FF00));
        assertEquals("#0A0B0C", MarkerColorParser.format(0x0A0B0C));
    }
}
