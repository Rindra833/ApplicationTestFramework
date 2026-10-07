package test;

import java.util.Arrays;
import java.util.List;

public class EmployeeService {

    public List<String> findAll() {
        return Arrays.asList("Jean", "Marie", "Paul", "Aina");
    }

    public List<String> findAllForPost() {
        return Arrays.asList("Niry", "Hery", "Lova");
    }
}