package com.linked.service;

import com.dmc00.core.swe.dto.SweUsrDto;

/** Mails transaccionales de linked. */
public interface EmailService {

    /**
     * Manda el enlace de "olvidé mi contraseña", que apunta a {@code /reset-password} del cliente
     * web. Es asíncrono: si el servidor de correo falla queda en el log y no corta la operación.
     */
    void enviarRecuperacionDeContrasena(SweUsrDto.PasswordReset recuperacion);
}
