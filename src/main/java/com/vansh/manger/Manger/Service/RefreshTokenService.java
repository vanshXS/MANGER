package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.Entity.RefreshToken;
import com.vansh.manger.Manger.Entity.User;
import com.vansh.manger.Manger.Repository.RefreshTokenRepository;
import com.vansh.manger.Manger.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepo userRepo;

   public void createRefreshToken(Long userId, String token, Instant expiryDate) {
       User user = userRepo.findById(userId)
               .orElseThrow(() -> new RuntimeException("User not founded"));

       Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByUserId(userId);

       RefreshToken refreshToken;

       if(existingTokenOpt.isPresent()) {
           refreshToken = existingTokenOpt.get();
       } else {
           refreshToken = new RefreshToken();
           refreshToken.setUser(user);
       }
       refreshToken.setToken(token);
       refreshToken.setExpiryDate(expiryDate);

       refreshTokenRepository.save(refreshToken);
   }



    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if(token.getExpiryDate().isBefore(Instant.now())) {
            logger.info("comes");
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please login again");
        }

        return token;
    }
}
