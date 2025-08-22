package com.gospelee.api.utils;

import com.gospelee.api.dto.account.AccountAuthDTO;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticatedUserUtils {

  /**
   * 인증된 사용자 정보를 반환하며, 없을 경우 예외를 던집니다.
   *
   * @return UserDetailDTO 인증된 사용자 정보
   * @throws AuthenticationCredentialsNotFoundException 인증된 사용자 정보가 없을 때 예외 발생
   */
  public static AccountAuthDTO getAuthenticatedUserOrElseThrow() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AuthenticationCredentialsNotFoundException("인증된 계정이 존재하지 않음");
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof AccountAuthDTO)) {
      throw new AuthenticationCredentialsNotFoundException("인증된 principal이 AccountAuthDTO 타입이 아님");
    }

    return (AccountAuthDTO) principal;
  }

  /**
   * 인증된 사용자 정보를 반환하며, 없을 경우 null을 return 합니다.
   *
   * @return UserDetailDTO or null
   */
  public static AccountAuthDTO getAuthenticatedUserOrNull() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof AccountAuthDTO)) {
      return null;
    }

    return (AccountAuthDTO) principal;
  }
}
