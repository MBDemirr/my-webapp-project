package com.example.servlet;

import com.example.dao.TaskDAO;
import com.example.model.Task;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/tasks/*")
public class TasksServlet extends HttpServlet {
	private TaskDAO taskDAO;
	private Gson gson;

	@Override
	public void init() throws ServletException {
		// Use system temp directory for the database file for reliability
		String tempDir = System.getProperty("java.io.tmpdir");
		String dbPath = tempDir + File.separator + "my-webapp-todo.db";
		
		System.out.println("[TasksServlet] Database path: " + dbPath);
		getServletContext().log("[TasksServlet] Database path: " + dbPath);
		
		taskDAO = new TaskDAO(dbPath);
		gson = new Gson();
		
		System.out.println("[TasksServlet] Initialization complete");
		getServletContext().log("[TasksServlet] Initialization complete");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			List<Task> tasks = taskDAO.getAllTasks();
			out.print(tasksToJson(tasks));
		} else {
			try {
				int id = Integer.parseInt(pathInfo.substring(1));
				Task task = taskDAO.getTask(id);
				if (task != null) {
					out.print(taskToJson(task));
				} else {
					resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
					out.print("{\"error\":\"Task not found\"}");
				}
			} catch (NumberFormatException e) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print("{\"error\":\"Invalid task ID\"}");
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		Task task = parseTaskFromRequest(req);
		if (task == null) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().print("{\"error\":\"Invalid request: missing or invalid task data (title is required)\"}");
			return;
		}
		getServletContext().log("[TasksServlet] attempting to add task: " + task);
		if (taskDAO.addTask(task)) {
			getServletContext().log("[TasksServlet] task added successfully");
			resp.setStatus(HttpServletResponse.SC_CREATED);
			resp.getWriter().print(taskToJson(task));
		} else {
			getServletContext().log("[TasksServlet] taskDAO.addTask returned false");
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().print("{\"error\":\"Failed to create task - database error\"}");
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		String pathInfo = req.getPathInfo();
		if (pathInfo != null && pathInfo.length() > 1) {
			try {
				int id = Integer.parseInt(pathInfo.substring(1));
				Task task = parseTaskFromRequest(req);
				if (task != null) {
					task.setId(id);
					if (taskDAO.updateTask(task)) {
						resp.setStatus(HttpServletResponse.SC_OK);
						resp.getWriter().print(taskToJson(task));
					} else {
						resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
						resp.getWriter().print("{\"error\":\"Task not found\"}");
					}
				} else {
					resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					resp.getWriter().print("{\"error\":\"Invalid request: missing or invalid task data (title is required)\"}");
				}
			} catch (NumberFormatException e) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().print("{\"error\":\"Invalid task ID\"}");
			}
		} else {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().print("{\"error\":\"Task ID is required\"}");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		String pathInfo = req.getPathInfo();
		if (pathInfo != null && pathInfo.length() > 1) {
			try {
				int id = Integer.parseInt(pathInfo.substring(1));
				if (taskDAO.deleteTask(id)) {
					resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
				} else {
					resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
					resp.getWriter().print("{\"error\":\"Task not found\"}");
				}
			} catch (NumberFormatException e) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().print("{\"error\":\"Invalid task ID\"}");
			}
		} else {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().print("{\"error\":\"Task ID is required\"}");
		}
	}

	private Task parseTaskFromRequest(HttpServletRequest req) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = req.getReader();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		String body = sb.toString();
		getServletContext().log("[TasksServlet] received body: " + body);
		
		if (body == null || body.trim().isEmpty()) {
			getServletContext().log("[TasksServlet] request body is empty");
			return null;
		}
		
		try {
			Task t = gson.fromJson(body, Task.class);
			// ensure required field
			if (t == null) {
				getServletContext().log("[TasksServlet] failed to parse task - gson returned null");
				return null;
			}
			if (t.getTitle() == null || t.getTitle().trim().isEmpty()) {
				getServletContext().log("[TasksServlet] task title is null or empty");
				return null;
			}
			return t;
		} catch (JsonSyntaxException e) {
			getServletContext().log("[TasksServlet] JSON parsing error: " + e.getMessage(), e);
			return null;
		}
	}

	// older regex-based parsing removed; gson used instead

	private String taskToJson(Task task) {
		return String.format("{\"id\":%d,\"title\":\"%s\",\"completed\":%s}", task.getId(), task.getTitle(), task.isCompleted());
	}

	private String tasksToJson(List<Task> tasks) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < tasks.size(); i++) {
			sb.append(taskToJson(tasks.get(i)));
			if (i < tasks.size() - 1) sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
}