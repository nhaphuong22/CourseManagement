package model;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;

public class GeminiClient {
    // KHÓA API: Đây là khóa xác thực để truy cập Google Gemini API.
    // LƯU Ý: Việc để khóa API trực tiếp trong mã nguồn là không an toàn cho môi trường sản phẩm.
    // Nên sử dụng biến môi trường hoặc các dịch vụ quản lý bí mật.
    private static final String API_KEY = "AIzaSyDNFCSOm9cMi0SF1rPttHhsrchio42Bpcg";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Gửi yêu cầu đến API của Gemini để tạo một lộ trình học tập dựa trên mục tiêu của người dùng và các khóa học có sẵn.
     *
     * @param userGoal Mục tiêu học tập của sinh viên (ví dụ: "trở thành nhà phát triển web full-stack").
     * @param availableCourses Danh sách các khóa học hiện có để AI lựa chọn.
     * @return Một {@code ArrayList<String>} chứa các ID của khóa học được đề xuất theo đúng thứ tự.
     * @throws IOException Ném ra khi có lỗi mạng hoặc API trả về mã trạng thái lỗi.
     * @throws InterruptedException Ném ra nếu luồng gửi yêu cầu bị gián đoạn.
     */
    public ArrayList<String> fetchLearningPath(String userGoal, ArrayList<Course> availableCourses) throws IOException, InterruptedException {
        // Xây dựng prompt và payload JSON cho yêu cầu
        String prompt = buildLearningPathPrompt(userGoal, availableCourses);
        String jsonPayload = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}]}";

        // Tạo yêu cầu HTTP POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Gửi yêu cầu và nhận phản hồi
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Xử lý phản hồi
        if (response.statusCode() == 200) {
            return parseResponse(response.body());
        } else {
            throw new IOException("Lỗi khi gọi API: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Gửi yêu cầu đến API của Gemini để tóm tắt mô tả của một khóa học.
     *
     * @param courseDescription Mô tả chi tiết của khóa học cần được tóm tắt.
     * @return Một chuỗi {@code String} chứa nội dung tóm tắt của khóa học.
     * @throws IOException Ném ra khi có lỗi mạng hoặc API trả về mã trạng thái lỗi.
     * @throws InterruptedException Ném ra nếu luồng gửi yêu cầu bị gián đoạn.
     */
    public String summarizeCourseDescription(String courseDescription) throws IOException, InterruptedException {
        // Xây dựng prompt và payload JSON cho yêu cầu
        String prompt = buildSummaryPrompt(courseDescription);
        String jsonPayload = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}]}";

        // Tạo yêu cầu HTTP POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Gửi yêu cầu và nhận phản hồi
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Xử lý phản hồi
        if (response.statusCode() == 200) {
            return parseSummaryResponse(response.body());
        } else {
            throw new IOException("Lỗi khi gọi API: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * (Phương thức trợ giúp) Xây dựng chuỗi prompt chi tiết để yêu cầu Gemini tạo lộ trình học tập.
     * Prompt này cung cấp bối cảnh, mục tiêu của sinh viên, danh sách khóa học và định dạng đầu ra mong muốn.
     *
     * @param userGoal Mục tiêu của sinh viên.
     * @param availableCourses Danh sách các khóa học có sẵn.
     * @return Một chuỗi {@code String} là prompt hoàn chỉnh.
     */
    private String buildLearningPathPrompt(String userGoal, ArrayList<Course> availableCourses) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Bạn là một chuyên gia tư vấn học thuật. Một sinh viên muốn có một lộ trình học tập.\n\n");
        promptBuilder.append("MỤC TIÊU CỦA SINH VIÊN: \"").append(userGoal).append("\"\n\n");
        promptBuilder.append("Dưới đây là danh sách các khóa học có sẵn:\n");
        availableCourses.forEach(course -> {
            // đưa danh sách khóa học có trong danh sách vào prompt và gửi lên gemini
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

    /**
     * (Phương thức trợ giúp) Xây dựng chuỗi prompt để yêu cầu Gemini tóm tắt mô tả khóa học.
     *
     * @param courseDescription Mô tả khóa học cần tóm tắt.
     * @return Một chuỗi {@code String} là prompt hoàn chỉnh cho việc tóm tắt.
     */
    private String buildSummaryPrompt(String courseDescription) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Bạn là một trợ lý AI. Hãy tóm tắt ngắn gọn nội dung khóa học sau cho sinh viên dễ hiểu (tối đa 3 câu):\n");
        promptBuilder.append(courseDescription);
        return promptBuilder.toString();
    }

    /**
     * (Phương thức trợ giúp) Phân tích chuỗi JSON trả về từ API để trích xuất danh sách ID khóa học.
     * Phương thức này tìm kiếm phần "text" trong phản hồi và tách chuỗi kết quả bằng dấu phẩy.
     *
     * @param responseBody Nội dung phản hồi (dạng chuỗi JSON) từ API.
     * @return Một {@code ArrayList<String>} chứa các ID khóa học. Trả về danh sách rỗng nếu có lỗi.
     */
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

    /**
     * (Phương thức trợ giúp) Phân tích chuỗi JSON trả về từ API để trích xuất nội dung tóm tắt.
     *
     * @param responseBody Nội dung phản hồi (dạng chuỗi JSON) từ API.
     * @return Một chuỗi {@code String} chứa nội dung tóm tắt đã được làm sạch.
     */
    private String parseSummaryResponse(String responseBody) {
        try {
            // Tìm kiếm thẻ "text" trong chuỗi JSON để bắt đầu trích xuất
            String key = "\"text\": \"";
            int start = responseBody.indexOf(key);
            if (start == -1) {
                throw new RuntimeException("Không tìm thấy trường 'text' trong phản hồi của API.");
            }
            start += key.length();

            // Tìm dấu ngoặc kép kết thúc của nội dung
            int end = responseBody.indexOf("\"", start);
            if (end == -1) {
                throw new RuntimeException("Không tìm thấy dấu ngoặc kép đóng cho trường 'text'.");
            }

            // Trích xuất và làm sạch nội dung tóm tắt
            String content = responseBody.substring(start, end);
            content = content.replace("\\n", " ").trim(); // Thay thế ký tự xuống dòng bằng khoảng trắng
            return content;
        } catch (Exception e) {
            System.err.println("Không thể phân tích phản hồi từ API. Nội dung phản hồi: " + responseBody);
            System.err.println("Lỗi chi tiết: " + e.getMessage());
            return "Không thể tóm tắt nội dung.";
        }
    }
}