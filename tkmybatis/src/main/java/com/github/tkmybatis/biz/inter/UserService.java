package com.github.tkmybatis.biz.inter;

import com.github.tkmybatis.dao.entity.User;

import java.util.List;

/**
 * @author Joey
 * @date 2020/9/24
 * @description User服务接口定义
 */
public interface UserService {
    User implSelectOne(User user);
    List<User> implSelect(User user);
    List<User> implSelectAll();
    int implSelectCount(User user);
    User implSelectByPrimaryKey(String id,String name);
    boolean implExistsWithPrimaryKey(String id,String name);

    int implInsert(User user);
    int implInsertSelective(User user);

    int implUpdateByPrimaryKey(User user);
    int implUpdateByPrimaryKeySelective(User user);

    int implDelete(User user);
    int implDeleteByPrimaryKey(String id,String name);
}
