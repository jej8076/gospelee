package com.gospelee.socket.repository;

import com.gospelee.socket.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

  Optional<User> findByEmail(String email);
}
