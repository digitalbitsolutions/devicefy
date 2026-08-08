package com.devicefy.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AsignarDesplieguesRequest {

    private List<Long> despliegueIds;
}
