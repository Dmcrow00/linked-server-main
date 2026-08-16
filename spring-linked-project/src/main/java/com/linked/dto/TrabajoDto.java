package com.linked.dto;

import java.util.List;

public class TrabajoDto {

    public record CreateRequest(
            String descripcion
    ) {}

    public record Response(
            Long id,
            String descripcion,
            String instagramMediaId,
            List<String> fotos,
            UserDto.Response tatuador,
            int likesCount,
            boolean likedByMe,
            String creationTimestamp,
            String status
    ) {}
}
