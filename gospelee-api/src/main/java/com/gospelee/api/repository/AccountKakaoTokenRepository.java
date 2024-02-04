package com.gospelee.api.repository;

import com.gospelee.api.entity.AccountKakaoToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountKakaoTokenRepository extends JpaRepository<AccountKakaoToken, String> {
	Optional<AccountKakaoToken> findByAccessToken(String token);
}
