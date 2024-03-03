package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User newUser = new User(name, mobile);
        users.add(newUser);
        userPlaylistMap.put(newUser,new ArrayList<>());
        return newUser;
    }

    public Artist createArtist(String name) {
        Artist newArtist = new Artist(name);
        artists.add(newArtist);
        artistAlbumMap.put(newArtist,new ArrayList<>());
        return newArtist;
    }

    public Album createAlbum(String title, String artistName) {
        for(Artist artist : artists){
            if (artist.getName().equals(artistName)) {
                break;
            }
            else {
                createArtist(artistName);
            }
        }
        Album newAlbum = new Album(title);
        albums.add(newAlbum);
        albumSongMap.put(newAlbum,new ArrayList<>());
        return  newAlbum;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = getAlbumByName(albumName);
        if (album == null) {
            throw new Exception("Album does not exist");
        }
        Song newSong = new Song(title, length);
        songs.add(newSong);
        albumSongMap.get(album).add(newSong);

        songLikeMap.put(newSong,new ArrayList<>());
        return newSong;
    }


    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = getUserByMobile(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }

        List<Song> songsWithGivenLength = getSongsByLength(length);
        Playlist newPlaylist = new Playlist(title);
        playlistSongMap.put(newPlaylist,new ArrayList<>());
        playlistListenerMap.put(newPlaylist,new ArrayList<>());
        addPlaylistToUser(user, newPlaylist);
        playlists.add(newPlaylist);
        playlistListenerMap.get(newPlaylist).add(user);   //current listener of the playlist
        creatorPlaylistMap.put(user,newPlaylist);         //creator of the playlist
        userPlaylistMap.get(user).add(newPlaylist);   //user and his list of playlist
        return newPlaylist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = getUserByMobile(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }

        List<Song> songsWithGivenTitles = getSongsByTitles(songTitles);
        Playlist newPlaylist = new Playlist(title);
        addPlaylistToUser(user, newPlaylist);
        addListenerToPlaylist(newPlaylist, user);
        playlists.add(newPlaylist);
        playlistSongMap.put(newPlaylist,new ArrayList<>());
        playlistListenerMap.put(newPlaylist,new ArrayList<>());
        return newPlaylist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = getUserByMobile(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }

        Playlist playlist = getPlaylistByTitle(playlistTitle);
        if (playlist == null) {
            throw new Exception("Playlist does not exist");
        }

        // Check if the user is the creator or already a listener
        if (playlistListenerMap.getOrDefault(playlist, Collections.emptyList()).contains(user)) {
            // Do nothing if the user is the creator or already a listener
            return playlist;
        }

        // Add user as a listener to the playlist and update accordingly
        addListenerToPlaylist(playlist, user);
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = getUserByMobile(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }

        Song song = getSongByTitle(songTitle);
        if (song == null) {
            throw new Exception("Song does not exist");
        }

        // Check if the user has already liked the song
        if (songLikeMap.getOrDefault(song, Collections.emptyList()).contains(user)) {
            // Do nothing if the user has already liked the song
            return song;
        }

        // Like the song by the user
        likeSongByUser(user, song);
        song.setLikes(song.getLikes()+1);


        // Auto-like the corresponding artist


        return song;
    }

    public String mostPopularArtist() {
        int countLikes=Integer.MIN_VALUE;
        String popularArtist="";
        for(Artist artist:artists){
            if(artist.getLikes() > countLikes){
                popularArtist=artist.getName();
                countLikes=artist.getLikes();
            }
        }
        return popularArtist;
    }

    public String mostPopularSong() {
        Song mostPopularSong = new Song();


        int like = 0;
        for(Song song:songs){
            if(song.getLikes() > like){
                like = song.getLikes();
                mostPopularSong = song;
            }
        }
        String title = mostPopularSong.getTitle();
        return title;
    }

    // Helper methods for retrieving objects by certain properties

    private User getUserByMobile(String mobile) {
        for (User user : users) {
            if (user.getMobile().equals(mobile)) {
                return user;
            }
        }
        return null;
    }

    private Album getAlbumByName(String albumName) {
        for (Album album : albums) {
            if (album.getTitle().equals(albumName)) {
                return album;
            }
        }
        return null;
    }

    private List<Song> getSongsByLength(int length) {
        List<Song> list = new ArrayList<>();
        for (Song song : songs) {
            if (song.getLength() == length) {
                list.add(song);
            }
        }
        return list;
    }

    private List<Song> getSongsByTitles(List<String> songTitles) {
        List<Song> list = new ArrayList<>();
        for (Song song : songs) {
            if (songTitles.contains(song.getTitle())) {
                list.add(song);
            }
        }
        return list;
    }

    private Playlist getPlaylistByTitle(String playlistTitle) {
        for (Playlist playlist : playlists) {
            if (playlist.getTitle().equals(playlistTitle)) {
                return playlist;
            }
        }
        return null;
    }

    private void addListenerToPlaylist(Playlist playlist, User user) {
        if (!playlistListenerMap.containsKey(playlist)) {
            playlistListenerMap.put(playlist, new ArrayList<>());
        }

        List<User> listeners = playlistListenerMap.get(playlist);
        if (!listeners.contains(user)) {
            listeners.add(user);
        }
    }

    private Song getSongByTitle(String songTitle) {
        for (Song song : songs) {
            if (song.getTitle().equals(songTitle)) {
                return song;
            }
        }
        return null;
    }

    private void likeSongByUser(User user, Song song) {
        if (!songLikeMap.containsKey(song)) {
            songLikeMap.put(song, new ArrayList<>());
        }

        List<User> likers = songLikeMap.get(song);
        if (!likers.contains(user)) {
            likers.add(user);
        }

    }


    private void addPlaylistToUser(User user, Playlist playlist) {
        if (!userPlaylistMap.containsKey(user)) {
            userPlaylistMap.put(user, new ArrayList<>());
        }

        List<Playlist> userPlaylists = userPlaylistMap.get(user);
        if (!userPlaylists.contains(playlist)) {
            userPlaylists.add(playlist);
        }
    }
}
