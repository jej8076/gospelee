package com.gospelee.socket.dto;

import com.gospelee.socket.entity.User;
import com.gospelee.socket.entity.UserCharacter;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.ObjectUtils;

@Getter
public class UserDTO {

  private Long seq;
  private String email;
  private String phone;
  private UserCharacter userCharacter;

  @Builder
  public UserDTO(long seq, String email, String phone, UserCharacter userCharacter) {
    this.seq = seq;
    this.email = email;
    this.phone = phone;
    this.userCharacter = userCharacter;
  }

  public static UserDTO fromEntity(User user) {
    return UserDTO.builder()
        .seq(ObjectUtils.isEmpty(user) ? null : user.getSeq())
        .email(user.getEmail())
        .phone(user.getPhone())
        .userCharacter(user.getUserCharacter())
        .build();
  }

  public User toEntity() {
    return User.builder()
        .seq(this.seq)
        .email(this.email)
        .phone(this.phone)
        .userCharacter(this.userCharacter)
        .build();
  }

}
