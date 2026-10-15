package com.dfiliopoulos.ragdocumentqa.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RagUtilsTest {

    @Test
    void returnsExtensionAfterLastDot() {
        assertEquals("pdf", RagUtils.getFileExtension("folder/report.final.pdf"));
    }

    @Test
    void returnsEmptyStringWhenFilenameHasNoExtension() {
        assertEquals("", RagUtils.getFileExtension("README"));
    }

    @Test
    void returnsEmptyStringWhenFilenameEndsWithDot() {
        assertEquals("", RagUtils.getFileExtension("report."));
    }
}
