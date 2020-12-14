package com.github.tkmybatis.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Joey
 * @date 2020/9/24
 * @description User实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User {
    @Id
    private String id;
    @Id
    private String name;
    private Integer age;
    private String phone;
}
