package com.example.wordle;

import java.io.Serializable;
/**
 *
 * @author Rey Sanayei
 * This class represents a guess. It stores the original guess, the results
 * of the guess for each specific character index, and whether this guess was
 * correct or not.
 *
 */
public class Guess implements Serializable {

    private String guess;
    private INDEX_RESULT[] indices;
    private boolean isCorrect;
    private static final long serialVersionUID = 6522172245622776147L;

    /**
     * Guess constructor.
     * @param guess A string of the original guess.
     * @param indices An array describing the correctness of each individual index in the guess.
     * @param isCorrect A boolean of whether that guess is correct or not.
     */
    public Guess(String guess, INDEX_RESULT[] indices, boolean isCorrect) {
        if (guess.length() != indices.length) {
            throw new IllegalArgumentException("The length of the guess and its" +
                    " index results must be equal.");
        }
        this.guess = guess;
        this.indices = indices;
        this.isCorrect = isCorrect;
    }

    /**
     * Returns the original guess.
     * @return A string of the original guess.
     */
    public String getGuess() {
        return this.guess;
    }

    /**
     * Returns the results at each index of the guess.
     * @return An array of the results of the guess at each index.
     */
    public INDEX_RESULT[] getIndices() {
        return this.indices;
    }

    /**
     * Returns whether this guess was correct or not.
     * @return A boolean stating whether this guess was correct or not.
     */
    public boolean getIsCorrect() {
        return this.isCorrect;
    }
}