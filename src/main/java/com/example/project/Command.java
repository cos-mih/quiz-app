package com.example.project;

/**
 * Class that deals with the different types of commands supported by the quiz generator.
 */
public class Command {

    /**
     * Command type
     */
    private String type;

    /**
     * Command line arguments
     */
    private String[] arguments;

    /**
     * Constructor without parameters.
     */
    public Command() {
        this(null);
    }

    /**
     * Constructor with parameters.
     * @param args command line arguments representing a command.
     */
    public Command(String[] args) {
        this.arguments = args;
        this.type = args[0];
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }


    /**
     * Triggers the specific actions associated with each type of command.
     * @param db the Database object that will keep the information created in the system as a result
     *           of each command.
     */
    public void interpreter(Database db) {
        switch (this.type) {
            case "-create-user":
                this.userAction(db);
                break;
            case "-create-question":
                this.questionAction(db);
                break;
            case "-get-question-id-by-text":
                Question.find(this, db);
                break;
            case "-get-all-questions":
                Question.findAll(this, db);
                break;
            case "-create-quizz":
                this.quizAction(db);
                break;
            case "-get-quizz-by-name":
                Quiz.find(this.arguments, db);
                break;
            case "-get-all-quizzes":
                Quiz.findAll(this.arguments, db);
                break;
            case "-get-quizz-details-by-id":
                Quiz.getDetails(this.arguments, db);
                break;
            case "-submit-quizz":
                User.submitQuiz(this.arguments, db);
                break;
            case "-delete-quizz-by-id":
                Quiz.deleteQuiz(this.arguments, db);
                break;
            case "-get-my-solutions":
                User.showSolutions(this.arguments, db);
                break;
            case "-cleanup-all":
                db.cleanup();
                break;
        }
    }

    /**
     * Treats the possible error cases related to user creation.
     * @param db database in use for information storage in the system.
     */
    public void userAction(Database db) {
        String[] args = this.arguments;
        switch (this.userCheckValidity()) {
            case 0:
                String username = args[1].split("'")[1];
                String password = args[2].split("'")[1];
                User.create(username, password, db);
                break;
            case 1:
                System.out.println("{ 'status' : 'error', 'message' : 'Please provide username'}");
                break;
            case 2:
                System.out.println("{ 'status' : 'error', 'message' : 'Please provide password'}");
                break;
        }

    }

    /**
     * Verifies validity of arguments passed to the "-create-user" command.
     * @return 1 if no username is provided; 2 if no password is provided; 0 if both are provided.
     */
    public int userCheckValidity() {
        String[] args = this.arguments;
        switch (args.length) {
            case 1:
                return 1;

            case 2:
                switch (args[1].split(" ")[0]) {
                    case "-u":
                        return 2;

                    case "-p":
                        return 1;

                }
                break;


            case 3:
                if (!args[1].split(" ")[0].equals("-u") || args[1].split(" ").length == 1) {
                    return 1;
                }
                if (!args[2].split(" ")[0].equals("-p") || args[2].split(" ").length == 1) {
                    return 2;
                }
                break;

        }

        return 0;
    }


    /**
     * Treats the possible error cases related to question creation.
     * @param db database in use for information storage in the system.
     */
    public void questionAction(Database db) {
        String[] args = this.arguments;
        if (User.login(args, db) == null) {
            return;
        }
        switch (this.questionCheckValidity()) {
            case 0:
                Question.create(this, db);
                break;
            case 1:
                System.out.println("{ 'status' : 'error', 'message' : 'No answer provided'}");
                break;
            case 2:
                System.out.println("{ 'status' : 'error', 'message' : 'Only one answer provided'}");
                break;
            case 3:
                System.out.println("{ 'status' : 'error', 'message' : 'More than 5 answers were submitted'}");
                break;
            case 4:
                System.out.println("{ 'status' : 'error', 'message' : 'No question text provided'}");
                break;
            case 5:
                System.out.println("{ 'status' : 'error', 'message' : 'Single correct answer question has more than one correct answer'}");
                break;
            case 6:
                System.out.println("{ 'status' : 'error', 'message' : 'Same answer provided more than once'}");
        }
    }

    /**
     * Verifies validity of arguments passed to the "-create-question" command (based on both their number
     * and flags).
     * @return 1 if no answers were provided (there are less than 5 arguments - command name, username, password,
     * question name, type); 2 if only one answer was provided (one or two extra arguments); 3 if there are more
     * than 15 arguments (the maximum for 5 provided answers); 4 if there is no text provided; this.answerValidity()
     * if all previous checks succeeded;
     */
    public int questionCheckValidity() {
        String[] args = this.arguments;

        if (args.length == 5) {
            return 1;
        }

        if (args.length == 6 || args.length == 7) {
            return 2;
        }

        if (args.length > 15) {
            return 3;
        }

        if (args.length == 3 || !args[3].split(" ")[0].equals("-text") || args[3].split(" ").length == 1) {
            return 4;
        }

        return this.answersValidity();
    }

    /**
     * Verifies string of arguments corresponding to the answers for a "-create-question" command.
     * @return -1 if any answer lacks an associated text or value flag; 5 if more than one correct answer were
     * counted for a question marked "single"; 6 if there are two answers with the same text; 0 if all checks succeed.
     */
    public int answersValidity() {
        String[] args = this.arguments;
        String type = args[4].split("'")[1];

        String[] answers = new String[5];
        int ansCount = 0, correctCount = 0;
        for (int i = 5; i < args.length; i++) {
            String[] currentAnswer = args[i].split("'");
            if (!currentAnswer[0].trim().equals("-answer-" + (ansCount + 1)) || currentAnswer.length == 1) {
                System.out.printf("{ 'status' : 'error', 'message' : 'Answer %d has no answer description'}\n", ansCount + 1);
                return -1;
            }
            answers[ansCount++] = currentAnswer[1];

            i++;
            String[] flags = args[i].split("'");
            if (i >= args.length || !flags[0].trim().equals("-answer-" + ansCount + "-is-correct") || flags.length == 1) {
                System.out.printf("{ 'status' : 'error', 'message' : 'Answer %d has no answer correct flag'}\n", ansCount);
                return -1;
            }
            correctCount += Integer.parseInt(flags[1]);
        }

        if (type.equals("single") && correctCount > 1) {
            return 5;
        }

        for (int i = 0; i < ansCount - 1; i++) {
            for (int j = i + 1; j < ansCount; j++) {
                if (answers[i].equals(answers[j])) {
                    return 6;
                }
            }
        }

        return 0;
    }

    /**
     * Treats the possible error cases related to quiz creation.
     * @param db database in use for information storage in the system.
     */
    public void quizAction(Database db) {
        String[] args = this.arguments;
        if (User.login(args, db) == null) {
            return;
        }
        switch (this.quizCheckValidity()) {
            case 0:
                Quiz.create(args, db);
                break;
            case 1:
                System.out.println("{ 'status' : 'error', 'message' : 'Quizz has more than 10 questions'}");
                break;
        }
    }

    /**
     * Verifies arguments for the "-create-quiz" command.
     * @return 1 if more than 10 questions have been provided (more than 14 arguments, together with the
     * command name, username, password and quiz name; 0 if check is successful.
     */
    public int quizCheckValidity() {

        if (this.arguments.length > 14) {
            return 1;
        }

        return 0;
    }
}


