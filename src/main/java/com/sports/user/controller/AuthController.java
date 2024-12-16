package com.sports.user.controller;

import com.sports.Security.auth.PrincipalUserDetails;
import com.sports.Security.dto.AuthResponse;
import com.sports.Security.dto.LoginRequest;
import com.sports.Security.jwt.JwtTokenProvider;
import com.sports.user.entito.User;
import com.sports.user.entito.UserDTO;
import com.sports.user.refresh.UserRefreshTokenRepository;
import com.sports.user.repository.ValidationGroups;
import com.sports.user.service.UserContextService;
import com.sports.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Transactional
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserContextService userContextService;

    // 일반 회원가입
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> signUp(@Validated(ValidationGroups.Create.class) @ModelAttribute UserDTO userDTO,
                                                      BindingResult bindingResult,
                                                      @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        // 검증 오류가 있으면 에러 메시지를 반환
        if (bindingResult.hasErrors()) {
            // 필드 오류를 처리
            String errorMessages = bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + " : " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            Map<String, String> errorResponse = Map.of("message", "회원가입 실패: " + errorMessages);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String resultMessage = userService.register(userDTO, file);
        Map<String, String> response = Map.of("message", resultMessage);

        return "회원가입이 성공적으로 완료되었습니다.".equals(resultMessage)
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    // 일반 로그인
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@ModelAttribute LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = userService.authenticateUser(
                    loginRequest, authenticationManager, jwtTokenProvider, userRefreshTokenRepository);
            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("인증 실패: Id 또는 비밀번호 입력 오류"));
        }
    }

    // 소셜 로그인 (JWT토큰, 소셜에서 전달되는 유저정보 전송)
    @GetMapping("/oauth2/token")
    public ResponseEntity<Map<String, Object>> getToken(HttpServletRequest request) {

        HttpSession session = request.getSession(false); // 세션 가져오기
        if (session == null) {
            throw new RuntimeException("세션이 없습니다.");
        }

        SecurityContext securityContext = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (securityContext == null || securityContext.getAuthentication() == null) {
            throw new RuntimeException("SecurityContext에 인증 정보가 없습니다.");
        }

        User currentUser = userContextService.getCurrentUser(); // 현재 사용자 정보 가져오기
        Map<String, Object> response = userService.generateTokenResponseWithUser(currentUser.getId(), jwtTokenProvider, userRefreshTokenRepository);
        return ResponseEntity.ok(response);
    }

    // 아이디 중복체크
    @GetMapping("/oauth2/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean isDuplicate = userService.isUsernameDuplicate(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }

    // 통합 로그아웃
    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<String> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("인증된 사용자가 없어서 로그아웃 할 사람이 없습니다.");
        }

        Long userId = ((PrincipalUserDetails) authentication.getPrincipal()).getId();
        userContextService.logout(userId, userRefreshTokenRepository);

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }


    // 리프레시 토큰으로 새로운 액세스 토큰 생성
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: 리프레시토큰 없음");
        }

        try {
            String newAccessToken = userContextService.refreshAccessToken(refreshToken.trim(), jwtTokenProvider, userRefreshTokenRepository);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}