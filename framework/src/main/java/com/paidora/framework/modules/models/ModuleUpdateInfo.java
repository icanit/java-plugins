package com.paidora.framework.modules.models;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ModuleUpdateInfo {
    private String moduleName;
    private String localVersion;
    private String repoLastVersion;
    private List<String> repoVersions;
}
