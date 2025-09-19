package com.example.springproject.mapper;



import com.example.springproject.entity.Event;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EventMapper {

    @Select("SELECT * FROM events WHERE id = #{id}")
    @Results(id = "EventResult", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "description", property = "description"),
            @Result(column = "date", property = "date"),
            @Result(column = "location", property = "location"),
            @Result(column = "capacity", property = "capacity"),
            @Result(column = "created_on", property = "createdOn"),
            @Result(column = "updated_on", property = "updatedOn")
    })
    Optional<Event> findById(Long id);

    @Select("SELECT * FROM events ORDER BY date ASC")
    @ResultMap("EventResult")
    List<Event> findAllOrderByDateAsc();

    @Select("SELECT * FROM events WHERE name ILIKE CONCAT('%', #{name}, '%')")
    @ResultMap("EventResult")
    List<Event> findByNameContaining(String name);

    @Select("SELECT COUNT(*) > 0 FROM events WHERE id = #{id}")
    boolean existsById(Long id);

    @Insert("INSERT INTO events (name, description, date, location, capacity, created_on, updated_on) " +
            "VALUES (#{name}, #{description}, #{date}, #{location}, #{capacity}, #{createdOn}, #{updatedOn})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Event event);

    @Update("UPDATE events SET name = #{name}, description = #{description}, date = #{date}, " +
            "location = #{location}, capacity = #{capacity}, updated_on = #{updatedOn} WHERE id = #{id}")
    void update(Event event);

    @Delete("DELETE FROM events WHERE id = #{id}")
    void deleteById(Long id);
}