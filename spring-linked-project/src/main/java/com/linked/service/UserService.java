package com.linked.service;

import com.linked.dto.AuthDto;
import com.linked.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    List<UserDto.Response> findAll();

    List<UserDto.Response> findAllTatuadores();

    UserDto.Response findById(Long id);

    AuthDto.TokenResponse register(UserDto.CreateRequest request);

    AuthDto.TokenResponse registerTatuador(UserDto.RegisterTatuadorRequest request);

    UserDto.Response update(Long id, UserDto.UpdateRequest request);

    UserDto.Response updateProfilePhoto(Long id, MultipartFile file);

    void updateRoles(Long id, UserDto.UpdateRolesRequest request);

    void assignEspecialidades(Long tatuadorId, List<Long> especialidadIds);

    void removeEspecialidad(Long tatuadorId, Long especialidadId);

    void forgotPassword(UserDto.ForgotPasswordRequest request);

    void resetPassword(UserDto.ResetPasswordRequest request);

    void delete(Long id);
}
