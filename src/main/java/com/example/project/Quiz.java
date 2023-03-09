package com.example.project;

import javax.swing.table.AbstractTableModel;
import javax.xml.crypto.Data;
import java.io.File;

/**
 * Class representing a quiz in the quiz-generator's system.
 */
public class Quiz {

    private int id;
    private User user;
    private String name;
    private Question[] questions;

    private static int count = 1;

    /**
     * Constructor without parameters.
     */
    public Quiz() {
        this(null, "unk", null);
    }

    /**
     * Constructor with parameters. ID created using static counter.
     * @param user user creating quiz.
     * @param name quiz name.
     * @param questions arrays of questions in quiz.
     */
    public Quiz(User user, String name, Question[] questions) {
        this(count++, user, name, questions);
    }

    /**
     * Constructor with parameters.
     * @param id quiz ID; used when reading already existing quizzes without incrementing the counter.
     * @param user user creating quiz.
     * @param name quiz name.
     * @param questions arrays of questions in quiz.
     */
    public Quiz(int id, User user, String name, Question[] questions) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.questions = questions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Question[] getQuestions() {
        return questions;
    }

    public void setQuestions(Question[] questions) {
        this.questions = questions;
    }

    /**
     * Checks whether a question with the same name as the current question (this) already exists in the database.
     * @param db database storing current system information.
     * @return reference to Question object with the same name found in database; null if no question is found.
     */
    public int alreadyExists(Database db) {
        for (int i = 0; i < db.getNoQuizzes(); i++) {
            if (this.equals(db.quizzes[i])) {
                return db.quizzes[i].getId();
            }
        }

        return 0;
    }

    /**
     * Creates new Quiz object in the system based on the parameters passed as command line arguments if no such
     * quiz exists.
     * @param args command line arguments passed to the "-create-quiz" command.
     * @param db database storing current system information.
     */
    public static void create(String[] args, Database db) {
        User user = new User(args[1].split("'")[1],  args[2].split("'")[1]);
        String name = args[3].split("'")[1];

        Quiz quiz = new Quiz(user, name, null);
        if (quiz.alreadyExists(db) > 0) {
            System.out.println("{ 'status' : 'error', 'message' : 'Quizz name already exists'}");
            count--;
            return;
        }

        Question[] questions = new Question[args.length - 4];
        int count = 0;
        for (int i = 4; i < args.length; i++) {
            int id = Integer.parseInt(args[i].split("'")[1]);
            Question question = Question.isValidID(id, db);
            if (question == null) {
                System.out.printf("{ 'status' : 'error', 'message' : 'Question ID for question %d does not exist'}\n", i - 3);
                count--;
                return;
            }
            questions[count++] = question;
        }

        quiz.questions = questions;
        db.addQuiz(quiz);
        System.out.println("{ 'status' : 'ok', 'message' : 'Quizz added succesfully'}");
    }

    /**
     * Finds a specific quiz (printing its ID) in the database based on its name.
     * @param args command line arguments passed to the "-get-quizz-by-name" command.
     * @param db database storing current system information.
     */
    public static void find(String[] args, Database db) {
        if (User.login(args, db) == null) {
            return;
        }

        String name = args[3].split("'")[1];
        Quiz quiz = new Quiz(0, null, name, null);
        if (quiz.alreadyExists(db) == 0) {
            System.out.println("{ 'status' : 'error', 'message' : 'Quizz does not exist'}");
            return;
        }

        System.out.printf("{ 'status' : 'ok', 'message' : '%d'}", quiz.alreadyExists(db));
    }

    /**
     * Prints information about all quizzes currently in the system.
     * @param args command line arguments passed to the "-get-all-quizzes" command.
     * @param db database storing current system information.
     */
    public static void findAll(String[] args, Database db) {
        if (User.login(args, db) == null) {
            return;
        }

        String s = "";
        for (int i = 0; i < db.getNoQuizzes(); i++) {
            String isCompleted = User.login(args, db).isQuizIDCompleted(db.quizzes[i].getId()) ? "True" : "False";
            if (s.equals("")) {
                s += "{\"quizz_id\" : \"" + db.quizzes[i].getId() + "\", \"quizz_name\" : \"" + db.quizzes[i].getName() + "\", \"is_completed\" : \"" + isCompleted + "\"}";
            } else {
                s += ", {\"quizz_id\" : \"" + db.quizzes[i].getId() + "\", \"quizz_name\" : \"" + db.quizzes[i].getName() + "\", \"is_completed\" : \"" + isCompleted + "\"}";
            }
        }

        System.out.println("{ 'status' : 'ok', 'message' : '[" + s + "]'}");
    }

    /**
     * Prints information about a specific quiz indicated by its ID (if said ID is found to be valid).
     * @param args command line arguments passed to the "-get-quizz-details-by-id" command.
     * @param db database storing current system information.
     */
    public static void getDetails(String[] args, Database db) {
        if (User.login(args, db) == null) {
            return;
        }

        Quiz quiz = Quiz.isValidID(Integer.parseInt(args[3].split("'")[1]), db);
        if (quiz == null) {
            System.out.println("{ 'status' : 'error', 'message' : 'Quizz ID does not exist'}");
            return;
        }

        String s ="";
        for (int i = 0; i < quiz.questions.length; i++) {
            Question question = quiz.questions[i];
            String ans = "";

            for (int j = 0; j < question.getAnswers().length; j++) {
                if (ans.equals("")) {
                    ans += "{\"answer_name\":\"" + question.getAnswers()[j].getText() + "\", \"answer_id\":\"" + question.getAnswers()[j].getId() + "\"}";
                } else {
                    ans += ", {\"answer_name\":\"" + question.getAnswers()[j].getText() + "\", \"answer_id\":\"" + question.getAnswers()[j].getId() + "\"}";
                }
            }

            if (s.equals("")) {
                s += "{\"question-name\":\"" + question.getText() + "\", \"question_index\":\"" + (i + 1) +
                        "\", \"question_type\":\"" + question.getType() + "\", \"answers\":\"[" + ans + "]\"}";
            } else {
                s += ", {\"question-name\":\"" + question.getText() + "\", \"question_index\":\"" + (i + 1) +
                        "\", \"question_type\":\"" + question.getType() + "\", \"answers\":\"[" + ans + "]\"}";
            }
        }

        System.out.println("{ 'status' : 'ok', 'message' : '[" + s + "]'}");
    }

    /**
     * Checks whether a given quiz ID is valid (there is a quiz in the current database with that ID).
     * @param id quiz ID to check.
     * @param db database storing current system information.
     * @return reference to Quiz object with the given ID found in the database; null if no such quiz is found.
     */
    public static Quiz isValidID(int id, Database db) {
        for (int i = 0; i < db.getNoQuizzes(); i++) {
            if (id == db.quizzes[i].getId()) {
                return db.quizzes[i];
            }
        }

        return null;
    }

    /**
     * Checks whether the current quiz (this) already has a solution submitted by a given user.
     * @param user User object representing the user to be checked.
     * @return true if quiz was submitted by given user; false otherwise.
     */
    public boolean isSubmitted(User user) {
        if (user.getSolutions() == null) {
            return false;
        }

        for (int i = 0; i < user.getSolutions().length; i++) {
            if (this.equals(user.getSolutions()[i].getQuiz())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifies validity of the command line arguments passed to the "-delete-quizz-by-id" command and triggers
     * the removal of the quiz indicated by said arguments from the database.
     * @param args command line arguments passed to the "-delete-quizz-by-id" command.
     * @param db database storing current system information.
     */
    public static void deleteQuiz(String[] args, Database db) {
        if (User.login(args, db) == null) {
            return;
        }

        if (args.length == 3) {
            System.out.println("{ 'status' : 'error', 'message' : 'No quizz identifier was provided'}");
            return;
        }

        if (Quiz.isValidID(Integer.parseInt(args[3].split("'")[1]), db) == null) {
            System.out.println("{ 'status' : 'error', 'message' : 'No quiz was found'}");
            return;
        }

        Quiz quiz = Quiz.isValidID(Integer.parseInt(args[3].split("'")[1]), db);
        if (!quiz.user.equals(User.login(args, db))) {
            System.out.println("{ 'status' : 'error', 'message' : 'You can only delete the quizzes you created'}");
            return;
        }
        quiz.delete(db);

        System.out.println("{ 'status' : 'ok', 'message' : 'Quizz deleted successfully'}");
    }

    /**
     * Removes current quiz (this) from the database (file and array): Quizzes.csv is deleted and re-written
     * without the quiz that needs to be removed; all solutions of said quiz are also removed.
     * @param db database storing current system information.
     */
    public void delete(Database db) {
        File quizzes = new File("Quizzes.csv");
        quizzes.delete();

        int index = 0;
        for (int i = 0; i < db.getNoQuizzes(); i++) {
            if (this.equals(db.quizzes[i])) {
                index = i;
                continue;
            }
            db.write("Quizzes.csv", db.quizzes[i].toString());
        }

        for (int i = index; i < db.getNoQuizzes() - 1; i++) {
            db.quizzes[i] = db.quizzes[i + 1];
        }

        db.setNoQuizzes(db.getNoQuizzes() + 1);

        SolvedQuiz.remove(this, db);
    }

    /**
     * Creates String representation of a Quiz object as needed for the CSV type file.
     * @return String representing a Quiz.
     */
    public String toString() {
        String s = "";
        for (int i = 0; i < this.questions.length; i++) {
            if (s.equals("")) {
                s += this.questions[i].getId();
            } else {
                s += "," + this.questions[i].getId();
            }
        }

        return this.id + "," + this.user + "," + this.name + "," + s;
    }

    /**
     * Verifies equality of 2 Quiz objects by name.
     * @param quiz Quiz object to be compared to current object (this).
     * @return true if quiz has the same name as the current quiz (this).
     */
    public boolean equals(Quiz quiz) {
        return this.name.equals(quiz.getName());
    }

    /**
     * Clears all quiz related data from the file system.
     */
    public static void clean() {
        count = 1;
        File quiz_file = new File("Quizzes.csv");
        quiz_file.delete();
    }
}
