package com.example.wordle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author Rey Sanayei
 * This class represents the player of this game and the essential data related to them.
 * The data includes their total games played, total wins, current streak, max streak,
 * previous wins breakdown,username, profile picture (path), theme prefrence,
 * and a unique ID for communicating with the server.
 *
 */
public class Player implements Serializable {
    private final static int MAX_GUESSES = 6;
    // Unique ID for communicating with the server
    private String ID;
    private int totalPlays;
    private int totalWins;
    private int curStreak;
    private int maxStreak;
    // Username
    private String name;
    private String profilePicPath;
    // HashMap storing the user's previous wins' breakdown by guesses used
    private HashMap<Integer, Integer> guessHistory;
    private boolean isThemeLight;
    private static final long serialVersionUID = 7526472245622776147L;

    public Player(){
        // Generating the unique ID
        ID = Long.toString(System.currentTimeMillis()) + (new Random()).nextInt(1000) +
                (new Random()).nextInt(1000);
        totalPlays = 0;
        totalWins = 0;
        curStreak = 0;
        maxStreak = 0;
        // Default username
        name = "Guest";
        guessHistory = new HashMap<Integer, Integer>();
        profilePicPath = "";
        // initializing the map with 0s
        for (int i = 1; i<= MAX_GUESSES; i++){
            guessHistory.put(i,0);
        }
        isThemeLight = true;
    }

    /**
     * Getter method for curStreak
     * @return int curStreak
     */
    public int getCurStreak() {
        return curStreak;
    }

    /**
     * Update method for curStreak
     * @param didWin  boolean determining if the streak should be continued or cut off.
     */
    public void updateCurStreak(boolean didWin) {
        if(didWin)
            this.curStreak++;
        else
            this.curStreak = 0;
        if (this.curStreak > this.maxStreak)
            this.maxStreak = this.curStreak;
    }

    /**
     * Getter method for maxStreak
     * @return int maxStreak
     */
    public int getMaxStreak() {
        return maxStreak;
    }

    /**
     * Getter method for the username
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for the username
     * @param name the new username
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for the profilePicPath
     * @return String the profile Picture's Path
     */
    public String getProfilePicPath() {
        return profilePicPath;
    }

    /**
     * Setter method for the profilePicPath
     * @param profilePicPath
     */
    public void setProfilePicPath(String profilePicPath) {
        this.profilePicPath = profilePicPath;
    }

    /**
     * Getter method for the Guess History HashMap
     * @return HashMap<Integer, Integer> the guessHistory
     */
    public HashMap<Integer, Integer> getGuessHistory(){
        return this.guessHistory;
    }

    /**
     * Update method for guessHistory HashMap
     * @param attempts int representing the number of guesses it took to win the game
     */
    public void updateGuessHistory(int attempts){
        if(attempts <= MAX_GUESSES)
            this.guessHistory.put(attempts, this.guessHistory.get(attempts) + 1);
    }

    /**
     * Getter method for the total games played by the player
     * @return int totalPlays
     */
    public int getTotalPlays() {
        return totalPlays;
    }

    /**
     * Update method for the total games played by the player
     */
    public void updateTotalPlays() {
        this.totalPlays++;
    }

    /**
     * Getter method for the player's total wins
     * @return int totalWins
     */
    public int getTotalWins() {
        return totalWins;
    }

    /**
     * Update method for the total games won by the player
     * @param didWin boolean value determining if the game was won or not
     */
    public void updateTotalWins(boolean didWin) {
        if(didWin)
            this.totalWins++;
    }

    /**
     * Getter method for the user's Theme preference
     * @return boolean isThemeLight
     */
    public boolean isThemeLight() {
        return isThemeLight;
    }

    /**
     * Setter method for the user's Theme preference
     * @param themeLight the boolean value (true: theme is light)
     */
    public void setThemeLight(boolean themeLight) {
        isThemeLight = themeLight;
    }

    public void setID(String id){
        ID = id;
    }

    public String getID() {return ID;}
}
