package com.linked.service;

import com.linked.dto.InstagramStatusDto;

public interface InstagramService {

    String initiateOAuth(Long tatuadorId);

    void handleCallback(String code, String state);

    void syncPosts(Long tatuadorId);

    void syncAsync(Long tatuadorId);

    InstagramStatusDto getStatus(Long tatuadorId);

    void disconnect(Long tatuadorId);
}
