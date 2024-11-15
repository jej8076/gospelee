package com.gospelee.socket.service;

import com.gospelee.socket.dto.UserCharacterDTO;
import com.gospelee.socket.dto.UserDTO;
import com.gospelee.socket.dto.jwt.JwtPayload;

public interface UserService {

  UserDTO findUser(String email);

  UserDTO createUser(JwtPayload jwtPayload);

  UserCharacterDTO createCharacter(JwtPayload jwtPayload, UserCharacterDTO userCharacterDTO);

}
