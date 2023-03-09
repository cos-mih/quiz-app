package com.example.project;

import java.io.*;
import java.util.Arrays;

/**
 * Class that keeps the information about the system at any given point during runtime.
 */
public class Database {

    User[] users;
    Question[] questions;
    Quiz[] quizzes;
    private int noUsers;
    private int noQuestions;
    private int noQuizzes;

    /**
     * Constructor with no parameters that initialises all fields.
     */
    public Database() {
        this.users = new User[0];
        this.questions = new Question[0];
        this.quizzes = new Quiz[0];
        noUsers = 0;
        noQuestions = 0;
        noQuizzes = 0;
    }

    public int getNoUsers() {
        return noUsers;
    }

    public void setNoUsers(int noUsers) {
        this.noUsers = noUsers;
    }

    public int getNoQuestions() {
        return noQuestions;
    }

    public void setNoQuestions(int noQuestions) {
        this.noQuestions = noQuestions;
    }

    public int getNoQuizzes() {
        return noQuizzes;
    }

    public void setNoQuizzes(int noQuizzes) {
        this.noQuizzes = noQuizzes;
    }

    /**
     * Attempts to read data from all files in the system to populate each specific structure.
     */
    public void connect() {
        this.readUsers();
        this.readQuestions();
        this.readQuizzes();
        this.readSolutions();
    }

    /**
     * Reads data associated with users from a "Users.csv" file, in a "username,password" format and
     * adds each user to the users array as a new object.
     */
    public void readUsers() {
        if (!new File("Users.csv").exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(("Users.csv")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String readUsername = line.split(",")[0];
                String readPassword = line.split(",")[1];
                User user = new User(readUsername, readPassword);
                this.users = Arrays.copyOf(this.users, this.users.length + 1);
                this.users[noUsers++] = user;
            }

        } catch (IOException e) {
            System.out.println("{ 'status' : 'error', 'message' : 'Could not read file'}");
        }
    }

    /**
     * Reads data associated with questions from a "Questions.csv" file, in a "id,text,answer,answerFlag,..." format
     * and adds each question to the question array as a new object.
     */
    public void readQuestions() {
        if (!new File("Questions.csv").exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(("Questions.csv")))) {
            String line;
            int ansCount = 1;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                int id = Integer.parseInt(split[0]);
                String text = split[1], type = split[2];
                int noAnswers = (split.length - 3) / 2;
                Answer[] answers = new Answer[noAnswers];
                int count = 0;
                for (int i = 3; i < split.length; i++) {
                    String ansText = split[i++];
                    boolean ansFlag = split[i].equals("true") ? true : false;
                    Answer newAnswer = new Answer(ansCount++, ansText, ansFlag);
                    answers[count++] = newAnswer;
                }
                Question question = new Question(id, text, type, answers);
                this.questions = Arrays.copyOf(this.questions, this.questions.length + 1);
                this.questions[noQuestions++] = question;
            }

        } catch (IOException e) {
            System.out.println("{ 'status' : 'error', 'message' : 'Could not read file'}");
        }
    }

    /**
     * Reads data associated with quizzes from a "Quizzes.csv" file, in a "id,username,password,name,questionID,..."
     * format and adds each quiz to the quizzes array as a new object.
     */
    public void readQuizzes() {
        if (!new File("Quizzes.csv").exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(("Quizzes.csv")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                int id = Integer.parseInt(split[0]);
                String username = split[1], password = split[2], name = split[3];
                User user = new User(username, password);
                Question[] questions = new Question[split.length - 4];
                for (int i = 4; i < split.length; i++) {
                    int qid = Integer.parseInt(split[i]);
                    for (int j = 0; j < noQuestions; j++) {
                        if (qid == this.questions[j].getId()) {
                            questions[i - 4] = this.questions[j];
                        }
                    }
                }
                Quiz quiz = new Quiz(id, user, name, questions);
                this.quizzes = Arrays.copyOf(this.quizzes, this.quizzes.length + 1);
                this.quizzes[noQuizzes++] = quiz;
            }

        } catch (IOException e) {
            System.out.println("{ 'status' : 'error', 'message' : 'Could not read file'}");
        }
    }

    /**
     * Reads data associated with solutions from a "Solutions.csv" file, in a "username,quizID,scorer" format and
     * adds each solution as a new object to the corresponding user in the users array.
     */
    public void readSolutions() {
        if (!new File("Solutions.csv").exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(("Solutions.csv")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                String username = split[0];
                int qId = Integer.parseInt(split[1]);
                int score = Integer.parseInt(split[2]);

                User user = new User(username, "unk");
                user = user.alreadyExists(this);

                SolvedQuiz solution = new SolvedQuiz(Quiz.isValidID(qId, this), score);
                user.setSolutions(Arrays.copyOf(user.getSolutions(), user.getSolutions().length + 1));
                user.setSolutionsIndex(user.getSolCount(), solution);
                user.setSolCount(user.getSolCount() + 1);
            }

        } catch (IOException e) {
            System.out.println("{ 'status' : 'error', 'message' : 'Could not read file'}");
        }
    }

    /**
     * Writes text in file with specified name.
     * @param file filename.
     * @param text text to be written.
     */
    public void write(String file, String text) {
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(text);
        } catch (IOException e) {
            System.out.println("{ 'status' : 'error', 'message' : 'Could not write in file'}");
        }
    }

    /**
     * Adds new user to current database(file and array).
     * @param user user to be added.
     */
    public void addUser(User user) {
        this.write("Users.csv", user.toString());

        this.users = Arrays.copyOf(this.users, this.users.length + 1);
        this.users[noUsers++] = user;
    }

    /**
     * Adds new question to current database(file and array).
     * @param question question to be added
     */
    public void addQuestion(Question question) {
        this.write("Questions.csv", question.toString());

        this.questions = Arrays.copyOf(this.questions, this.questions.length + 1);
        this.questions[noQuestions++] = question;
    }

    /**
     * Adds new quiz to current database(file and array).
     * @param quiz
     */
    public void addQuiz(Quiz quiz) {
        this.write("Quizzes.csv", quiz.toString());

        this.quizzes = Arrays.copyOf(this.quizzes, this.quizzes.length + 1);
        this.quizzes[noQuizzes++] = quiz;
    }

    /**
     * Adds new solution to current database(file and array).
     * @param user user that adds the solution.
     * @param solution solution to be added.
     */
    public void addSolution(User user, SolvedQuiz solution) {
        this.write("Solutions.csv", user.getUsername() + "," + solution.getQuiz().getId() + "," + solution.getScore());

    }

    /**
     * Deletes all information about the system being currently stored in files.
     */
    public void cleanup() {
        User.clean();
        Question.clean();
        Quiz.clean();
        SolvedQuiz.clean();
    }
}
