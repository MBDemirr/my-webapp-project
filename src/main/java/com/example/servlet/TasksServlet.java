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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/tasks/*")
public class TasksServlet extends HttpServlet {
	private TaskDAO taskDAO;
	private Gson gson;

	@Override
	public void init() throws ServletException {
		// determine location for SQLite file inside WEB-INF so it's writable
		String dbPath = getServletContext().getRealPath("/WEB-INF/todo.db");
		getServletContext().log("[TasksServlet] DB path: '" + dbPath + "' length=" + (dbPath==null?0:dbPath.length()));
		if (dbPath != null) {
			StringBuilder codePoints = new StringBuilder();
			for (int i = 0; i < dbPath.length(); i++) {
				codePoints.append((int) dbPath.charAt(i)).append(" ");
			}
			getServletContext().log("[TasksServlet] DB path codepoints: " + codePoints);
		}
		taskDAO = new TaskDAO(dbPath);
		// check whether SQLite file exists now that DAO has connected
		java.io.File dbFile = new java.io.File(dbPath);
		getServletContext().log("[TasksServlet] DB file exists after DAO init: " + dbFile.exists() + " (" + dbFile.getAbsolutePath() + ")");
		// try to create the file manually to test permissions
		try {
			boolean created = dbFile.createNewFile();
			getServletContext().log("[TasksServlet] manual createNewFile returned: " + created);
		} catch (java.io.IOException e) {
			getServletContext().log("[TasksServlet] manual file creation failed", e);
		}
		gson = new Gson();
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
				}
			} catch (NumberFormatException e) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Task task = parseTaskFromRequest(req);
		if (task != null && taskDAO.addTask(task)) {
			resp.setStatus(HttpServletResponse.SC_CREATED);
		} else {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo != null && pathInfo.length() > 1) {
			try {
				int id = Integer.parseInt(pathInfo.substring(1));
				Task task = parseTaskFromRequest(req);
				if (task != null) {
					task.setId(id);
					if (taskDAO.updateTask(task)) {
						resp.setStatus(HttpServletResponse.SC_OK);
					} else {
						resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
					}
				} else {
					resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
			} catch (NumberFormatException e) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo != null && pathInfo.length() > 1) {
			try {
				int id = Integer.parseInt(pathInfo.substring(1));
				if (taskDAO.deleteTask(id)) {
					resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
				} else {
					resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}
			} catch (NumberFormatException e) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
		try {
			Task t = gson.fromJson(body, Task.class);
			// ensure required field
			if (t != null && t.getTitle() != null) {
				return t;
			}
		} catch (JsonSyntaxException e) {
			// ignore, will return null
		}
		return null;
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