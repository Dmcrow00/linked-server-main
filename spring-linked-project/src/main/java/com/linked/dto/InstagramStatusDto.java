package com.linked.dto;

public record InstagramStatusDto(
        boolean connected,
        String instagramUserId,
        String expiresAt
) {}
