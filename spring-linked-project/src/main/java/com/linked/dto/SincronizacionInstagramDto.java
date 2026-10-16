package com.linked.dto;

/**
 * Resultado de importar las publicaciones de Instagram de un tatuador.
 *
 * @param nuevos     publicaciones importadas como trabajos
 * @param eliminados trabajos dados de baja porque su publicación ya no está en Instagram
 * @param omitidos   videos, publicaciones ya importadas o sin imágenes que se pudieran copiar
 */
public record SincronizacionInstagramDto(int nuevos, int eliminados, int omitidos) {
}
