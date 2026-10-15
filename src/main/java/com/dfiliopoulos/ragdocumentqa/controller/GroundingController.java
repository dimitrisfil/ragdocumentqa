package com.dfiliopoulos.ragdocumentqa.controller;

import com.dfiliopoulos.ragdocumentqa.dtos.GroundingRequest;
import com.dfiliopoulos.ragdocumentqa.dtos.GroundingResponse;
import com.dfiliopoulos.ragdocumentqa.service.GroundingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class GroundingController {

    private final GroundingService groundingService;

    public GroundingController(GroundingService groundingService) {
        this.groundingService = groundingService;
    }

    @PostMapping("/api/v1/grounding")
    public GroundingResponse getGrounding(@RequestBody GroundingRequest groundingRequest) {
        return groundingService.retrieveAnswer(groundingRequest);
    }


}
