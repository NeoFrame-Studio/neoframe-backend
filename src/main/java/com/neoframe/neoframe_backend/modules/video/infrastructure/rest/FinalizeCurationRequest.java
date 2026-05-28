package com.neoframe.neoframe_backend.modules.video.infrastructure.rest;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class FinalizeCurationRequest {
    private List<String> urlsEscolhidas;

}