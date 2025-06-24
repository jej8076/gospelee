package com.gospelee.socket.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private long seq;

  @Column
  private String email;

  @Column
  private String phone;

  @Column
  private LocalDateTime insertTime;

  @Column
  private LocalDateTime updateTime;

  @OneToOne(mappedBy = "user")
  @JsonManagedReference
  private UserCharacter userCharacter;

  @Builder
  public User(long seq, String email, String phone, LocalDateTime insertTime,
      LocalDateTime updateTime, UserCharacter userCharacter) {
    this.seq = seq;
    this.email = email;
    this.phone = phone;
    this.insertTime = insertTime;
    this.updateTime = updateTime;
    this.userCharacter = userCharacter;
  }
}
