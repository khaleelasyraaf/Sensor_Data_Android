package com.example.lhapp4;

public class QuestionsLibrary {

    private String mQuestions [] = {
            "Question 1",
            "Question 2",
            "Question 3",
            "Question 4",
            "Thank you!"
    };

    public String getQuestion(int a) {
        String question = "Q:" + mQuestions[a];
        return question;
    }

}
