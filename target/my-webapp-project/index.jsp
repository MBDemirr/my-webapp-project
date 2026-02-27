
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Modern Hello</title>
    <style>
        /* Reset and Base Styles */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #2d3436;
        }

        /* Card Container */
        .card {
            background: rgba(255, 255, 255, 0.95);
            padding: 3rem 5rem;
            border-radius: 24px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
            text-align: center;
            transition: transform 0.3s ease;
        }

        .card:hover {
            transform: translateY(-5px);
        }

        /* Typography */
        h2 {
            font-size: 3rem;
            font-weight: 800;
            background: linear-gradient(to right, #667eea, #764ba2);
            background-clip: text;
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            letter-spacing: -1px;
        }

        p {
            margin-top: 10px;
            color: #636e72;
            font-size: 1.1rem;
        }

        .counter {
            margin-top: 1.5rem;
            font-size: 1.25rem;
            font-weight: 600;
            color: #2d3436;
        }

        .increment-btn {
            margin-top: 1rem;
            padding: 0.7rem 1.4rem;
            border: none;
            border-radius: 12px;
            background: #667eea;
            color: white;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.2s ease;
        }

        .increment-btn:hover {
            background: #5a6fd6;
        }
    </style>
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
</head>
<body>

    <div class="card">
        <h2>TODO List</h2>
        <p>Add and remove tasks using the backend API.</p>
        <form id="task-form">
            <input type="text" id="new-task" placeholder="New task title" required />
            <button type="submit" class="increment-btn">Add</button>
        </form>
        <ul id="tasks" style="list-style:none; padding:0; margin-top:1rem;"></ul>
    </div>

    <script>
        // expose context path to client JS
        window.CONTEXT_PATH = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/app.js"></script>

    <script>
        // Use JSP context path so client works under any deployment name
        window.CONTEXT_PATH = '${pageContext.request.contextPath}';
    </script>
    <script>
        (function(){
            const API_BASE = (window.CONTEXT_PATH || '') + '/api/tasks';
            const tasksList = document.getElementById('tasks');
            const taskForm = document.getElementById('task-form');
            const newTaskInput = document.getElementById('new-task');

            function fetchTasks(){
                fetch(API_BASE)
                    .then(r=>r.json())
                    .then(renderTasks)
                    .catch(console.error);
            }

            function renderTasks(tasks){
                tasksList.innerHTML = '';
                tasks.forEach(task => {
                    const li = document.createElement('li');
                    li.style.display = 'flex';
                    li.style.alignItems = 'center';
                    li.style.justifyContent = 'space-between';
                    li.style.padding = '0.5rem 0';

                    const left = document.createElement('div');
                    left.style.display = 'flex';
                    left.style.alignItems = 'center';
                    left.style.gap = '0.75rem';

                    const checkbox = document.createElement('input');
                    checkbox.type = 'checkbox';
                    checkbox.checked = !!task.completed;
                    checkbox.addEventListener('change', () => {
                        updateTask({...task, completed: checkbox.checked});
                    });

                    const titleSpan = document.createElement('span');
                    titleSpan.textContent = task.title;
                    titleSpan.style.minWidth = '200px';
                    if (task.completed) titleSpan.style.textDecoration = 'line-through';

                    left.appendChild(checkbox);
                    left.appendChild(titleSpan);

                    const actions = document.createElement('div');
                    actions.style.display = 'flex';
                    actions.style.gap = '0.5rem';

                    const editBtn = document.createElement('button');
                    editBtn.className = 'icon-btn';
                    editBtn.title = 'Edit';
                    editBtn.innerHTML = '<i class="bi bi-pencil"></i>';
                    editBtn.addEventListener('click', () => startEdit(titleSpan, task));

                    const delBtn = document.createElement('button');
                    delBtn.className = 'icon-btn';
                    delBtn.title = 'Delete';
                    delBtn.innerHTML = '<i class="bi bi-trash"></i>';
                    delBtn.addEventListener('click', () => deleteTask(task.id));

                    actions.appendChild(editBtn);
                    actions.appendChild(delBtn);

                    li.appendChild(left);
                    li.appendChild(actions);
                    tasksList.appendChild(li);
                });
            }

            function startEdit(titleSpan, task){
                const input = document.createElement('input');
                input.type = 'text';
                input.value = task.title;
                input.style.minWidth = '200px';
                titleSpan.replaceWith(input);
                input.focus();
                input.select();
                function finish(){
                    const newTitle = input.value.trim();
                    if (newTitle && newTitle !== task.title){
                        updateTask({...task, title: newTitle});
                    } else {
                        input.replaceWith(titleSpan);
                    }
                }
                input.addEventListener('blur', finish);
                input.addEventListener('keydown', (e)=>{ if (e.key==='Enter') { finish(); } if (e.key==='Escape'){ input.replaceWith(titleSpan); } });
            }

            function addTask(title){
                fetch(API_BASE, {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({title, completed:false})})
                    .then(res=>{ if(res.ok) fetchTasks(); else console.error('Add failed', res.status); })
                    .catch(console.error);
            }

            function updateTask(task){
                fetch(`${API_BASE}/${task.id}`, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(task)})
                    .then(res=>{ if(res.ok) fetchTasks(); else console.error('Update failed', res.status); })
                    .catch(console.error);
            }

            function deleteTask(id){
                fetch(`${API_BASE}/${id}`, {method:'DELETE'})
                    .then(res=>{ if(res.ok) fetchTasks(); else console.error('Delete failed', res.status); })
                    .catch(console.error);
            }

            taskForm.addEventListener('submit', e=>{ e.preventDefault(); const t = newTaskInput.value.trim(); if(t){ addTask(t); newTaskInput.value=''; } });

            fetchTasks();
        })();
    </script>

</body>
</html>