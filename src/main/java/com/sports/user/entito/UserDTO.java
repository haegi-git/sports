package com.sports.user.entito;

import com.sports.user.repository.ValidationGroups;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;

    @NotBlank(groups = ValidationGroups.Create.class)
    @Size(min = 6, max = 20, message = "{Id.Size}")
    @Pattern(regexp = "^[a-zA-Z]+[a-zA-Z0-9]*$", message = "{Id.Pattern}")
    private String username;

    @NotBlank(groups = ValidationGroups.Create.class)
    @Size(min = 8, max = 20, message = "{Pw.Size}")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]*$", message = "{Pw.Pattern}")
    private String password;

    @NotBlank
    @Size(min = 2, max = 20, message = "{Name.Size}")
    private String nickname;
    // 주문시에는 이름으로 사용

    @NotBlank
    @Size(min = 10, max = 13, message = "{Ph.Size}")
    @Column(unique = true)
    private String phone;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;
    private String role;

    private String address;
    private String imgURL;

    // 마이페이지 유저 정보 보내기용 생성자
    public UserDTO(Long id, String username, String nickname, String phone, String email, String address, String imgURL, String role) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.imgURL = imgURL;
        this.role = role;
    }

}
