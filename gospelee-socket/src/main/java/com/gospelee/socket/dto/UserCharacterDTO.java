package com.gospelee.socket.dto;

import com.gospelee.socket.entity.User;
import com.gospelee.socket.entity.UserCharacter;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserCharacterDTO {

  private long seq;
  private String nickName;
  private String race;

  private User user;

  @Builder
  public UserCharacterDTO(long seq, String nickName, String race, User user) {
    this.seq = seq;
    this.nickName = nickName;
    this.race = race;
    this.user = user;
  }

  public static UserCharacterDTO fromEntity(UserCharacter userCharacter) {
    return UserCharacterDTO.builder()
        .seq(userCharacter.getSeq())
        .nickName(userCharacter.getNickName())
        .race(userCharacter.getRace())
        .user(userCharacter.getUser())
        .build();
  }

  public UserCharacter toEntity() {
    return UserCharacter.builder()
        .seq(this.seq)
        .nickName(this.nickName)
        .race(this.race)
        .user(this.user)
        .build();
  }

}
