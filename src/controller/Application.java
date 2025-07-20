package controller;

import model.CourseList;
import model.GeminiClient;
import model.Recommendation;
import view.ListView;
import view.Menu;
import view.RecommendationView;
import view.Validation;

public class Application extends Menu<String> {

    private static String[] options = {
            "Hiển thị tất cả khóa học",
            "Tìm kiếm khóa học",
            "Sắp xếp các khóa học theo tên",
            "Xóa một khóa học",
            "AI Tư vấn lộ trình học",
            "Đăng kí khóa học mới",
            "Tóm tắt khóa học"
    };
    private CourseList courseList;

    public Application(String td, String[] mc) {
        super(td, mc);
        courseList = new CourseList();
    }

    @Override
    public void execute(int n) {
        switch (n) {
            case 1 -> courseList.display();
            case 2 -> searchCourse();
            case 3 -> courseList.sort();
            case 4 -> delete();
            case 5 -> adviseCourse();
            case 6 -> courseList.registerCourse();
            case 7 -> summarizeCourse();
            default -> System.exit(0);
        }
    }

    public void searchCourse() {
        String name = Validation.getString("Nhập tên khóa học cần tìm: ");
        ListView.listAll(courseList.search(p-> p.getCourseName().equalsIgnoreCase(name)));
    }
    public void delete() {
        if (courseList.delete()){
            System.out.println("Delete successfully!");
        } else {
            System.out.println("Delete failed!");
        }
    }
    public void adviseCourse() {
        GeminiClient client = new GeminiClient();
        Recommendation service = new Recommendation(courseList, client);

        // 2. Khởi tạo View
        RecommendationView view = new RecommendationView();

        // 3. Khởi tạo Controller và inject Model, View vào
        RecommendationController controller = new RecommendationController(service, view);

        // 4. Bắt đầu luồng ứng dụng
        controller.start();
    }

    public void summarizeCourse() {
        String courseId = Validation.getString("Nhập mã khóa học cần tóm tắt: ");
        GeminiClient client = new GeminiClient();
        Recommendation service = new Recommendation(courseList, client);
        RecommendationView view = new RecommendationView();
        try {
            var course = courseList.getCourseById(courseId);
            if (course == null) {
                System.out.println("Không tìm thấy khóa học với mã: " + courseId);
                return;
            }
            StringBuilder description = new StringBuilder();
            description.append("Khóa học: ").append(course.getCourseName()).append("\n");
            description.append("Chủ đề: ").append(course.getTopic()).append("\n");
            description.append("Cấp độ: ").append(course.getDifficulty()).append("\n");
            String summary = client.summarizeCourseDescription(description.toString());
            view.displayCourseSummary(course, summary);
        } catch (Exception e) {
            System.out.println("Lỗi khi tóm tắt khóa học: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Application("Quản lý khóa học", options).run();
    }
}
