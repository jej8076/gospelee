package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends EditInfomation implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private long uid;

  @Column
  private String name;

  @Column
  private String ecclesiaUid;

  @Column
  private String rrn;

  @Column
  private String phone;

  @Column
  private String email;

  @Column
  private RoleType role;

  @Column
  private String id_token;

  @Builder
  public Account(long uid, String name, String ecclesiaUid, String rrn, String phone, String email,
      RoleType role, String id_token) {
    this.uid = uid;
    this.name = name;
    this.ecclesiaUid = ecclesiaUid;
    this.rrn = rrn;
    this.phone = phone;
    this.email = email;
    this.role = role;
    this.id_token = id_token;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(role.name()));
    return grantedAuthorities;
  }

  @Override
  public String getPassword() {
    return id_token;
  }

  @Override
  public String getUsername() {
    return email;
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
    return true;
  }
}