package com.example.lib;

import java.util.ArrayList;
import java.util.List;

public class MyClass {
    public static void main(String[] args) {
        School s = new School();
        School.Student s1 = s.createStudent("abc");
        System.out.println(s1.getPrintString());

        final List<String> tabItemModels = new ArrayList<>();
        tabItemModels.add("23");
//        tabItemModels = new ArrayList<String>();
    }
}