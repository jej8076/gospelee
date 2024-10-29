package com.gospelee.socket.service;

import com.gospelee.socket.dto.UserDTO;

public interface UserService {

  UserDTO findUser(String email);

}
