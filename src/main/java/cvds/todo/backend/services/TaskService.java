package cvds.todo.backend.services;


import cvds.todo.backend.exceptions.AppException;
import cvds.todo.backend.exceptions.TaskException;
import cvds.todo.backend.interfeces.TaskRepository;
import cvds.todo.backend.interfeces.TasksService;
import cvds.todo.backend.model.Difficulty;
import cvds.todo.backend.model.TaskModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class TaskService implements TasksService {

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public List<TaskModel> getAllTasks() throws AppException {
        return taskRepository.findAll();
    }

    @Override
    public TaskModel getTaskById(String id) throws AppException {
        Optional<TaskModel> result = taskRepository.findById(id);

        if (result.isPresent()) {
            return result.get();
        }

        throw new TaskException.TaskNotFoundException(id);
    }

    @Override
    public TaskModel createTask(TaskModel task) throws AppException {
        this.isValidTask(task);

        String id = UUID.randomUUID().toString();
        task.setId(id);

        return taskRepository.insert(task);
    }

    @Override
    public TaskModel updateTask(String id, TaskModel task) throws AppException {
        this.isValidTask(task);

        Optional<TaskModel> existingTask = taskRepository.findById(id);

        if (existingTask.isPresent()) {
            TaskModel taskToUpdate = existingTask.get();

            taskToUpdate.setName(task.getName() == null ? taskToUpdate.getName() : task.getName());
            taskToUpdate.setDescription(task.getDescription() == null ? taskToUpdate.getDescription() : task.getDescription());
            taskToUpdate.setDone(task.isDone());

            this.taskRepository.save(taskToUpdate);

            return taskToUpdate;
        }

        throw new TaskException.TaskNotFoundException(id);
    }

    @Override
    public TaskModel deleteTask(String id) throws AppException {
        Optional<TaskModel> optionalTask = taskRepository.findById(id);

        if (optionalTask.isPresent()) {
            TaskModel taskToDelete = optionalTask.get();
            taskRepository.deleteById(id);
            return taskToDelete;
        }

        throw new TaskException.TaskNotFoundException(id);
    }

    @Override
    public List<TaskModel> generateExamples() throws AppException {
        Random random = new Random();
        int numberOfTasks = random.nextInt(901) + 100;
        List<TaskModel> tasks = new ArrayList<>();

        for (int i = 0; i < numberOfTasks; i++) {
            TaskModel task = new TaskModel();
            task.setName("Task: " + (i + 1));
            task.setDescription("Description for Task " + (i + 1));
            task.setPriority(random.nextInt(5) + 1);
            task.setDifficult(String.valueOf(Difficulty.values()[random.nextInt(Difficulty.values().length)]));
            task.setDone(random.nextBoolean());
            tasks.add(task);
            this.createTask(task);
        }
        return tasks;
    }

    public List<TaskModel> deleteAllTasks() throws AppException {
        List<TaskModel> tasksDeleted = this.getAllTasks();
        for (TaskModel task : tasksDeleted) {
            this.deleteTask(task.getId());
        }
        return tasksDeleted;
    }

    public void isValidTask(TaskModel task) throws AppException {
        if (task.getName() == null) {
            throw new TaskException.TaskInvalidValueException("Task name is required");
        }
        if (task.getDifficult() != null) {
            try {
                Difficulty.valueOf(task.getDifficult().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new TaskException.TaskInvalidValueException("Task difficult is invalid");
            }
        }
    }
}
