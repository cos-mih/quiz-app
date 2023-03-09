package com.example.project;

/**
 * Class representing an answer to a question in the quiz-generator's system.
 */
public class Answer {

    private int id;
    private String text;
    private boolean value;

    static int count = 1;

    /**
     * Constructor  without parameters.
     */
    public Answer() {
        this(-1, "unk", false);
    }

    /**
     * Constructor with parameters; ID created using a static counter.
     * @param text answer text.
     * @param value answer correctness value.
     */
    public Answer(String text, boolean value) {
        this(count++, text, value);
    }

    /**
     * Constructor with parameters.
     * @param id answer id; used for reading already existing answers without incrementing the counter.
     * @param text answer text.
     * @param value answer correctness value.
     */
    public Answer(int id,  String text, boolean value) {
        this.id = id;
        this.text = text;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    /**
     * Checks whether the current answer's ID exists in an array of int values given as a representation of
     * selected answers for a certain quiz.
     * @param answerIDs array of int values representing the IDs of all the answers chosen by a user for a quiz
     *                  containing the current question (this).
     * @return true if answer is marked as chosen (its ID exists in answerIDs); false if not.
     */
    public boolean isChosen(int[] answerIDs) {
        for (int i = 0; i < answerIDs.length; i++) {
            if (this.getId() == answerIDs[i]) {
                return true;
            }
        }

        return false;
    }
}
