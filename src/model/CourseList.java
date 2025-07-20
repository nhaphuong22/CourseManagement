package model;

import view.ListView;
import view.Validation;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.function.Predicate;

public class CourseList {
    private final ArrayList<Course> allCourses = new ArrayList<>();

    public CourseList() {
        loadCoursesFromFile("course.txt");
    }

    private void loadCoursesFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    allCourses.add(new Course(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Course> getAllCourses() {
        return allCourses;
    }

    public Course getCourseById(String id) {
        return allCourses.stream()
                .filter(c -> c.getCourseId().equals(id.trim()))
                .findFirst().orElse(null);
    }

    public void display() {
        ListView.listAll(allCourses);
    }
    public ArrayList<Course> search(Predicate<Course> predicate){
        ArrayList<Course> list = new ArrayList<>();
        for (Course c : allCourses) {
            if (predicate.test(c)) {
                list.add(c);
            }
        }
        return list;
    }
    public void sort() {
        allCourses.sort(Comparator.comparing(Course::getCourseName));
        display();
    }
    public boolean delete() {
        String id = Validation.getString("Enter course ID to delete: ");
        return allCourses.removeIf(c -> c.getCourseId().equals(id));
    }
}
