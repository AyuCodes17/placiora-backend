package com.placiora.backend.service;

import com.placiora.backend.entity.Student;
import com.placiora.backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public Student registerStudent(Student student) {

        if (studentRepository.findByEmail(student.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        return studentRepository.save(student);
    }

    public Student loginStudent(String email, String password) {

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password!"));

        if (!student.getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password!");
        }

        return student;
    }

    // Get the latest student data from MySQL
    public Student getStudentById(Long id) {

        return studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found!"));
    }

    public Student updateStudent(Long id, Student updatedStudent) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found!"));

        student.setFullName(updatedStudent.getFullName());
        student.setPhone(updatedStudent.getPhone());
        student.setDepartment(updatedStudent.getDepartment());
        student.setSemester(updatedStudent.getSemester());
        student.setCgpa(updatedStudent.getCgpa());

        student.setLocation(updatedStudent.getLocation());
        student.setPortfolioUrl(updatedStudent.getPortfolioUrl());
        student.setGithubUrl(updatedStudent.getGithubUrl());
        student.setLinkedinUrl(updatedStudent.getLinkedinUrl());
        student.setBio(updatedStudent.getBio());

        return studentRepository.save(student);
    }
}