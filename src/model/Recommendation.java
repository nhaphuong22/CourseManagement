package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Recommendation {
    private final CourseList courseDB;
    private final GeminiClient apiClient;

    public Recommendation(CourseList courseDB, GeminiClient apiClient) {
        this.courseDB = courseDB;
        this.apiClient = apiClient;
    }

    public ArrayList<Course> generateLearningPathFor(String userGoal, int numberOfSteps) throws IOException, InterruptedException {
        ArrayList<Course> allCourses = courseDB.getAllCourses();
        ArrayList<String> pathIds = apiClient.fetchLearningPath(userGoal, allCourses);

        if(pathIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Trả về danh sách Course theo đúng thứ tự mà API đã đề xuất
        ArrayList<Course> recommendedCourses = (ArrayList<Course>) pathIds.stream()
                .map(courseDB::getCourseById)
                .filter(course -> course != null)
                .limit(numberOfSteps)
                .collect(Collectors.toList());

        // Thêm tóm tắt cho từng khóa học được gợi ý
        for (int i = 0; i < recommendedCourses.size(); i++) {
            Course course = recommendedCourses.get(i);
            // Tạo mô tả dựa trên thông tin thực tế của khóa học
            StringBuilder description = new StringBuilder();
            description.append("Khóa học: ").append(course.getCourseName()).append("\n");
            description.append("Chủ đề: ").append(course.getTopic()).append("\n");
            description.append("Cấp độ: ").append(course.getDifficulty()).append("\n");
            String summary = apiClient.summarizeCourseDescription(description.toString());
            // Tạo Course mới với tóm tắt
            Course courseWithSummary = new Course(
                course.getCourseId(),
                course.getCourseName(),
                course.getTopic(),
                course.getDifficulty(),
                summary
            );
            recommendedCourses.set(i, courseWithSummary);
        }
        return recommendedCourses;
    }
}
