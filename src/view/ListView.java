/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package view;

import model.Course;

import java.util.ArrayList;



public class ListView {
        public ListView() {
        }

        public static void listAll(ArrayList<Course> list){
            int total = list.size();
            if (total == 0){
                System.out.println("Sorry. Nothing to print!");
                return;
            }
            System.out.println("----------------------------------------------------------------------------------------------");
            System.out.printf("%-40s | %-10s | %-20s | %-10s \n",
                    "courseName","courseId", "topic", "difficulty");

            System.out.println("----------------------------------------------------------------------------------------------");
            list.forEach(System.out::println);
            System.out.println("----------------------------------------------------------------------------------------------");
            System.out.println("Total: "+total);
        }
}
