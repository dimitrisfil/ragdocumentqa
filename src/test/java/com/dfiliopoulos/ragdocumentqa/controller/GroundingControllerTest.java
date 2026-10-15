package com.dfiliopoulos.ragdocumentqa.controller;

import com.dfiliopoulos.ragdocumentqa.dtos.GroundingRequest;
import com.dfiliopoulos.ragdocumentqa.dtos.GroundingResponse;
import com.dfiliopoulos.ragdocumentqa.service.GroundingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroundingControllerTest {

    private GroundingService groundingService;
    private GroundingController controller;

    @BeforeEach
    void setUp() {
        groundingService = mock(GroundingService.class);
        controller = new GroundingController(groundingService);
    }

    @Test
    void delegatesQuestionAndReturnsAnswer() {
        var request = new GroundingRequest("What is in the document?");
        var expected = new GroundingResponse("The answer.");
        when(groundingService.retrieveAnswer(request)).thenReturn(expected);

        var response = controller.getGrounding(request);

        assertEquals(expected, response);
        verify(groundingService).retrieveAnswer(request);
    }
}
