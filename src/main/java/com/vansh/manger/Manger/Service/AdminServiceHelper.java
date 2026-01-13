package com.vansh.manger.Manger.Service;


import com.vansh.manger.Manger.Entity.School;
import com.vansh.manger.Manger.Entity.User;
import com.vansh.manger.Manger.Repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceHelper {

    private final UserRepo userRepo;

    public School getCurrentAdminSchool() {

        Object principle = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
           String email = null;

        if(principle instanceof User) {

            email = ((User) principle).getEmail();
        }else if(principle instanceof String) {
            email = (String) principle;
        }else {
            throw new IllegalStateException("User is not authenticated properly..");
        }

        User admin = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Admin user not found."));

        if(admin.getSchool() == null) {
            throw new IllegalStateException("Admin is not associated with any school.");
        }

        return admin.getSchool();
    }



}
