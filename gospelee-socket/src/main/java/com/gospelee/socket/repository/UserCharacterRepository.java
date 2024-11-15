package com.gospelee.socket.repository;

import com.gospelee.socket.entity.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, String> {

}
