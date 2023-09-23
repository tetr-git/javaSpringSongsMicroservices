package su.spotifyuploader.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import su.spotifyuploader.models.SpotifyUser;

import java.util.List;

@Repository
public interface SpotifyRepository extends CrudRepository<SpotifyUser, Long> {

    //query to look for userId
    @Query(value = "SELECT * FROM spotify_users WHERE user_id =?1", nativeQuery = true)
    List<SpotifyUser> findSpotifyUserByUserId(String userId);
}
