package com.github.tkmybatis.mvc.controller;

import com.github.tkmybatis.biz.inter.UserService;
import com.github.tkmybatis.dao.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Joey
 * @date 2020/9/24
 * @description User控制器
 */
@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //===========================================================
    //Select
    @GetMapping("/select/1")
    public User getInfo1(@RequestBody User user) {
        return userService.implSelectOne(user);
    }

    @GetMapping("/select/2")
    public List<User> getInfo2(@RequestBody User user) {
        return userService.implSelect(user);
    }

    @GetMapping("/select/3")
    public List<User> getInfo3() {
        return userService.implSelectAll();
    }

    @GetMapping("/select/4")
    public int getInfo4(@RequestBody User user) {
        return userService.implSelectCount(user);
    }

    @GetMapping("/select/5")
    public User getInfo5(@RequestParam String id,
                           @RequestParam String name) {
        return userService.implSelectByPrimaryKey(id, name);
    }

    @GetMapping("/select/6")
    public boolean getInfo6(@RequestParam String id,
                            @RequestParam String name) {
        return userService.implExistsWithPrimaryKey(id, name);
    }

    //===========================================================
    //insert

    @PostMapping(value = "/insert/1")
    public int insertInfo1(@RequestBody User user) {
        return userService.implInsert(user);
    }

    @PostMapping("/insert/2")
    public int insertInfo2(@RequestBody User user) {
        return userService.implInsertSelective(user);
    }

    //===========================================================
    //update

    @PostMapping("/update/1")
    public int updateInfo1(@RequestBody User user) {
        return userService.implUpdateByPrimaryKey(user);
    }

    @PostMapping("/update/2")
    public int updateInfo2(@RequestBody User user) {
        return userService.implUpdateByPrimaryKeySelective(user);
    }

    //===========================================================
    //delete

    @DeleteMapping("/delete/1")
    public int deleteInfo1(@RequestBody User user) {
        return userService.implDelete(user);
    }

    @DeleteMapping("/delete/2")
    public int deleteInfo2(@RequestParam String id,
                           @RequestParam String name) {
        return userService.implDeleteByPrimaryKey(id, name);
    }

}