package com.github.tkmybatis.biz.converter;

import com.github.tkmybatis.dao.entity.User;
import com.github.tkmybatis.mvc.vo.UserVo;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author Joey
 * @date 2020/9/24
 * @description 实体转换
 */
@Mapper(componentModel = "spring")
public interface UserConverter {
    User UserVoToUser(UserVo userVo);
    UserVo UserToUserVo(User user);

    List<User> ListUserVoToListUser(List<UserVo> userVos);
    List<UserVo> ListUserToListUserVo(List<User> users);

}
