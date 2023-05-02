package com.example.InfBezTim10.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordDTO {

    @Length(min = 6)
    private String newPassword;
    @Length(min = 6)
    private String newPasswordConfirm;
    @NotEmpty
    private String code;
}
