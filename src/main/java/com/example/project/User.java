package com.example.project;

import java.io.*;
import java.util.Arrays;

/**
 * Class that represents a user in the quiz-generator's system.
 */
public class User {
    private String username;
    private String password;

    private SolvedQuiz[] solutions;

    private int solCount = 0;

    /**
     * Constructor without parameters.
     */
    public User() {
        this("unk", "unk");
    }

    /**
     * Constructor with parameters.
     * @param username username to be set.
     * @param password password to be set for user.
     */
    public User(String username, String password) {
        this(username, password, new SolvedQuiz[0]);
    }

    /**
     * Constructor with parameters.
     * @param username username for new user.
     * @param password password for new user.
     * @param solutions array of solutions attributed to the user.
     */
    public User(String username, String password, SolvedQuiz[] solutions) {
        this.username = username;
        this.password = password;
        this.solutions = solutions;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SolvedQuiz[] getSolutions() {
        return solutions;
    }

    public void setSolutions(SolvedQuiz[] solutions) {
        this.solutions = solutions;
    }

    public SolvedQuiz getSolutionsIndex(int i) {
        return solutions[i];
    }

    public void setSolutionsIndex(int i, SolvedQuiz solution) {
        this.solutions[i] = solution;
    }

    public int getSolCount() {
        return solCount;
    }

    public void setSolCount(int solCount) {
        this.solCount = solCount;
    }


    /**
     * Checks whether a user with the same username as "this" is already registered in the database.
     * @param db database containing user list.
     * @return reference to the object in the user array (of the database) representing said found user;
     * null if the user does not exist.
     */
    public User alreadyExists(Database db) {
        for (int i = 0; i < db.getNoUsers(); i++) {
            if (this.username.equals(db.users[i].username)) {
                return db.users[i];
            }
        }
        return null;
    }

    /**
     * Creates a new user in the system if there is no user with the same given parameters already.
     * @param username username for new user.
     * @param password password for new user.
     * @param db database storing current information.
     */
    public static void create(String username, String password, Database db) {
        User user = new User(username, password);
        if (user.alreadyExists(db) != null) {
            System.out.println("{ 'status' : 'error', 'message' : 'User already exists' }");
            return;
        }

        db.addUser(user);

        System.out.println("{ 'status' : 'ok', 'message' : 'User created successfully' }");
    }

    /**
     * Checks whether the user (this) is a valid existing user in the system.
     * @param db database storing current information.
     * @return reference to the user object found in the database; null if it is not a valid user.
     */
    public User userIsValid(Database db) {
        for (int i = 0; i < db.getNoUsers(); i++) {
            if (this.equals(db.users[i])) {
                return db.users[i];
            }
        }

        return null;
    }

    /**
     * Given the string of command line arguments passed to any command "login" verifies if all user credentials
     * are provided and valid before permitting any new system actions.
     * @param args
     * @param db
     * @return
     */
    public static User login(String[] args, Database db) {
        if (args.length <= 2) {
            System.out.println("{ 'status' : 'error', 'message' : 'You need to be authenticated' }");
            return null;
        }

        String[] u = args[1].split("'");
        String[] p = args[2].split("'");
        User user = new User(u[1], p[1]);
        if (!u[0].trim().equals("-u") || !p[0].trim().equals("-p") || user.userIsValid(db) == null) {
            System.out.println("{ 'status' : 'error', 'message' : 'Login failed' }");
            return null;
        }

        return user.userIsValid(db);
    }

    /**
     * Verifies whether the quiz with a given ID was already submitted by the user (this).
     * @param id quiz ID to check.
     * @return true if quiz with given ID exists in the solutions array of the user; false otherwise.
     */
    public boolean isQuizIDCompleted(int id) {
        if (this.solutions == null) {
            return false;
        }

        for (int i = 0; i < this.solutions.length; i++) {
            if (id == this.solutions[i].getQuiz().getId()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Given the string of command line arguments, "submitQuiz" treats possible error cases of a submit attempt.
     * @param args command line arguments given for the "-submit-quizz" command.
     * @param db database storing current system information.
     */
    public static void submitQuiz(String[] args, Database db) {
        if (User.login(args, db) == null) {
            return;
        }

        switch (checkAttempt(args, db)) {
            case 0:
                User.scoreQuiz(args, db);
                break;
            case 1:
                System.out.println("{ 'status' : 'error', 'message' : 'No quizz identifier was provided'}");
                break;
            case 2:
                System.out.println("{ 'status' : 'error', 'message' : 'No quiz was found'}");
                break;
            case 3:
                System.out.println("{ 'status' : 'error', 'message' : 'You already submitted this quizz'}");
                break;
            case 4:
                System.out.println("{ 'status' : 'error', 'message' : 'You cannot answer your own quizz'}");
                break;
        }
    }

    /**
     * Verifies validity of a submit attempt made through the "-submit-quizz" command.
     * @param args command line arguments given to command.
     * @param db database storing current system information.
     * @return 1 if only credentials were provided, without any quiz; 2 if the quiz ID provided does not exist;
     * 3 if the quiz has already been submitted by the current user; 4 if the quiz was created by the same user
     * that is attempting  to submit it; 0 if all checks succeeded.
     */
    public static int checkAttempt(String[] args, Database db) {
        if (args.length == 3 || args[3].split(" ").length == 1) {
            return 1;
        }

        int id = Integer.parseInt(args[3].split("'")[1]);
        if(Quiz.isValidID(id, db) == null) {
            return 2;
        }

        if (Quiz.isValidID(id, db).isSubmitted(User.login(args, db))) {
            return 3;
        }

        if (Quiz.isValidID(id, db).getUser().equals(User.login(args, db))) {
            return 4;
        }

        return 0;
    }

    /**
     * Given the valid string of command line arguments passed to the "-submit-quizz" command, "scoreQuiz"
     * instantiates the elements for a new solution and adds it to the database.
     * @param args command line arguments passed to "-submit-quizz".
     * @param db database storing current system information.
     */
    public static void scoreQuiz(String[] args, Database db) {
        User user = User.login(args, db);
        int id = Integer.parseInt(args[3].split("'")[1]);
        Quiz quiz = Quiz.isValidID(id, db);

        int[] answerIDs = new int[args.length - 4];
        for (int i = 0; i < args.length - 4; i++) {
            answerIDs[i] = Integer.parseInt(args[i + 4].split("'")[1]);
        }

        user.addSolution(quiz, answerIDs, db);

        System.out.printf("{ 'status' : 'ok', 'message' : '%d points'}\n", user.getPoints(quiz));
    }

    /**
     * Calculates the score associated with a given quiz (question by question) based on a given list of answers
     * and add a new solution to the solutions array of the current user; each question has the same proportion of
     * the total score that gets multiplied with each question's individual score out of 1.
     * @param quiz quiz to be scored.
     * @param answerIDs array of int values representing all IDs of answers submitted as correct.
     * @param db database storing current system information.
     */
    public void addSolution(Quiz quiz, int[] answerIDs, Database db) {
        double score = 0;

        int noQuestions = quiz.getQuestions().length;
        double marksPerQuestion = 100f / noQuestions;

        for (int i = 0; i < quiz.getQuestions().length; i++) {
            Question currentQuestion = quiz.getQuestions()[i];
            score += marksPerQuestion * currentQuestion.getScore(answerIDs);
        }

        if (score < 0) {
            score = 0;
        }

        SolvedQuiz solution = new SolvedQuiz(quiz, (int)Math.round(score));
        this.solutions = Arrays.copyOf(this.solutions, this.solutions.length + 1);
        this.solutions[this.solCount++] = solution;

        db.addSolution(this, solution);
    }

    /**
     * Finds the current user's score for a certain given quiz.
     * @param quiz quiz needed to be found.
     * @return score for given quiz if it was previously submitted; -1 if it was not submitted.
     */
    public int getPoints(Quiz quiz) {
        for (int i = 0; i < this.solutions.length; i++) {
            if (quiz.equals(this.solutions[i].getQuiz())) {
                return this.solutions[i].getScore();
            }
        }

        return -1;
    }

    /**
     * Prints information about all quizzes with their associated scores found in the solutions array of the
     * current user.
     * @param args command line arguments passe to the "-get-my-solutions" command.
     * @param db database storing current system information.
     */
    public static void showSolutions(String[] args, Database db) {
        if (User.login(args, db) == null) {
            return;
        }

        String s = "";
        User user = User.login(args, db);
        for (int i = 0; i < user.solutions.length; i++) {
            if (!s.equals("")) {
                s += ", ";
            }
            s += "{\"quiz-id\" : \"" + user.solutions[i].getQuiz().getId() + "\", \"quiz-name\" : \"" + user.solutions[i].getQuiz().getName() +
                    "\", \"score\" : \"" + user.solutions[i].getScore() + "\", \"index_in_list\" : \"" + (i + 1) + "\"}";
        }

        System.out.println("{ 'status' : 'ok', 'message' : '[" + s + "]'}");
    }

    /**
     * Creates String representation of a User object as needed for the CSV type file.
     * @return String representing User.
     */
    public String toString() {
        return this.getUsername() + ',' + this.getPassword();
    }

    /**
     * Verifies equality of 2 User objects.
     * @param user User object the current object (this) is being compared to.
     * @return true if both users have the same username and password; false otherwise.
     */
    public boolean equals(User user) {
        return this.username.equals(user.getUsername()) && this.password.equals(user.getPassword());
    }

    /**
     * Clears user related data from the file system.
     */
    public static void clean() {
        File users_file = new File("Users.csv");
        users_file.delete();
    }
}
