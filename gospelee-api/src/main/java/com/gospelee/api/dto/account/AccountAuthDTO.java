package com.gospelee.api.dto.account;

import com.gospelee.api.enums.RoleType;
import com.gospelee.api.enums.Yn;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Builder
@Getter
public class AccountAuthDTO implements UserDetails {

  private Long uid;

  private String name;

  private Long ecclesiaUid;

  private String rrn;

  private String phone;

  private String email;

  private RoleType role;

  private String idToken;

  private String accessToken;

  private String refreshToken;

  private String pushToken;

  private String ecclesiaStatus;

  private Yn leaveYn;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(role.name()));
    return grantedAuthorities;
  }

  @Override
  public String getPassword() {
    return "";
  }

  @Override
  public String getUsername() {
    return "";
  }

  @Override
  public boolean isAccountNonExpired() {
    return false;
  }

  @Override
  public boolean isAccountNonLocked() {
    return false;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }
}
