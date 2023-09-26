package su.spotifyuploader.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import su.spotifyuploader.models.SpotifyUser;

import java.util.List;

@Repository
public interface SpotifyRepository extends CrudRepository<SpotifyUser, Long> {

    @Query(value = "SELECT * FROM spotifyuser WHERE userid =?1", nativeQuery = true)
    List<SpotifyUser> findSpotifyUserByUserId(String userId);
}

