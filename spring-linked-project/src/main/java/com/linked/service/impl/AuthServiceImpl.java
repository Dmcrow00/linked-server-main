package com.linked.service.impl;

import com.linked.dto.AuthDto;
import com.linked.exception.AppExceptions;
import com.linked.model.entity.SweUsrapl;
import com.linked.repository.SweUsrRepository;
import com.linked.security.JwtService;
import com.linked.security.RsaDecryptionService;
import com.linked.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final SweUsrRepository sweUsrRepository;
    private final JwtService jwtService;
    private final RsaDecryptionService rsaDecryptionService;
    private final RecaptchaServiceImpl recaptchaService;
    private final InstagramServiceImpl instagramService;

    @Override
    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        recaptchaService.verifyIfEnabled(request.recaptchaToken());

        String plainPassword = rsaDecryptionService.decrypt(request.password());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), plainPassword));

        SweUsrapl user = sweUsrRepository.findByUsername(request.identifier())
                .or(() -> sweUsrRepository.findByEmail(request.identifier()))
                .orElseThrow(() -> new AppExceptions.UnauthorizedException("Credenciales inválidas"));

        if (!"true".equals(user.getStatus())) {
            throw new AppExceptions.UnauthorizedException("Cuenta inactiva");
        }

        String token = jwtService.generateToken(user);

        // Async Instagram sync for tatuadores
        if (user.hasRole("Tatuador")) {
            instagramService.syncAsync(user.getId());
        }

        return new AuthDto.TokenResponse(token, user.getUsername(), user.getId());
    }

    @Override
    public AuthDto.CheckAvailabilityResponse checkAvailability(String field, String value) {
        boolean available = switch (field.toLowerCase()) {
            case "username" -> !sweUsrRepository.existsByUsername(value);
            case "email"    -> !sweUsrRepository.existsByEmail(value);
            default -> throw new AppExceptions.BadRequestException("Campo inválido: " + field);
        };
        return new AuthDto.CheckAvailabilityResponse(available);
    }
}
