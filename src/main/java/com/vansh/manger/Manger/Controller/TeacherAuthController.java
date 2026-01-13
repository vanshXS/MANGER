package com.vansh.manger.Manger.Controller;


import com.vansh.manger.Manger.Config.JwtUtil;
import com.vansh.manger.Manger.DTO.*;
import com.vansh.manger.Manger.Entity.RefreshToken;
import com.vansh.manger.Manger.Entity.Roles;
import com.vansh.manger.Manger.Entity.User;
import com.vansh.manger.Manger.Repository.UserRepo;
import com.vansh.manger.Manger.Service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/api/auth/teacher")
@RequiredArgsConstructor
public class TeacherAuthController {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final JavaMailSender mailsender;


    @PostMapping("/login")
    public ResponseEntity<AdminResponseDTO> login(@RequestBody @Valid AdminLoginDTO teacherLogin) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(teacherLogin.getEmail(), teacherLogin.getPassword())
            );

            User user = userRepo.findByEmail(teacherLogin.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if(!user.getRoles().equals(Roles.TEACHER)) {
                return ResponseEntity.status(403).build();

            }
            String accessToken = jwtUtil.generateAccessToken(user, user.getRoles().name());
            String refreshToken = jwtUtil.generateRefreshToken(user, user.getRoles().name());

            refreshTokenService.createRefreshToken(
                    user.getId(),
                    refreshToken,
                    Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000) // 7 days
            );

            return ResponseEntity.ok(new AdminResponseDTO(accessToken, refreshToken, user.getRoles().name()));


        }catch (Exception e) {
            log.error("Authentication failed for teacher: " + teacherLogin.getEmail(), e);
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map( user -> {
                    String newAccesstoken = jwtUtil.generateAccessToken(user, user.getRoles().name());
                    return ResponseEntity.ok(new AdminResponseDTO(newAccesstoken, refreshToken, user.getRoles().name()));
                }).orElseThrow(() -> new RuntimeException("Invalid RefreshToken"));
    }

     //reset-password(while logged out)
    @PostMapping("/change-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request){

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User with this email not registered"));

        if(!user.getRoles().equals(Roles.TEACHER)) {
            return ResponseEntity.badRequest().body("This is not teacher account");
        }

        if(!request.getOldPassword().equals(user.getPassword())) {
            return ResponseEntity.badRequest().body("Password not matched");
        }
        user.setPassword(request.getNewPassword());
        userRepo.save(user);

        return new ResponseEntity<>("Password Reset!", HttpStatus.ACCEPTED);

    }

    //forget password(send otp)
    @PostMapping("/forget-password")
    public ResponseEntity<String> forgetPassword(@RequestBody @Valid ForgetPasswordRequest request) {

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

          if (!user.getRoles().equals(Roles.TEACHER)) {
            return ResponseEntity.status(403).body("Not a Teacher account");
        }

          String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);
          user.setResetOtp(otp);
          user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
          userRepo.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Teacher Password Reset OTP");
        message.setText("Your OTP is: " + otp + "\nIt will expire in 10 minutes.");
        mailsender.send(message);

        return ResponseEntity.ok("OTP sent to registered email!");

    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> rstPassword(@RequestBody @Valid ForgetResetPassword request) {

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        if(!user.getRoles().equals(Roles.TEACHER)) {
            return ResponseEntity.status(403).body("Not a Teacher account");
        }

        //Validate OTP
        if(user.getResetOtp() == null || !user.getResetOtp().equals(request.getOtp())) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
        if(user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("OTP expired");
        }
        //update password

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetOtp(null);
        user.setOtpExpiry(null);
        userRepo.save(user);

        return ResponseEntity.ok("Password reset successful");
    }




}

