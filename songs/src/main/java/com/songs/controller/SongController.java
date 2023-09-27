package com.songs.controller;

import com.songs.repositories.SongRepository;
import com.songs.clients.AuthClient;
import com.songs.models.Song;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("songms")
public class SongController {
    private final SongRepository songRepo;
    private final AuthClient authClient;

    public SongController(SongRepository repo, AuthClient authClient) {
        this.songRepo = repo;
        this.authClient = authClient;
    }

    @GetMapping("/songs")
    public ResponseEntity<Iterable<Song>> getAllSongs(@RequestHeader("Authorization") String authorization) {
        if (!authClient.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().body(songRepo.findAll());
    }

    @PostMapping("/songs")
    public ResponseEntity<?> createSong(
            @RequestHeader("Authorization") String authorization,
            @Valid
            @RequestBody Song song) {
        if (!authClient.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID uuid = UUID.randomUUID();
        song.setUuid(uuid);

        Song createdSong;
        try {
            //check if song already exists
            Song existingSong = songRepo.findByDetails(song.getTitle(), song.getArtist(), song.getLabel(), song.getReleased());
            if (existingSong != null) {
                return ResponseEntity.status(409).build();
            }

            createdSong = songRepo.save(song);
        } catch (Exception e) {
            return ResponseEntity.status(406).build();
        }

        return ResponseEntity
                .created(URI.create("/songms/songs/" + createdSong.getId()))
                .build();
    }

    @GetMapping("/songs/{id}")
    public ResponseEntity<Song> getSongById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable(value = "id") Long id) {
        if (!authClient.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Song> optionalSong = songRepo.findById(id);
        if (optionalSong.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(optionalSong.get());
    }

    @PutMapping("/songs/{id}")
    public ResponseEntity<?> updateSong(
            @RequestHeader("Authorization") String authorization,
            @PathVariable(value = "id") Long id,
            @RequestBody Song songToPut) {
        if (!authClient.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Song> optionalSong = songRepo.findById(id);
        if (optionalSong.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Song existingSong = optionalSong.get();
        existingSong.setArtist(songToPut.getArtist());
        existingSong.setTitle(songToPut.getTitle());
        existingSong.setLabel(songToPut.getLabel());
        existingSong.setReleased(songToPut.getReleased());

        UUID uuid = UUID.randomUUID();
        existingSong.setUuid(uuid);

        songRepo.save(existingSong);

        return ResponseEntity.noContent().build();
    }

    //get song by uuid
        @GetMapping("/songs/uuid/{uuid}")
        public ResponseEntity<Song> getSongByUuid(
                @RequestHeader("Authorization") String authorization,
                @PathVariable(value = "uuid") String uuid) {
            if (!authClient.isAuthorizationValid(authorization)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Song song = songRepo.findByUuid(uuid);
            if (song == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(song);
        }

    @GetMapping("/songs/find")
    public ResponseEntity<Song> findSong(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam("label") String label,
            @RequestParam("releaseYear") int releaseYear) {
        if (!authClient.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (title.contains("%20")) {
            title = title.replace("%20", " ");
        }
        if (artist.contains("%20")) {
            artist = artist.replace("%20", " ");
        }
        if (label.contains("%20")) {
            label = label.replace("%20", " ");
        }

        Song song = songRepo.findByDetails(title, artist, label, releaseYear);
        if (song == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(song);
    }

}
