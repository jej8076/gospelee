package com.gospelee.socket.service;

import com.gospelee.socket.dto.UserDTO;
import com.gospelee.socket.entity.User;
import com.gospelee.socket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public UserDTO findUser(String email) {
    User user = userRepository.findByEmail(email);
    return UserDTO.fromEntity(user);
  }
}


