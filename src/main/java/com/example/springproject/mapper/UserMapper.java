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
            @Result(column = "updated_on", property = "updatedOn"),
            // 👇 load roles for this user
            @Result(property = "roles", column = "id",
                    many = @Many(select = "com.example.springproject.mapper.UserMapper.findRolesByUserId"))
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

    @Select("SELECT * FROM users")
    @ResultMap("UserResult")
    List<User> findAll();

    @Select("SELECT EXISTS(SELECT 1 FROM user_roles WHERE user_id = #{userId} AND role_id = #{roleId})")
    boolean userHasRole(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    @Select("SELECT r.* FROM roles r")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "created_on", property = "createdOn"),
            @Result(column = "updated_on", property = "updatedOn")
    })
    List<com.example.springproject.entity.Role> findAllRoles();

    @Insert("INSERT INTO user_roles (user_id, role_id, created_on) VALUES (#{userId}, #{roleId}, CURRENT_TIMESTAMP)")
    void addRoleToUser(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    @Delete("DELETE FROM user_roles WHERE user_id = #{userId} AND role_id = #{roleId}")
    void removeRoleFromUser(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    void removeAllRolesFromUser(Long userId);

    @Select("SELECT r.* FROM roles r JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "created_on", property = "createdOn"),
            @Result(column = "updated_on", property = "updatedOn")
    })
    List<com.example.springproject.entity.Role> findRolesByUserId(Long userId);

}