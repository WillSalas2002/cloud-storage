package com.will.cloud.storage.service.impl;

import com.will.cloud.storage.dto.request.AuthRequest;
import com.will.cloud.storage.dto.response.AuthResponse;
import com.will.cloud.storage.mapper.AuthMapper;
import com.will.cloud.storage.model.User;
import com.will.cloud.storage.repository.UserRepository;
import com.will.cloud.storage.service.AuthService;
import com.will.cloud.storage.service.MinioUtils;
import com.will.cloud.storage.util.AppConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMapper authMapper;
    private final MinioUtils minioUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse signUp(AuthRequest request) {
        User user = authMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);

        createMinioFolderForUser(user);
        log.info("User [{}] has successfully signed up", user.getUsername());
        return authMapper.toAuthResponse(savedUser);
    }

    private void createMinioFolderForUser(User user) {
        String folderName = String.format(AppConstants.PERSONAL_FOLDER_NAME_TEMPLATE, user.getId());
        log.info("Creating personal folder [{}] for user [{}]", folderName, user.getUsername());
        minioUtils.createDir(AppConstants.BUCKET_NAME, folderName);
        log.info(
                "Successfully created personal folder [{}] for user [{}]",
                folderName,
                user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse signIn(AuthRequest request, HttpServletRequest httpRequest) {
        Authentication authenticationToken =
                new UsernamePasswordAuthenticationToken(request.username(), request.password());
        Authentication auth = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        log.info("Creating session for user: {}", request.username());
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        log.info(
                "User {} successfully logged in, session: {}",
                request.username(),
                session.getAttribute("SESSION"));
        return new AuthResponse(auth.getName());
    }
}
