package com.example;

import com.example.dao.TaskDAO;
import com.example.model.Task;

public class TestDB {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java com.example.TestDB <path-to-db>");
            System.exit(1);
        }
        String path = args[0];
        System.out.println("Creating DAO with path: '" + path + "'");
        TaskDAO dao = new TaskDAO(path);
        System.out.println("DAO created. Adding test task...");
        Task t = new Task(0, "from TestDB", false);
        boolean added = dao.addTask(t);
        System.out.println("addTask returned: " + added);
        System.out.println("Checking count: " + dao.getAllTasks().size());
    }
}