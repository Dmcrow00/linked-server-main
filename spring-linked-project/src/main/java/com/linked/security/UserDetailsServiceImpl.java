package com.linked.security;

import com.linked.repository.SweUsrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga UserDetails desde swe_usrapl.
 * Soporta login por username o por email:
 *   - Si el identificador contiene "@" → busca por email
 *   - En caso contrario → busca por username
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SweUsrRepository sweUsrRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier != null && identifier.contains("@")) {
            return sweUsrRepository.findByEmailForAuth(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Usuario no encontrado o inactivo: " + identifier));
        }
        return sweUsrRepository.findByUsernameForAuth(identifier)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado o inactivo: " + identifier));
    }
}
