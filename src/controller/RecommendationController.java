package controller;

import model.Course;
import model.Recommendation;
import view.RecommendationView;

import java.io.IOException;
import java.util.ArrayList;

public class RecommendationController {
    private final Recommendation service;
    private final RecommendationView view;

    public RecommendationController(Recommendation service, RecommendationView view) {
        this.service = service;
        this.view = view;
    }

    public void start() {
        view.displayWelcomeMessage();
        String userGoal = null;
        userGoal = view.promptForUserGoal();

        if (userGoal == null || userGoal.isBlank()) {
            view.displayError("Yêu cầu đầu vào không được để trống.");
            return;
        }

        view.displayAnalysisMessage(userGoal);

        try {
            ArrayList<Course> learningPath = service.generateLearningPathFor(userGoal, 5);
            view.displayLearningPath(userGoal, learningPath);
        } catch (IOException | InterruptedException e) {
            view.displayError("Lỗi khi xử lý yêu cầu: " + e.getMessage());
            // In chi tiết lỗi để debug
            e.printStackTrace();
        }



    }
}
