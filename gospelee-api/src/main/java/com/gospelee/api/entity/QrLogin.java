package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QrLogin extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private long uid;

  @Column
  private String email;

  @Column
  private String code;

  @Column(length = 1000)
  private String idToken;

  @Column(length = 1000)
  private String accessToken;

  @Column(length = 1000)
  private String refreshToken;

  @Column
  private LocalDateTime expireTime;

  @Builder
  public QrLogin(long uid, String email, String code, String idToken, String accessToken,
      String refreshToken, LocalDateTime expireTime) {
    this.uid = uid;
    this.email = email;
    this.code = code;
    this.idToken = idToken;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expireTime = expireTime;
  }
}
