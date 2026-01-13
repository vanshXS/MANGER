package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.Entity.User;
import com.vansh.manger.Manger.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private record ProfileReponse(Long id, String fullName, String email, String role) {}
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User user) {

        if(user == null) {
            return ResponseEntity.status(401).build();
        }

        ProfileReponse profile = new ProfileReponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles().toString()
        );

        return ResponseEntity.ok(profile);


    }

}
