package com.camunda.demo1;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class CamundaDemo1ApplicationTests {


    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    ProcessEngine processEngine;

    /**
     * 流程定义部署
     */
    @Test
    void deploy() {
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("BPMN/holiday.bpmn")
                .deploy();
        //部署Id
        System.out.println(deploy.getName());//84c0a64c-cc1c-11ea-85a7-98fa9b4e8fcb
    }

    /**
     * 开启一条流程，并给用户任务的 assignee 赋值
     */
    @Test
    public void startProcessInstanceWithAssignee(){
        Map<String,Object> map = new HashMap<>();
        map.put("employee","zhangsan");
        map.put("deptment","lisi");
        map.put("personal","wangwu");
        ProcessInstance holiday = runtimeService.startProcessInstanceByKey("Process_07aip45", map);
        //流程实例Id
        System.out.println(holiday.getProcessInstanceId());//c3156a0d-cc28-11ea-adb8-98fa9b4e8fcb
    }

    @Test
    public void runProcinst(){
        Map<String,Object> params = new HashMap<>();
        params.put("employee","zhangsan");
        //params.put("leave",new Leave("NO00001","休假",new Date()));
        params.put("days",2);
        ProcessInstance holiday = runtimeService.startProcessInstanceByKey("Process_07aip45",params);
        System.out.println(holiday.getProcessDefinitionId());
        System.out.println(holiday.getId());
        System.out.println(holiday.getProcessInstanceId());
    }

    /**
     * 候选人拾取任务
     */
    @Test
    public void claimTask() {
        //查询候选人的代办任务
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("Process_07aip45")
                .taskCandidateUser("lisi")
                .singleResult();
        //打印任务实例Id和assignee
        System.out.println(task.getId());
        System.out.println(task.getAssignee());//null
        taskService.claim(task.getId(),"lisi");
    }

    /**
     * 流程任务执行
     */
    @Test
    public void taskComplete(){
        //已经变成了 lis 的个人任务
        Task task = taskService.createTaskQuery()
                .taskAssignee("zhangsan")
                .singleResult();
        taskService.complete(task.getId());
    }

    /**
     * 获取任务列表
     */
    @Test
    public void taskList(){
        List<Execution> executionList = runtimeService.createExecutionQuery().list();

        for (int i =0; i < executionList.size(); i++){
            List<Task> list = taskService.createTaskQuery().processInstanceId(executionList.get(i).getProcessInstanceId()).active().list();
            list.forEach(s-> {
                System.out.print(s.getId()+":"+s.getName());
                taskService.complete(s.getId());
            });
        }
    }

    /**
     * camunda用户
     */
    @Test
    public void userTest(){
        User user = processEngine.getIdentityService()
                .createUserQuery()
                .userId("admin")
                .singleResult();
        System.out.print(user.toString());
    }

    /**
     * 新增用户
     */
    @Test
    public void addUser(){
        UserEntity userEntity = new UserEntity();
        String id = new Date().getTime() + "";
        userEntity.setId(id);
        userEntity.setFirstName("san");
        userEntity.setLastName("zhang");
        userEntity.setEmail("18487101393@163.com");
        userEntity.setPassword("a123456");
        IdentityService identityService = processEngine.getIdentityService();

        identityService.saveUser(userEntity);

        System.out.print("id:"+id);
    }

    /**
     * 根据id查询用户
     */
    @Test
    public void getUserById(){
        IdentityService identityService = processEngine.getIdentityService();

        //User user = identityService.createUserQuery().userId("1633684145198").singleResult();

        UserQuery userQuery = identityService.createUserQuery();

         List<User> userList = identityService.createUserQuery().listPage(0, 10);

         for (int i = 0; i< userList.size(); i++){
             System.out.print("user:"+userList.get(i).getLastName());
             System.out.print("\n");

         }
    }

    /**
     * 删除用户
     */
    @Test
    public void delUser(){
        IdentityService identityService = processEngine.getIdentityService();

        identityService.deleteUser("1633684145198");
    }

    /**
     * 获取组下用户
     */
    @Test
    public void getUsersFromTenant(){
        IdentityService identityService = processEngine.getIdentityService();

        List<User> userList = identityService.createUserQuery().memberOfGroup("camunda-admin").listPage(0, 10);

        for (int i = 0; i< userList.size(); i++){
            System.out.print("user:"+userList.get(i).getId());
            System.out.print("\n");
        }
    }

    /**
     * 启动实例
     */
    @Test
    public void startProcess(){
        Map<String,Object> params = new HashMap<>();
        params.put("employee","zhangsan");
        params.put("days",2);
        String key = "Process_07aip45";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, params);
        processInstance.getId();
        System.out.println("id is : " + processInstance.getId());
        System.out.println("getBusinessKey is : " + processInstance.getBusinessKey());
        System.out.println("getRootProcessInstanceId is : " + processInstance.getRootProcessInstanceId());
        System.out.println("getProcessInstanceId is : " + processInstance.getProcessInstanceId());
    }

    /**
     * 获取任务列表
     */
    @Test
    public void getTasks() {
        List<Task> tasks = taskService.createTaskQuery().listPage(0, 5);;
        tasks.stream().forEach(task -> {
            System.out.println("taskId is : " + task.getId());
            ;
        });
    }

    /**
     * 分配任务
     */
    @Test
    public void assignTask() {
        String taskId = "1f3a1bff-2896-11ec-a3b9-085700f2914a";
        String assignee = "lisi";
        taskService.setAssignee(taskId, assignee);
    }

    /**
     * 获取任务分配人
     */
    @Test
    public void getTasksByAssignee() {
        String assignee =  "lisi";
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee).listPage(0, 5);
        tasks.stream().forEach(task -> {
            System.out.println("assignee is: " + task.getAssignee());
        });
    }

    /**
     * 完成任务
     */
    @Test
    public void completeTask() {
        String taskId = "1f3a1bff-2896-11ec-a3b9-085700f2914a";
        taskService.complete(taskId);
        ;
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    /**
     * 删除流程
     */
    @Test
    public void delProcess(){
        String processId = "";
        String reason = "";
        runtimeService.deleteProcessInstance(processId, reason);
    }

    /**
     * 挂起流程:两种方式
     */
    @Test
    public void suspendProcessDef(){
        String id = "";
        runtimeService.suspendProcessInstanceByProcessDefinitionId(id);
        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();
        processInstanceQuery.processInstanceId(id);
        runtimeService.updateProcessInstanceSuspensionState().byProcessInstanceQuery(processInstanceQuery).suspendAsync();
    }
    /**
     * 激活流程: 两种激活流程定义的方法
     */
    @Test
    public void activeProcessDef(){
        String id = "";
        //repositoryService.activateProcessDefinitionById(id);
        repositoryService
                .updateProcessDefinitionSuspensionState()
                .byProcessDefinitionId(id)
                .includeProcessInstances(true)
                .activate();
    }

}
