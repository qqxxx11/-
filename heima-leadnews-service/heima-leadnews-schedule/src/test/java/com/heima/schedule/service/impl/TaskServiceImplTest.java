package com.heima.schedule.service.impl;

import com.heima.model.schedule.dto.Task;
import com.heima.schedule.ScheduleApplication;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.*;
@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class TaskServiceImplTest {
    @Autowired
    private TaskService taskService;
    @Test
    public void addTask() {
        Task task = new Task();
        task.setPriority(50);
        task.setTaskType(100);
        task.setExecuteTime(new Date().getTime());
        task.setParameters("task test".getBytes());
        long l = taskService.addTask(task);
        System.out.println(l);
    }
}