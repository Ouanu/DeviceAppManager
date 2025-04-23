package org.ouanu.manager.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUploadRequest {
    private String deviceUuid;
    private List<ApplicationDTO> applications;
}