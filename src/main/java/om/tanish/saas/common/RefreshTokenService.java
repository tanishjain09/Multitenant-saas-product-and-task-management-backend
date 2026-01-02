package om.tanish.saas.common;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000L;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository){
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createRefreshToken(UUID userId){
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_VALIDITY));
        refreshToken.setCreatedAt(Instant.now());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }
    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(token);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token expired. Please login again");
        }
        return token;
    }
    @Transactional
    public void deleteByToken(String token){
        refreshTokenRepository.deleteByToken(token);
    }
    @Transactional
    public void deleteByUserId(UUID userId){
        refreshTokenRepository.deleteByUserId(userId);
    }
}