package com.songs;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends CrudRepository<Song, Long> {

    @Query(value = "SELECT * FROM songtable WHERE id = ?1", nativeQuery = true)
    List<Song> selectSongById(int id);

    //get song by uuid
    @Query(value = "SELECT * FROM songtable WHERE uuid = ?1", nativeQuery = true)
    Song findByUuid(String uuid);

    @Query(value = "SELECT * FROM songtable WHERE title = ?1 AND artist = ?2 AND label = ?3 AND released = ?4", nativeQuery = true)
    Song findByDetails(String title, String artist, String label, int released);


}
