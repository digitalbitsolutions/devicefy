package com.devicefy.backend.config;

import com.devicefy.backend.domain.Rol;
import com.devicefy.backend.domain.Usuario;
import com.devicefy.backend.domain.enums.RolNombre;
import com.devicefy.backend.repository.RolRepository;
import com.devicefy.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-username}")
    private String adminUsername;

    @Value("${app.bootstrap.admin-password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByUsername(adminUsername)) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setUsername(adminUsername);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setNombreCompleto("Administrador");
        admin.setEmail("admin@devicefy.local");
        admin.setActivo(true);
        rolRepository.findByNombre(RolNombre.ADMIN)
                .ifPresent(rol -> admin.getRoles().add(rol));
        usuarioRepository.save(admin);
    }
}
