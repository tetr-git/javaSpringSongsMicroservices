package com.songlists.repositories;

import com.songlists.models.SongListSong;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface SonglistSongsRepository extends CrudRepository<SongListSong, Long> {

    @Query(value = "SELECT * FROM songlist_song WHERE songListId = ?1", nativeQuery = true)
    List<SongListSong> selectSongListSongBySongListId(int songListId);
}
