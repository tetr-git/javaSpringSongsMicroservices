package com.songs.repo;

import de.htwb.ai.model.Song;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends CrudRepository<Song, Long> {

    @Query(value = "SELECT * FROM songtable WHERE id = ?1", nativeQuery = true)
    List<Song> selectSongById(int id);

}
