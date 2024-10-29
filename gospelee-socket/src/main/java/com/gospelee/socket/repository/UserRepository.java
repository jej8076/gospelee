package com.gospelee.socket.repository;

import com.gospelee.socket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

  User findByEmail(String email);
}
