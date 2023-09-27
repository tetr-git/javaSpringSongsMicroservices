    package com.songlists.controller;

    import com.songlists.clients.AuthorizationClient;
    import com.songlists.clients.SongsClient;
    import com.songlists.exceptions.ResourceNotFoundException;
    import com.songlists.models.Song;
    import com.songlists.models.SongList;
    import com.songlists.models.SongListSong;
    import com.songlists.repositories.SongListRepository;
    import com.songlists.repositories.SonglistSongsRepository;
    import jakarta.transaction.Transactional;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.net.URI;
    import java.util.*;

    @RestController
    @RequestMapping("/songms")
    public class SongListController {

        private final SongListRepository songListRepository;

        private final SonglistSongsRepository songListSongRepository;

        private final AuthorizationClient authorizationClient;

        private final SongsClient songsClient;

        public SongListController(SongListRepository songListRepository, SonglistSongsRepository songListSongRepository,
                                  AuthorizationClient authorizationClient, SongsClient songsClient) {
            this.songListRepository = songListRepository;
            this.songListSongRepository = songListSongRepository;
            this.authorizationClient = authorizationClient;
            this.songsClient = songsClient;
        }

        @GetMapping("/song_lists")
        public ResponseEntity<List<Map<String, Object>>> getSongLists(
                @RequestParam("userId") String userId,
                @RequestHeader("Authorization") String authorization) {
            if (!authorizationClient.isAuthorizationValid(authorization)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<SongList> songLists;
            // if user id is not the same as the currently logged-in user
            if (!userId.equals(authorizationClient.getUserId(authorization))) {
                // return public songlists from userid
                songLists = songListRepository.findPublicByUserId(userId);
            } else {
                // return all songlists from userid
                songLists = songListRepository.findByUserId(userId);
            }

            if (songLists.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<Map<String, Object>> songListResponses = new ArrayList<>();
            for (SongList songList : songLists) {
                Map<String, Object> songListResponse = buildSongListResponse(songList, authorization);
                songListResponses.add(songListResponse);
            }

            return ResponseEntity.ok(songListResponses);
        }

        @GetMapping("/song_lists/{id}")
        public ResponseEntity<Map<String, Object>> getSongListById(
                @PathVariable(value = "id") Long id,
                @RequestHeader("Authorization") String authorization) {
            if (!authorizationClient.isAuthorizationValid(authorization)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            SongList songList = songListRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("SongList", "id", id));
            Map<String, Object> songListResponse;
            // prvate listen werden nicht ausggeben
            if (!songList.getUserId().equals(authorizationClient.getUserId(authorization))) {
                if (!songList.isPrivate()) {
                    songListResponse = buildSongListResponse(songList, authorization);
                    return ResponseEntity.ok(songListResponse);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            songListResponse = buildSongListResponse(songList, authorization);
            return ResponseEntity.ok(songListResponse);
        }

        @PostMapping("/song_lists")
        @Transactional
        public ResponseEntity<?> createSongList(
                @RequestBody Map<String, Object> songListPayload,
                @RequestHeader("Authorization") String authorization) {
            if (!authorizationClient.isAuthorizationValid(authorization)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            try {
                boolean isPrivate = (boolean) songListPayload.get("isPrivate");
                String name = (String) songListPayload.get("name");
                List<Map<String, Object>> songsPayload = (List<Map<String, Object>>) songListPayload.get("songList");

                Set<Song> songs = new HashSet<>();
                for (Map<String, Object> songPayload : songsPayload) {
                    String title = (String) songPayload.get("title");
                    String artist = (String) songPayload.get("artist");
                    String label = (String) songPayload.get("label");
                    Integer released = (Integer) songPayload.get("released");

                    Song song = songsClient.getSongByDetails(title, artist, label, released, authorization);
                    if (song == null) {
                        throw new ResourceNotFoundException("Song", "title", title);
                    } else {
                        songs.add(song);
                    }
                }

                String userId = authorizationClient.getUserId(authorization);

                // Create and save the SongList first to generate songListId
                SongList songList = new SongList(isPrivate, name, userId);
                SongList createdSongList = songListRepository.save(songList);

                // Use the generated songListId to create and associate SongListSong instances
                Set<SongListSong> songListSongs = new HashSet<>();
                for (Song song : songs) {
                    SongListSong songListSong = new SongListSong();
                    songListSong.setSongListId(createdSongList.getId()); // Use the generated songListId
                    songListSong.setSongId(song.getUuid());
                    songListSongs.add(songListSong);
                }

                // Save the SongListSong instances
                songListSongRepository.saveAll(songListSongs);

                // Build the location URL with the newly created song list's ID
                String locationUrl = "/songLists/" + createdSongList.getId();

                return ResponseEntity.created(URI.create(locationUrl)).build();
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }

        @Transactional
        @DeleteMapping("/song_lists/{id}")
        public ResponseEntity<?> deleteSongList(
                @PathVariable(value = "id") Long id,
                @RequestHeader("Authorization") String authorization) {
            if (!authorizationClient.isAuthorizationValid(authorization)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Logger logger = LoggerFactory.getLogger(SongListController.class);

            SongList songList = songListRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("SongList", "id", id));

            logger.debug("songList: {}", songList);
            logger.debug(songList.getUserId().toString());
            logger.debug(authorizationClient.getUserId(authorization).toString());

            if (!songList.getUserId().toString().equals(authorizationClient.getUserId(authorization).toString())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            //delete entris in songlist_song table first
            songListRepository.deleteSongListSongBySongListId(songList.getId());
            songListRepository.deleteSongListById(songList.getId());
            //        songListRepository.deleteSongListById(songList.getId());
            return ResponseEntity.noContent().build();
        }


        @PutMapping("/song_lists/{id}")
        @Transactional
        public ResponseEntity<?> updateSongList(
                @PathVariable(value = "id") Long id,
                @RequestBody Map<String, Object> songListPayload,
                @RequestHeader("Authorization") String authorization) {
            if (!authorizationClient.isAuthorizationValid(authorization)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Logger logger = LoggerFactory.getLogger(SongListController.class);

            try {
                // Check if the song list with the given ID exists
                SongList existingSongList = songListRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("SongList", "id", id));

                // Check if the user is the owner of the song list
                if (!existingSongList.getUserId().equals(authorizationClient.getUserId(authorization))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

                logger.debug("Received updated songListPayload: {}", songListPayload);
                boolean isPrivate = (boolean) songListPayload.get("isPrivate");
                String name = (String) songListPayload.get("name");
                List<Map<String, Object>> songsPayload = (List<Map<String, Object>>) songListPayload.get("songList");

                logger.debug("Updated isPrivate: {}", isPrivate);
                logger.debug("Updated name: {}", name);
                logger.debug("Updated songsPayload: {}", songsPayload);

                songListRepository.deleteSongListSongBySongListId(existingSongList.getId());

                Set<Song> updatedSongs = new HashSet<>();
                for (Map<String, Object> songPayload : songsPayload) {
                    String title = (String) songPayload.get("title");
                    String artist = (String) songPayload.get("artist");
                    String label = (String) songPayload.get("label");
                    Integer released = (Integer) songPayload.get("released");

                    Song song = songsClient.getSongByDetails(title, artist, label, released, authorization);
                    if (song == null) {
                        throw new ResourceNotFoundException("Song", "title", title);
                    } else {
                        updatedSongs.add(song);
                    }
                }

                // Update the song list's properties
                existingSongList.setPrivate(isPrivate);
                existingSongList.setName(name);
                songListRepository.save(existingSongList);

                // Associate the updated songs with the song list
                Set<SongListSong> songListSongs = new HashSet<>();
                for (Song updatedSong : updatedSongs) {
                    SongListSong songListSong = new SongListSong();
                    songListSong.setSongListId(existingSongList.getId());
                    songListSong.setSongId(updatedSong.getUuid());
                    songListSongs.add(songListSong);
                }
                songListSongRepository.saveAll(songListSongs);

                return ResponseEntity.ok().build();
            } catch (Exception e) {
                logger.error("Error occurred while updating the song list", e);
                return ResponseEntity.badRequest().build();
            }
        }


        private Map<String, Object> buildSongListResponse(SongList songList, String authorization) {
            Map<String, Object> songListResponse = new LinkedHashMap<>();
            songListResponse.put("id", songList.getId());
            songListResponse.put("isPrivate", songList.isPrivate());
            songListResponse.put("ownerId", songList.getUserId());
            songListResponse.put("name", songList.getName());
            Set<UUID> songsUuids = songList.getSongsUuid();
            Set<Song> songs = new HashSet<>();
            for (UUID uuid : songsUuids) {
                Song song = songsClient.getSongByUuid(uuid.toString(), authorization);
                songs.add(song);
            }

            List<Map<String, Object>> songResponses = buildSongResponses(songs);
            songListResponse.put("songList", songResponses);

            return songListResponse;
        }


        private List<Map<String, Object>> buildSongResponses(Set<Song> songs) {
            List<Map<String, Object>> songResponses = new ArrayList<>();
            for (Song song : songs) {
                Map<String, Object> songResponse = new LinkedHashMap<>();
                songResponse.put("id", song.getId());
                songResponse.put("title", song.getTitle());
                songResponse.put("artist", song.getArtist());
                songResponse.put("label", song.getLabel());
                songResponse.put("released", song.getReleased());
                songResponse.put("uuid", song.getUuid());
                songResponses.add(songResponse);
            }
            return songResponses;
        }

        private Song getSongByDetails(String titel, String artist, String label, Integer released, String authorization) {
            return songsClient.getSongByDetails(titel, artist, label, released,authorization);
        }

        private Song getSongByUuid(String uuid, String authorization) {
            return songsClient.getSongByUuid(uuid, authorization);
        }
    }
