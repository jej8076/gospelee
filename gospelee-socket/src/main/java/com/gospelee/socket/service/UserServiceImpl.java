package com.gospelee.socket.service;

import com.gospelee.socket.dto.UserCharacterDTO;
import com.gospelee.socket.dto.UserDTO;
import com.gospelee.socket.dto.jwt.JwtPayload;
import com.gospelee.socket.entity.User;
import com.gospelee.socket.entity.UserCharacter;
import com.gospelee.socket.repository.UserCharacterRepository;
import com.gospelee.socket.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  private final UserCharacterRepository userCharacterRepository;


  @Override
  public UserDTO findUser(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    return UserDTO.fromEntity(user);
  }

  @Override
  public UserDTO createUser(JwtPayload jwtPayload) {

    User user = User.builder()
        .email(jwtPayload.getEmail())
        .phone(randomTelNo())
        .insertTime(LocalDateTime.now())
        .updateTime(LocalDateTime.now())
        .build();

    User result = userRepository.save(user);
    return UserDTO.fromEntity(result);
  }

  @Override
  public UserCharacterDTO createCharacter(JwtPayload jwtPayload,
      UserCharacterDTO userCharacterDTO) {
    User user = userRepository.findByEmail(jwtPayload.getEmail())
        .orElseThrow(
            () -> new RuntimeException("User not found with email: " + jwtPayload.getEmail()));
    UserCharacter userCharacter = UserCharacter.builder()
        .user(user)
        .nickName(userCharacterDTO.getNickName())
        .race(userCharacterDTO.getRace())
        .build();

    return UserCharacterDTO.fromEntity(userCharacterRepository.save(userCharacter));
  }

  private String randomTelNo() {
    Random random = new Random();
    String resultNum = "010" + random.ints(8, 0, 10)
        .mapToObj(String::valueOf)
        .collect(Collectors.joining());

    return resultNum;
  }
}


