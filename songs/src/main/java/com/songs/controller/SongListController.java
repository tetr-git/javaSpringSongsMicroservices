    package de.htwb.ai.controller;

    import de.htwb.ai.model.Song;
    import de.htwb.ai.model.SongList;
    import de.htwb.ai.model.User;
    import de.htwb.ai.repo.SongListRepository;
    import de.htwb.ai.exception.ResourceNotFoundException;
    import de.htwb.ai.repo.SongRepository;
    import de.htwb.ai.repo.UserRepository;
    import jakarta.transaction.Transactional;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import java.net.URI;
    import java.util.*;

    @RestController
    @RequestMapping("songsWS-max_samuel/rest")
    public class SongListController {

        private final SongListRepository songListRepository;

        private final AuthController authController;

        private final SongRepository songRepository;

        private final UserRepository userRepository;

        public SongListController(SongListRepository songListRepository, AuthController authController, SongRepository songRepository, UserRepository userRepository) {
            this.songListRepository = songListRepository;
            this.authController = authController;
            this.songRepository = songRepository;
            this.userRepository = userRepository;
        }

        @GetMapping("/songLists")
        public ResponseEntity<List<Map<String, Object>>> getSongLists(
                @RequestParam("userId") String userId,
                @RequestHeader("Authorization") String authorization) {
            if (!authController.isAuthorizationValid(authorization)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<SongList> songLists;
            // if user id is not the same as the currently logged in user
            if (!userId.equals(authController.getCurrentUserId())) {
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
                Map<String, Object> songListResponse = buildSongListResponse(songList);
                songListResponses.add(songListResponse);
            }

            return ResponseEntity.ok(songListResponses);
        }

        @GetMapping("/songLists/{id}")
        public ResponseEntity<Map<String, Object>> getSongListById(
                @PathVariable(value = "id") Long id,
                @RequestHeader("Authorization") String authorization) {
            if (!authController.isAuthorizationValid(authorization)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            SongList songList = songListRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("SongList", "id", id));
            Map<String, Object> songListResponse;
            // prvate listen werden nicht ausggeben
            if (!songList.getUserId().equals(authController.getCurrentUserId())) {
                if (!songList.isPrivate()) {
                    songListResponse = buildSongListResponse(songList);
                    return ResponseEntity.ok(songListResponse);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            songListResponse = buildSongListResponse(songList);
            return ResponseEntity.ok(songListResponse);
        }

        @PostMapping("/songLists")
        @Transactional
        public ResponseEntity<?> createSongList(
                @RequestBody Map<String, Object> songListPayload,
                @RequestHeader("Authorization") String authorization) {
            if (!authController.isAuthorizationValid(authorization)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String ownerId = authController.getCurrentUserId();

            Logger logger = LoggerFactory.getLogger(SongListController.class);

            try {
                logger.debug("Received songListPayload: {}", songListPayload);
                boolean isPrivate = (boolean) songListPayload.get("isPrivate");
                String name = (String) songListPayload.get("name");
                List<Map<String, Object>> songsPayload = (List<Map<String, Object>>) songListPayload.get("songList");

                logger.debug("isPrivate: {}", isPrivate);
                logger.debug("name: {}", name);
                logger.debug("songsPayload: {}", songsPayload);

                Set<Song> songs = new HashSet<>();
                for (Map<String, Object> songPayload : songsPayload) {
                    Integer songId = (Integer) songPayload.get("id");
                    Song song = songRepository.findById(songId.longValue())
                            .orElseThrow(() -> new ResourceNotFoundException("Song", "id", songId));
                    songs.add(song);
                }
                //get user from user id
                User currentUser = userRepository.findUserByStringId(ownerId);
                SongList songList = new SongList(isPrivate, name, currentUser, songs);
                SongList createdSongList = songListRepository.save(songList);

                // Build the location URL with the newly created song list's ID
                String locationUrl = "/songLists/" + createdSongList.getId();

                return ResponseEntity.created(URI.create(locationUrl)).build();
            } catch (Exception e) {
                logger.error("Error occurred while creating the song list", e);
                return ResponseEntity.badRequest().build();
            }
        }
        @Transactional
        @DeleteMapping("/songLists/{id}")
        public ResponseEntity<?> deleteSongList(
                @PathVariable(value = "id") Long id,
                @RequestHeader("Authorization") String authorization) {
            if (!authController.isAuthorizationValid(authorization)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Logger logger = LoggerFactory.getLogger(SongListController.class);

            SongList songList = songListRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("SongList", "id", id));

            logger.debug("songList: {}", songList);
            logger.debug(songList.getUserId().getUserId());
            logger.debug(authController.getCurrentUserId());

            if (!songList.getUserId().getUserId().equals(authController.getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            //delete entris in songlist_song table first
            songListRepository.deleteSongListSongBySongListId(songList.getId());
            songListRepository.deleteSongListById(songList.getId());
            //        songListRepository.deleteSongListById(songList.getId());
            return ResponseEntity.noContent().build();
        }

        private Map<String, Object> buildSongListResponse(SongList songList) {
            Map<String, Object> songListResponse = new LinkedHashMap<>();
            songListResponse.put("id", songList.getId());
            songListResponse.put("isPrivate", songList.isPrivate());
            songListResponse.put("ownerId", songList.getUserId().getUserId());
            songListResponse.put("name", songList.getName());
            Set<Song> songs = songList.getSongs();

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
                songResponses.add(songResponse);
            }
            return songResponses;
        }
    }
