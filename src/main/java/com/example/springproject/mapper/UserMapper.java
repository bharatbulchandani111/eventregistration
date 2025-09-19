package com.example.springproject.mapper;




import com.example.springproject.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE id = #{id}")
    @Results(id = "UserResult", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "username", property = "username"),
            @Result(column = "email", property = "email"),
            @Result(column = "password", property = "password"),
            @Result(column = "created_on", property = "createdOn"),
            @Result(column = "updated_on", property = "updatedOn")
    })
    Optional<User> findById(Long id);

    @Select("SELECT * FROM users WHERE username = #{username}")
    @ResultMap("UserResult")
    Optional<User> findByUsername(String username);

    @Select("SELECT * FROM users WHERE email = #{email}")
    @ResultMap("UserResult")
    Optional<User> findByEmail(String email);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(String username);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);

    @Insert("INSERT INTO users (username, email, password, created_on, updated_on) " +
            "VALUES (#{username}, #{email}, #{password}, #{createdOn}, #{updatedOn})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(User user);

    @Update("UPDATE users SET username = #{username}, email = #{email}, password = #{password}, " +
            "updated_on = #{updatedOn} WHERE id = #{id}")
    void update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(Long id);
}