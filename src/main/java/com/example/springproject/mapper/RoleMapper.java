package com.example.springproject.mapper;




import org.apache.ibatis.annotations.*;

import javax.management.relation.Role;
import java.util.Optional;

@Mapper
public interface RoleMapper {

    @Select("SELECT * FROM roles WHERE id = #{id}")
    @Results(id = "RoleResult", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "created_on", property = "createdOn"),
            @Result(column = "updated_on", property = "updatedOn")
    })
    Optional<Role> findById(Integer id);

    @Select("SELECT * FROM roles WHERE name = #{name}")
    @ResultMap("RoleResult")
    Optional<Role> findByName(String name);

    @Insert("INSERT INTO roles (name, created_on, updated_on) " +
            "VALUES (#{name}, #{createdOn}, #{updatedOn})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Role role);
}