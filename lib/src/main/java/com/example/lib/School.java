package com.example.lib;

public class School {
    String schoolName = "123";

    public Student createStudent(String name) {
        return new Student(name);
    }

    public class Student {
        private String name;

        public Student(String name) {
            this.name = name;
        }

        String getPrintString() {
//            return "SchoolName: " + schoolName + " StudentName: " + this.name;
            return "SchoolName: " + School.this.schoolName + " StudentName: " + this.name;

        }
    }

}
