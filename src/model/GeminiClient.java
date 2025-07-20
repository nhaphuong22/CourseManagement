package model;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;

public class GeminiClient {
    private static final String API_KEY = "AIzaSyAqVYh0lUqa8BOSQCbusgOm7ZWWz4AEOo4";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    private final HttpClient client = HttpClient.newHttpClient();

    public ArrayList<String> fetchLearningPath(String userGoal, ArrayList<Course> availableCourses) throws IOException, InterruptedException {

        String prompt = buildLearningPathPrompt(userGoal, availableCourses);
        String jsonPayload = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseResponse(response.body());
        } else {
            throw new IOException("Lỗi khi gọi API: " + response.statusCode() + " - " + response.body());
        }
    }

    public String summarizeCourseDescription(String courseDescription) throws IOException, InterruptedException {
        String prompt = buildSummaryPrompt(courseDescription);
        String jsonPayload = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseSummaryResponse(response.body());
        } else {
            throw new IOException("Lỗi khi gọi API: " + response.statusCode() + " - " + response.body());
        }
    }

    private String buildLearningPathPrompt(String userGoal, ArrayList<Course> availableCourses) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Bạn là một chuyên gia tư vấn học thuật. Một sinh viên muốn có một lộ trình học tập.\n\n");
        promptBuilder.append("MỤC TIÊU CỦA SINH VIÊN: \"").append(userGoal).append("\"\n\n");
        promptBuilder.append("Dưới đây là danh sách các khóa học có sẵn:\n");
        availableCourses.forEach(course -> {
            promptBuilder.append("- ID: ").append(course.getCourseId())
                    .append(", Tên: ").append(course.getCourseName())
                    .append(", Chủ đề: ").append(course.getTopic())
                    .append(", Cấp độ: ").append(course.getDifficulty())
                    .append("\n");
        });
        promptBuilder.append("\nDựa trên mục tiêu của sinh viên và các khóa học có sẵn, hãy tạo ra một lộ trình học tập hợp lý gồm 5 bước.\n");
        promptBuilder.append("Hãy sắp xếp các khóa học theo thứ tự logic, bắt đầu từ các khóa nền tảng (cơ bản) rồi mới đến các khóa nâng cao hơn.\n");
        promptBuilder.append("\nQUAN TRỌNG: Chỉ trả về một chuỗi chứa ID của các khóa học trong lộ trình, phân tách nhau bởi dấu phẩy (ví dụ: CS101,AI101,AI201). Không thêm bất kỳ văn bản giải thích nào khác.");
        return promptBuilder.toString();
    }

    private String buildSummaryPrompt(String courseDescription) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Bạn là một trợ lý AI. Hãy tóm tắt ngắn gọn nội dung khóa học sau cho sinh viên dễ hiểu (tối đa 3 câu):\n");
        promptBuilder.append(courseDescription);
        return promptBuilder.toString();
    }

    private ArrayList<String> parseResponse(String responseBody) {
        try {
            // SỬA LỖI: Sử dụng indexOf để phân tích response một cách đáng tin cậy hơn.
            String key = "\"text\": \"";
            int start = responseBody.indexOf(key);
            if (start == -1) {
                throw new RuntimeException("Không tìm thấy trường 'text' trong phản hồi của API.");
            }

            // Di chuyển con trỏ bắt đầu đến sau chuỗi key.
            start += key.length();

            // Tìm dấu ngoặc kép đóng.
            int end = responseBody.indexOf("\"", start);
            if (end == -1) {
                throw new RuntimeException("Không tìm thấy dấu ngoặc kép đóng cho trường 'text'.");
            }

            // Trích xuất nội dung.
            String content = responseBody.substring(start, end);

            // Dọn dẹp nội dung: xóa ký tự xuống dòng và các khoảng trắng thừa.
            content = content.replace("\\n", "").trim();

            if (content.isEmpty()) {
                return new ArrayList<>();
            }

            // Tách các ID được phân cách bằng dấu phẩy.
            return new ArrayList<> (Arrays.asList(content.split(",\\s*")));

        } catch (Exception e) {
            System.err.println("Không thể phân tích phản hồi từ API. Nội dung phản hồi: " + responseBody);
            System.err.println("Lỗi chi tiết: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String parseSummaryResponse(String responseBody) {
        try {
            String key = "\"text\": \"";
            int start = responseBody.indexOf(key);
            if (start == -1) {
                throw new RuntimeException("Không tìm thấy trường 'text' trong phản hồi của API.");
            }
            start += key.length();
            StringBuilder content = new StringBuilder();
            boolean escape = false;
            for (int i = start; i < responseBody.length(); i++) {
                char c = responseBody.charAt(i);
                if (escape) {
                    // Xử lý ký tự escape
                    if (c == 'n') content.append('\n');
                    else content.append(c);
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    break;
                } else {
                    content.append(c);
                }
            }
            String result = content.toString().replace("\\n", " ").trim();
            return result;
        } catch (Exception e) {
            System.err.println("Không thể phân tích phản hồi từ API. Nội dung phản hồi: " + responseBody);
            System.err.println("Lỗi chi tiết: " + e.getMessage());
            return "Không thể tóm tắt nội dung.";
        }
    }
}
