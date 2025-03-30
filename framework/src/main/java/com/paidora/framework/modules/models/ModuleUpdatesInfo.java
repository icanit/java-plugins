package com.paidora.framework.modules.models;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ModuleUpdatesInfo {
    private List<ModuleUpdateInfo> modules;
}
