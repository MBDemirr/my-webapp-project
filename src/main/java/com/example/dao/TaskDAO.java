package com.example.dao;

import com.example.model.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
	private final String dbUrl;

	public TaskDAO(String filePath) {
		// Load SQLite driver
		try {
			Class.forName("org.sqlite.JDBC");
			System.out.println("[TaskDAO] SQLite JDBC driver loaded successfully");
		} catch (ClassNotFoundException e) {
			System.err.println("[TaskDAO] Failed to load SQLite JDBC driver: " + e.getMessage());
			e.printStackTrace(System.err);
		}
		
		// make sure parent directories exist and file is created so SQLite can open it
		java.io.File f = new java.io.File(filePath);
		java.io.File parent = f.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		try {
			if (!f.exists()) {
				f.createNewFile();
			}
		} catch (java.io.IOException io) {
			io.printStackTrace();
		}
		this.dbUrl = "jdbc:sqlite:" + filePath;
		System.out.println("[TaskDAO] connecting to " + dbUrl + " (file exists=" + f.exists() + ")");
		try (Connection conn = DriverManager.getConnection(dbUrl)) {
			String sql = "CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, completed INTEGER)";
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sql);
				System.out.println("[TaskDAO] table created or already exists");
			}
		} catch (SQLException e) {
			System.err.println("[TaskDAO] Failed to initialize database: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	public List<Task> getAllTasks() {
		List<Task> tasks = new ArrayList<>();
		String sql = "SELECT * FROM tasks";
		try (Connection conn = DriverManager.getConnection(dbUrl);
			 Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				Task task = new Task(
						rs.getInt("id"),
						rs.getString("title"),
						rs.getInt("completed") == 1
				);
				tasks.add(task);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tasks;
	}

	public Task getTask(int id) {
		String sql = "SELECT * FROM tasks WHERE id = ?";
		try (Connection conn = DriverManager.getConnection(dbUrl);
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return new Task(
							rs.getInt("id"),
							rs.getString("title"),
							rs.getInt("completed") == 1
					);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean addTask(Task task) {
		String sql = "INSERT INTO tasks (title, completed) VALUES (?, ?)";
		try (Connection conn = DriverManager.getConnection(dbUrl);
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, task.getTitle());
			pstmt.setInt(2, task.isCompleted() ? 1 : 0);
			int rowsAffected = pstmt.executeUpdate();
			System.out.println("[TaskDAO] addTask: inserted " + rowsAffected + " rows for task: " + task);
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("[TaskDAO] addTask failed: " + e.getMessage());
			e.printStackTrace(System.err);
		}
		return false;
	}

	public boolean updateTask(Task task) {
		String sql = "UPDATE tasks SET title = ?, completed = ? WHERE id = ?";
		try (Connection conn = DriverManager.getConnection(dbUrl);
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, task.getTitle());
			pstmt.setInt(2, task.isCompleted() ? 1 : 0);
			pstmt.setInt(3, task.getId());
			int rowsAffected = pstmt.executeUpdate();
			System.out.println("[TaskDAO] updateTask: updated " + rowsAffected + " rows for task: " + task);
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("[TaskDAO] updateTask failed: " + e.getMessage());
			e.printStackTrace(System.err);
		}
		return false;
	}

	public boolean deleteTask(int id) {
		String sql = "DELETE FROM tasks WHERE id = ?";
		try (Connection conn = DriverManager.getConnection(dbUrl);
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}