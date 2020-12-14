package com.github.tkmybatis.mvc.vo;

import lombok.*;

/**
 * @author Joey
 * @date 2020/9/24
 * @description UserVo
 */
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {
    private String id;
    private String name;
    private Integer age;
    private String phone;
}
