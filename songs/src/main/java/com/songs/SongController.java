package com.songs.controller;

import de.htwb.ai.exception.ResourceNotFoundException;
import de.htwb.ai.model.Song;
import de.htwb.ai.repo.SongRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

//http://localhost:8080/api/users
@RestController
@RequestMapping("songsWS-max_samuel/rest")
public class SongController {
    private final SongRepository songRepo;
    private final AuthController authController;

    public SongController (SongRepository repo, AuthController authController) {
        this.songRepo = repo;
        this.authController = authController;
    }

    //@GetMapping("/users")
    @RequestMapping(value = "/songs", method = RequestMethod.GET)

    public ResponseEntity<Iterable<Song>> getAllSongs(@RequestHeader("Authorization") String authorization) {
        if(!authController.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();        }
        return ResponseEntity.ok().body(songRepo.findAll());
    }

    /*
    @PostMapping("/songs")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        //hier fehlt etwas
        //statuscode 202
        //userid aus user object lesenund
        //location-header setzen
        //return userRepo.save(user);
        // URI :/rest/users + user.getId()
        return ResponseEntity.created(null).build();
        //wie delete
    }


     */

    @PostMapping("/songs")
    public ResponseEntity<?> createSong(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Song song) {
        if(!authController.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        songRepo.findAll();
        Song createdSong = songRepo.save(song);
        return ResponseEntity
                .created(URI.create("/rest/songs/" + createdSong.getId()))
                .build();
    }



    @GetMapping("/songs/{id}")
    public ResponseEntity<Song> getSongById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable(value = "id") Long id) {
        if (!authController.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Song song = songRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return ResponseEntity.ok(song);
    }

    @PutMapping("/songs/{id}")
    public ResponseEntity<?> updateSong(
            @RequestHeader("Authorization") String authorization,
            @PathVariable(value = "id") Long id,
            @RequestBody Song songToPut) {
        if (!authController.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Song existingSong = songRepo.findById(id)
                .orElse(null);
        if (existingSong == null) {
            return ResponseEntity.notFound().build();
        }

        existingSong.setArtist(songToPut.getArtist());
        existingSong.setTitle(songToPut.getTitle());
        existingSong.setLabel(songToPut.getLabel());
        existingSong.setReleased(songToPut.getReleased());

        songRepo.save(existingSong);

        return ResponseEntity.noContent().build();
    }
    /*
    @DeleteMapping("/songs/{id}")Samu_l
    public ResponseEntity<?> deleteSong(
            @RequestHeader("Authorization") String authorization,
            @PathVariable(value = "id") Long id) {
        if (!authController.isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Song song = songRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        songRepo.delete(song);
        return ResponseEntity.noContent().build();
    }
     */
}
