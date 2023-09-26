package com.auth.repositories;

import com.auth.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    @Query(value = "SELECT * FROM usertable WHERE userId = ?1 AND password = ?2", nativeQuery = true)
    List<User> authenticateUser(String userId, String password);

    @Query(value = "SELECT * FROM usertable WHERE userId = ?1", nativeQuery = true)
    User findUserByStringId(String userId);
}
