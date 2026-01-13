package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.Config.*;
import com.vansh.manger.Manger.DTO.*;
import com.vansh.manger.Manger.Entity.*;
import com.vansh.manger.Manger.Repository.*;
import com.vansh.manger.Manger.Service.*;
import com.vansh.manger.Manger.Repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminAuthController {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final JavaMailSender mailsender;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid AdminRegisterationDTO adminRegisterationDTO) {
        if (userRepo.findByEmail(adminRegisterationDTO.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Admin with this email already exists");
        }

        User admin = User.builder()
                .email(adminRegisterationDTO.getEmail())
                .roles(Roles.ADMIN)
                .password(passwordEncoder.encode(adminRegisterationDTO.getPassword()))
                .fullName(adminRegisterationDTO.getFullName())
                .build();

        userRepo.save(admin);
        return ResponseEntity.ok("Admin registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AdminLoginDTO adminLoginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            adminLoginDTO.getEmail(),
                            adminLoginDTO.getPassword()
                    )
            );

            User user = userRepo.findByEmail(adminLoginDTO.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getRoles().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This account is not an admin account");
            }

            String accessToken = jwtUtil.generateAccessToken(user, user.getRoles().name());
            String refreshToken = jwtUtil.generateRefreshToken(user, user.getRoles().name());

            refreshTokenService.createRefreshToken(
                    user.getId(),
                    refreshToken,
                    Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000) // 7 days
            );

            return ResponseEntity.ok(new AdminResponseDTO(accessToken, refreshToken, user.getRoles().name()));

        }catch (BadCredentialsException e) {
               return ResponseEntity
                       .status(HttpStatus.UNAUTHORIZED)
                       .body("Invalid email or password");
        }


        catch (Exception e) {
            log.error("Authentication failed for user: " + adminLoginDTO.getEmail(), e);
            return ResponseEntity.status(401).build();
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtUtil.generateAccessToken(user, user.getRoles().name());
                    return ResponseEntity.ok(new AdminResponseDTO(newAccessToken, refreshToken, user.getRoles().name()));
                })
                .orElseThrow(() -> new RuntimeException("Invalid RefreshToken"));
    }



    //reset-password(while logged out)
    @PostMapping("/change-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request){

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User with this email not registered"));

        if(!user.getRoles().equals(Roles.ADMIN)) {
            return ResponseEntity.badRequest().body("This is not admin account");
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
                .orElseThrow(() -> new RuntimeException("This email is not registered yet"));

        if (!user.getRoles().equals(Roles.ADMIN)) {
            return ResponseEntity.status(403).body("Not a Admin account");
        }

        String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);
        user.setResetOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepo.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Admin Password Reset OTP");
        message.setText("Your OTP is: " + otp + "\nIt will expire in 10 minutes.");
        mailsender.send(message);

        return ResponseEntity.ok("OTP sent to registered email!");

    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> rstPassword(@RequestBody @Valid ForgetResetPassword request) {

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if(!user.getRoles().equals(Roles.ADMIN)) {
            return ResponseEntity.status(403).body("Not a Admin account");
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
