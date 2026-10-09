package com.gospelee.api.repository.jpa.account;

import com.gospelee.api.entity.AppleAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppleAuthTokenRepository extends JpaRepository<AppleAuthToken, Long> {

}
