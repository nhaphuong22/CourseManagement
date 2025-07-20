package model;

public class Course {
    private final String courseId;
    private final String courseName;
    private final String topic;
    private final String difficulty;
    private final String description;

    public Course(String courseId, String courseName, String topic, String difficulty) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.topic = topic;
        this.difficulty = difficulty;
        this.description = "";
    }

    public Course(String courseId, String courseName, String topic, String difficulty, String description) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.topic = topic;
        this.difficulty = difficulty;
        this.description = description;
    }

    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getTopic() { return topic; }
    public String getDifficulty() { return difficulty; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("%-40s | %-10s | %-20s | %-10s \n",
                courseName, courseId, topic, difficulty);
    }
}
