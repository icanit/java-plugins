package com.paidora.framework.modules.repository;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DownloadedJar {
    private String fileName;
    private byte[] jarBytes;
}
