package com.example.springproject.mapper;




import com.example.springproject.entity.Registration;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

@Mapper
public interface RegistrationMapper {

    @Select("SELECT * FROM registrations WHERE id = #{id}")
    @Results(id = "RegistrationResult", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "event_id", property = "eventId"),
            @Result(column = "registration_date", property = "registrationDate"),
            @Result(column = "created_on", property = "createdOn"),
            @Result(column = "updated_on", property = "updatedOn")
    })
    Optional<Registration> findById(Long id);

    @Select("SELECT * FROM registrations WHERE user_id = #{userId}")
    @ResultMap("RegistrationResult")
    List<Registration> findByUserId(Long userId);

    @Select("SELECT * FROM registrations WHERE event_id = #{eventId}")
    @ResultMap("RegistrationResult")
    List<Registration> findByEventId(Long eventId);

    @Select("SELECT * FROM registrations WHERE user_id = #{userId} AND event_id = #{eventId}")
    @ResultMap("RegistrationResult")
    Optional<Registration> findByUserIdAndEventId(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId);

    @Select("SELECT COUNT(*) FROM registrations WHERE event_id = #{eventId}")
    Integer countByEventId(Long eventId);

    @Select("SELECT COUNT(*) > 0 FROM registrations WHERE user_id = #{userId} AND event_id = #{eventId}")
    boolean existsByUserIdAndEventId(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId);

    @Insert("INSERT INTO registrations (user_id, event_id, registration_date, created_on, updated_on) " +
            "VALUES (#{userId}, #{eventId}, #{registrationDate}, #{createdOn}, #{updatedOn})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Registration registration);

    @Delete("DELETE FROM registrations WHERE id = #{id}")
    void deleteById(Long id);

    @Delete("DELETE FROM registrations WHERE user_id = #{userId} AND event_id = #{eventId}")
    void deleteByUserIdAndEventId(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId);
}