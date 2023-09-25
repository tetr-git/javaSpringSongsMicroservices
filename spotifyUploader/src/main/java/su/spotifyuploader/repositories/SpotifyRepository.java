package su.spotifyuploader.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import su.spotifyuploader.models.SpotifyUser;

import java.util.List;

@Repository
public interface SpotifyRepository extends CrudRepository<SpotifyUser, Long> {

    //query to look for userId
    @Query(value = "SELECT * FROM spotify_users WHERE user_id =?1", nativeQuery = true)
    List<SpotifyUser> findSpotifyUserByUserId(String userId);
    // not working
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO spotifyuser (userid, accesstoken) VALUES (:userId, :accessToken) " +
            "ON CONFLICT (userid) DO UPDATE SET accesstoken = :accessToken", nativeQuery = true)
    void saveOrUpdateSpotifyUser(@Param("userId") String userId, @Param("accessToken") String accessToken);

}

