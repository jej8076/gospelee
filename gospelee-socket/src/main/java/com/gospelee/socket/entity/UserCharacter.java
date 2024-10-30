package com.gospelee.socket.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCharacter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private long seq;

  private String nickName;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userSeq")
  @JsonBackReference
  private User user;

  @Builder
  public UserCharacter(long seq, String nickName) {
    this.seq = seq;
    this.nickName = nickName;
  }
}
