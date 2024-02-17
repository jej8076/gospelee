package com.gospelee.api.repository;

import com.gospelee.api.entity.AccountBibleWrite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountBibleWriteRepository extends JpaRepository<AccountBibleWrite, String> {
    Optional<List<AccountBibleWrite>> findAllByAccountUid(long accountUid);

}
