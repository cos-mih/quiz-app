package com.example.project;

import java.io.File;

/**
 * Class representing a solution to a quiz.
 */
public class SolvedQuiz {

    private Quiz quiz;

    private int score;

    /**
     * Constructor without parameters.
     */
    public SolvedQuiz() {
        this(null, 0);
    }

    /**
     * Constructor with arguments.
     * @param quiz quiz solved.
     * @param score score associated with the solution.
     */
    public SolvedQuiz(Quiz quiz, int score) {
        this.quiz = quiz;
        this.score = score;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Removes all solutions of a certain quiz from the database: deletes "Solution.csv", deletes solutions from
     * each users solution array and re-writes "Solutions.csv" with the updated data.
     * @param quiz quiz defining the solutions that need to be removed.
     * @param db database storing current system information.
     */
    public static void remove(Quiz quiz, Database db) {
        File sol = new File("Solutions.csv");
        sol.delete();

        for (int i = 0; i < db.getNoUsers(); i++) {
            User user = db.users[i];

            if (user.getSolCount() == 0) {
                continue;
            }

            int j = 0;
            while (j < user.getSolCount() - 1) {
                SolvedQuiz solution = user.getSolutionsIndex(i);
                if (solution.getQuiz().equals(quiz)) {
                    for (int k = j; k < user.getSolCount() - 2; k++) {
                        user.setSolutionsIndex(k, user.getSolutionsIndex(k + 1));
                    }
                    user.setSolCount(user.getSolCount() - 1);
                } else {
                    j++;
                }
            }
            if (user.getSolutionsIndex(user.getSolCount() - 1).getQuiz().equals(quiz)) {
                user.setSolutionsIndex(user.getSolCount() - 1, null);
                user.setSolCount(user.getSolCount() - 1);
            }
        }

        for (int i = 0; i < db.getNoUsers(); i++) {
            User user = db.users[i];

            for (int j = 0; j < user.getSolCount(); j++) {
                SolvedQuiz solution = user.getSolutionsIndex(i);
                db.write("Solutions.csv", user.getUsername() + "," + solution.getQuiz().getId() + "," + solution.getScore());
            }
        }
    }

    /**
     * Clears all solutions related data from the file system.
     */
    public static void clean() {
        File solFile = new File("Solutions.csv");
        solFile.delete();
    }
}
