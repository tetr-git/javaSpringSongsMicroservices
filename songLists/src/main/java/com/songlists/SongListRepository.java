package com.songlists;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface    SongListRepository extends CrudRepository<SongList, Long>{

    @Query(value = "SELECT * FROM songLists WHERE id = ?1", nativeQuery = true)
    List<SongList> selectSongListById(int id);

    @Query(value = "SELECT * FROM songLists WHERE userId = ?1", nativeQuery = true)
    List<SongList> findByUserId(String userId);

    @Query(value = "SELECT * FROM songLists WHERE userId = ?1 AND isPrivate = false", nativeQuery = true)
    List<SongList> findPublicByUserId(String userId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM songlists WHERE id = ?1", nativeQuery = true)
    void deleteSongListById(int id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM songlist_song WHERE songlistid = ?1", nativeQuery = true)
    void deleteSongListSongBySongListId(int id);
    }