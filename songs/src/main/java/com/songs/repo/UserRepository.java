package de.htwb.ai.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import de.htwb.ai.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    @Query(value = "SELECT * FROM usertable WHERE userId = ?1 AND password = ?2", nativeQuery = true)
    List<User> authenticateUser(String userId, String password);

    @Query(value = "SELECT * FROM usertable WHERE userId = ?1", nativeQuery = true)
    User findUserByStringId(String userId);
}
