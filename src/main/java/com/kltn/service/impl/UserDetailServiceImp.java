package com.kltn.service.impl;

import com.kltn.constant.Constant;
import com.kltn.dto.custom.CustomUserDetails;
import com.kltn.model.User;
import com.kltn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailServiceImp implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(username);
        if (!userOptional.isPresent())
            throw new UsernameNotFoundException(Constant.ErrorMessageAuthValidation.WRONG_USERNAME_OR_PASSWORD);
        User user = userOptional.get();
        return CustomUserDetails.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .userName(user.getEmail())
                .name(user.getFullName())
                .id(user.getId())
                .authorities(user.getAuthorities())
                .build();
    }
}
