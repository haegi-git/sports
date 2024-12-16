package com.sports.user.service;

import com.sports.Item.S3Service;
import com.sports.Security.dto.AuthResponse;
import com.sports.Security.jwt.JwtTokenProvider;
import com.sports.Security.dto.LoginRequest;
import com.sports.board.Board;
import com.sports.board.BoardResponseDTO;
import com.sports.user.refresh.UserRefreshToken;
import com.sports.user.refresh.UserRefreshTokenRepository;
import com.sports.user.repository.UserRepository;
import com.sports.user.entito.User;
import com.sports.user.entito.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final UserContextService userContextService;

    // 일반 회원가입 서비스
    public String register(UserDTO userDTO, MultipartFile file) throws IOException {

        // 1. 아이디 중복 체크
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            return "중복된 아이디가 존재합니다.";
        }

        // 2. 이메일 중복 체크
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return "중복된 이메일이 존재합니다.";
        }

        // 3. 전화번호 중복 체크 (필요한 경우)
        if (userRepository.existsByPhone(userDTO.getPhone())) {
            return "중복된 전화번호가 존재합니다.";
        }

        String phone = userDTO.getPhone().replaceAll("[^0-9]", "");
        userDTO.setPhone(phone);

        String imgURL;
        if (file != null && !file.isEmpty()) {
            imgURL = s3Service.saveFile(file.getOriginalFilename(), file.getInputStream());
        } else {
            imgURL = s3Service.saveFile(null, null);
        }
        userDTO.setImgURL(imgURL);

        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        User user = User.builder()
                .username(userDTO.getUsername())
                .password(encodedPassword)
                .nickname(userDTO.getNickname())
                .phone(userDTO.getPhone())
                .email(userDTO.getEmail())
                .address(userDTO.getAddress())
                .role("ROLE_USER")
                .imgURL(imgURL)
                .build();

        userRepository.save(user);
        return "회원가입이 성공적으로 완료되었습니다.";
    }

    // 일반 로그인 서비스 -- username, nickname, role, JWT토큰 반환
    public AuthResponse authenticateUser(LoginRequest loginRequest, AuthenticationManager authenticationManager,
                                         JwtTokenProvider jwtTokenProvider, UserRefreshTokenRepository userRefreshTokenRepository) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        User user = userContextService.findByUsername(authentication.getName());
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getUsername(), role);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        UserRefreshToken userRefreshToken = userRefreshTokenRepository.findById(user.getId())
                .orElse(new UserRefreshToken(user, refreshToken));
        userRefreshToken.updateRefreshToken(refreshToken);
        userRefreshTokenRepository.save(userRefreshToken);

        return new AuthResponse(user.getId(), user.getUsername(), user.getNickname(), role, accessToken, refreshToken, user.getImgURL());
    }


    // 오어스 로그인 서비스 -- 일반 로그인과 같은 정보 반환
    public Map<String, Object> generateTokenResponseWithUser(Long userId, JwtTokenProvider jwtTokenProvider,
                                                             UserRefreshTokenRepository userRefreshTokenRepository) {
        // 영속성 컨텍스트에서 User 엔티티 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // 리프레시 토큰 저장 또는 업데이트
        UserRefreshToken userRefreshToken = userRefreshTokenRepository.findById(user.getId())
                .orElse(new UserRefreshToken(user, refreshToken));
        userRefreshToken.updateRefreshToken(refreshToken);
        userRefreshTokenRepository.save(userRefreshToken);

        // 응답 데이터 구성 (토큰/ 유저정보)
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("nickname", user.getNickname());
        userInfo.put("role", user.getRole());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("imgURL", user.getImgURL());
        userInfo.put("userId", user.getId());

        response.put("user", userInfo);

        return response;
    }

    // 마이페이지 유저정보 업데이트 서비스
    public UserDTO updateUser(UserDTO userDTO, MultipartFile file) throws IOException {
        // 현재 사용자의 정보를 가져옴
        User existingUser = userContextService.getCurrentUser();

        // userDTO의 값으로 기존 user 엔티티 업데이트
        existingUser.setNickname(userDTO.getNickname());
        existingUser.setPhone(userDTO.getPhone());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setAddress(userDTO.getAddress());

        // 이미지 파일 처리 로직
        String newImgURL = existingUser.getImgURL();

        // 업로드된 파일이 있으면 해당 파일을 저장
        if (file != null && !file.isEmpty()) {
            newImgURL = s3Service.saveFile(file.getOriginalFilename(), file.getInputStream());
        } else {
            // 파일이 없으면 기본 이미지를 설정
            newImgURL = s3Service.saveFile(null, null);
        }

        // 기존 이미지 URL과 비교해 업데이트
        if (!newImgURL.equals(existingUser.getImgURL())) {
            existingUser.setImgURL(newImgURL);
        }


        // 변경된 사용자 정보를 저장
        User updatedUser = userRepository.save(existingUser);

        // 변경된 엔티티를 DTO로 변환 후 반환
        UserDTO updatedUserDTO = new UserDTO();
        BeanUtils.copyProperties(updatedUser, updatedUserDTO);

        return updatedUserDTO;
    }

    // 아이디 중복 여부
    public boolean isUsernameDuplicate(String username) {
        return userRepository.existsByUsername(username);
    }

    // 유저정보 저장하고 유저객체 반환
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // 유저아이디로 닉네임가져오기
    public String getNicknameByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getNickname();
    }

    //검색기능(유저)
    public List<UserDTO> searchBoardByEmail(String keyword) {
        List<User> users = userRepository.searchByEmail(keyword);
        return users.stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getNickname(),
                        user.getPhone(),
                        user.getEmail(),
                        user.getAddress(),
                        user.getImgURL(),
                        user.getRole()
                ))
                .collect(Collectors.toList());
    }

}
