package view;

import java.util.ArrayList;
import java.util.Scanner;

public abstract class Menu<T> {

    protected String title;
    protected ArrayList<T> mChon;

    public Menu() {
    }

    public Menu(String td, String[] mc) {
        title = td;
        mChon = new ArrayList<>();
        for (String s : mc) {
            mChon.add((T) s);
        }
    }
//-------------------------------------------

    public void display() {
        System.out.println(title);
        System.out.println("--------------------------------");
        for (int i = 0; i < mChon.size(); i++) {
            System.out.println((i + 1) + "." + mChon.get(i));
        }
        System.out.println("Enter another option to exit the menu");
        System.out.println("--------------------------------");

    }
//-------------------------------------------

    public int getSelected() {
        display();
        Scanner sc = new Scanner(System.in);
        int result;
        System.out.print("Enter selection..");
        while (true)
        try {
            result = Integer.parseInt(sc.nextLine());
            return result;
        } catch (NumberFormatException e) {
            System.out.println("Enter integer number.");
            System.out.println("Enter again");
        }
    }
//-------------------------------------------

    public abstract void execute(int n);
//-------------------------------------------

    public void run() {
        while (true) {
            int n = getSelected();
            if (n <= mChon.size()) {
                execute(n);
            } else {
                break;
            }
        }
    }
//-------------------------------------------    
}
