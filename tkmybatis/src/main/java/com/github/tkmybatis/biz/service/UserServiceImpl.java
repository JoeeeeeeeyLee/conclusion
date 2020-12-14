package com.github.tkmybatis.biz.service;

import com.github.tkmybatis.biz.inter.UserService;
import com.github.tkmybatis.dao.UserMapper;
import com.github.tkmybatis.dao.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Joey
 * @date 2020/9/24
 * @description User服务的实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User implSelectOne(User user) {
        return userMapper.selectOne(user);
    }

    @Override
    public List<User> implSelect(User user) {
        return userMapper.select(user);
    }

    @Override
    public List<User> implSelectAll() {
        return userMapper.selectAll();
    }

    @Override
    public int implSelectCount(User user) {
        return userMapper.selectCount(user);
    }

    @Override
    public User implSelectByPrimaryKey(String id, String name) {
        User user=new User();
        user.setId(id);
        user.setName(name);
        return userMapper.selectByPrimaryKey(user);
    }

    @Override
    public boolean implExistsWithPrimaryKey(String id, String name) {
        User user=new User();
        user.setId(id);
        user.setName(name);
        return userMapper.existsWithPrimaryKey(user);
    }

    @Override
    public int implInsert(User user) {
        return userMapper.insert(user);
    }

    @Override
    public int implInsertSelective(User user) {
        return userMapper.insert(user);
    }

    @Override
    public int implUpdateByPrimaryKey(User user) {
        return userMapper.updateByPrimaryKey(user);
    }

    //这个记得尝试一下如果其中的一个主键的值没有设置会怎样
    @Override
    public int implUpdateByPrimaryKeySelective(User user) {
        return userMapper.updateByPrimaryKey(user);
    }

    @Override
    public int implDelete(User user) {
        return userMapper.delete(user);
    }

    @Override
    public int implDeleteByPrimaryKey(String id, String name) {
        User user=new User();
        user.setId(id);
        user.setName(name);
        return userMapper.deleteByPrimaryKey(user);
    }
}
