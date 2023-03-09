package com.example.project;

import java.io.*;

/**
 * Class representing a question in the quiz-generator's system.
 */
public class Question {

    private int id;
    private String text;
    private String type;
    private Answer[] answers;

    private static int count = 1;

    /**
     * Constructor without parameters.
     */
    public Question() {
        this("unk", "unk", null);
    }

    /**
     * Constructor with parameters; ID created using a static counter.
     * @param text question text.
     * @param type question type.
     * @param answers answer array.
     */
    public Question(String text, String type, Answer[] answers) {
        this(count++, text, type, answers);
    }

    /**
     * Constructor with parameters.
     * @param id question ID to be set; used for reading already existing questions without incrementing the counter.
     * @param text question text.
     * @param type question type.
     * @param answers answer array.
     */
    public Question(int id, String text, String type, Answer[] answers) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.answers = answers;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Answer[] getAnswers() {
        return answers;
    }

    public void setAnswers(Answer[] answers) {
        this.answers = answers;
    }


    /**
     * Checks whether a question with the same text as the current one (this) already exists in the database.
     * @param db database storing current system information.
     * @return ID of the question with the same text found in the database; 0 if no question was found.
     */
    public int alreadyExists(Database db) {
        for (int i = 0; i < db.getNoQuestions(); i++) {
            if (this.equals(db.questions[i])) {
                return db.questions[i].getId();
            }
        }

        return 0;
    }

    /**
     * Extracts the elements of a new question from the arguments passed to the "-create-question" command .
     * Creates new question in the system if there is no question with the given parameters already.
     * @param command Command object that triggered question creation.
     * @param db database storing current system information.
     */
    public static void create(Command command, Database db) {
        String[] args = command.getArguments();
        String text = args[3].split("'")[1];
        String type = args[4].split("'")[1];
        int noAnswers = (args.length - 5) / 2;

        Answer[] answers = new Answer[noAnswers];
        int index = 5;
        for (int i = 0; i < noAnswers; i++) {
            String ansText = args[index++].split("'")[1];
            boolean ansFlag = args[index++].split("'")[1].equals("1") ? true : false;
            Answer newAnswer = new Answer(ansText, ansFlag);
            answers[i] = newAnswer;
        }

        Question question = new Question(text, type, answers);

        if (question.alreadyExists(db) > 0) {
            count--;
            System.out.println("{ 'status' : 'error', 'message' : 'Question already exists'}");
            return;
        }

        db.addQuestion(question);

        System.out.println("{ 'status' : 'ok', 'message' : 'Question added successfully' }");
    }

    /**
     * Finds a specific question in the database based on its text.
     * @param command Command object that called the "find" function.
     * @param db database storing current system information.
     */
    public static void find(Command command, Database db) {
        String[] args = command.getArguments();
        if (User.login(args, db) == null) {
            return;
        }

        String text = args[3].split("'")[1];
        Question question = new Question(0, text, "unk", null);
        if (question.alreadyExists(db) == 0) {
            System.out.println("{ 'status' : 'error', 'message' : 'Question does not exist' }");
            return;
        }

        System.out.printf("{ 'status' : 'ok', 'message' : '%d' }", question.alreadyExists(db));
    }

    /**
     * Prints information about all questions currently existing in the system after a successful login.
     * @param command Command object that called the "findAll" function.
     * @param db database storing current information in the system.
     */
    public static void findAll(Command command, Database db) {
        String[] args = command.getArguments();
        if (User.login(args, db) == null) {
            return;
        }

        String s = "";
        for (int i = 0; i < db.getNoQuestions(); i++) {
            if (s.equals("")) {
                s += "{\"question_id\" : \"" + db.questions[i].getId() + "\", \"question_name\" : \"" + db.questions[i].getText() + "\"}";
            } else {
                s += ", {\"question_id\" : \"" + db.questions[i].getId() + "\", \"question_name\" : \"" + db.questions[i].getText() + "\"}";
            }
        }

        System.out.println("{ 'status' : 'ok', 'message' : '[" + s + "]'}");
    }

    /**
     * Checks whether a question with a given ID exists in the database.
     * @param id question ID to verify.
     * @param db database storing current information.
     * @return reference to Question object with the given question ID; null if no question with the ID was found.
     */
    public static Question isValidID(int id, Database db) {
        for (int i = 0; i < db.getNoQuestions(); i++) {
            if (id == db.questions[i].getId()) {
                return db.questions[i];
            }
        }

        return null;
    }

    /**
     * Calculates the score (out of 1) associated with each correct answer based on their number.
     * @return score for each correct answer to the current question (this).
     */
    public double getCorrectAnswerScore() {
        int correctCount = 0;
        for (int i = 0; i < this.answers.length; i++) {
            if (this.answers[i].getValue()) {
                correctCount++;
            }
        }

        return 1f / correctCount;
    }

    /**
     * Calculates the score (out of 1) associated with each wrong answer based on their number.
     * @return score for each wrong answer to the current question (this).
     */
    public double getWrongAnswerScore() {
        int wrongCount = 0;
        for (int i = 0; i < this.answers.length; i++) {
            if (!this.answers[i].getValue()) {
                wrongCount++;
            }
        }

        return -1f / wrongCount;
    }

    /**
     * Calculates score associated with the current question (this) when the answers chosen are passed as an array
     * of int values representing their IDs. Iterates through all possible answers to the question, verifies whether
     * they have been chosen (their ID is found in the given ID array), and adds the score corresponding to
     * its correct-flag value.
     * @param answerIDs array of int values representing the IDs of all the answers chosen by a user for a quiz
     *                  containing the current question (this).
     * @return score for question.
     */
    public double getScore(int[] answerIDs) {
        double score = 0;
        for (int i = 0; i < this.answers.length; i++) {
            if (this.answers[i].isChosen(answerIDs)) {
                if (this.answers[i].getValue()) {
                    score += this.getCorrectAnswerScore();
                } else {
                    score += this.getWrongAnswerScore();
                }
            }
        }

        return score;
    }

    /**
     * Creates String representation of a Question object as needed for the CSV type file.
     * @return String representing Question.
     */
    public String toString() {
        String s =  "";
        for (int i = 0; i < this.answers.length; i++) {
            s += "," + this.answers[i].getText() + "," + this.answers[i].getValue();
        }
        return this.id + "," + this.text + "," + this.type + s;
    }

    /**
     * Verifies equality of 2 Question objects.
     * @param question Question object the current object (this) is being compared to.
     * @return true if both questions have the same text; false otherwise.
     */
    public boolean equals(Question question) {
        return this.text.equals(question.getText());
    }

    /**
     * Clears all question related data from the file system.
     */
    public static void clean() {
        count = 1;
        Answer.count = 1;
        File questions_file = new File("Questions.csv");
        questions_file.delete();
    }
}
