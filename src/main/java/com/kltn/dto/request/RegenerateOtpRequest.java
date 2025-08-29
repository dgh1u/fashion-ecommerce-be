package com.kltn.dto.request;

import com.kltn.constant.Constant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegenerateOtpRequest {
    @Email(regexp = ".+[@].+[\\.].+",message = Constant.ErrMessageUserValidation.EMAIL_VALIDATE)
    @NotBlank(message = Constant.ErrMessageUserValidation.EMAIL_NOT_BLANK)
    private String email;
}
