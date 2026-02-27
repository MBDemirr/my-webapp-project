(function(){
  const CONTEXT = window.CONTEXT_PATH || '';
  const API_BASE = CONTEXT + '/api/tasks';
  const tasksList = document.getElementById('tasks');
  const taskForm = document.getElementById('task-form');
  const newTaskInput = document.getElementById('new-task');

  function fetchTasks(){
    fetch(API_BASE)
      .then(r => r.json())
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
        updateTask(Object.assign({}, task, {completed: checkbox.checked}));
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
        updateTask(Object.assign({}, task, {title: newTitle}));
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
