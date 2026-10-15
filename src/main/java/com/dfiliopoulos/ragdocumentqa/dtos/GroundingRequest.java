package com.dfiliopoulos.ragdocumentqa.dtos;

import jakarta.validation.constraints.NotBlank;

public record GroundingRequest(@NotBlank String prompt) {
}
