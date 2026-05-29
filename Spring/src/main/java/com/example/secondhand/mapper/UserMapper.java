package com.example.secondhand.mapper;

import com.example.secondhand.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {

    String SELECT_FIELDS = "id, username, password, creditscore AS creditScore, studentid AS studentId, gender, is_admin AS isAdmin";

    @Select("SELECT " + SELECT_FIELDS + " FROM [user] ORDER BY id DESC")
    List<User> findAll();

    @Select("SELECT " + SELECT_FIELDS + " FROM [user] WHERE id = #{id}")
    User findById(Integer id);

    @Insert("INSERT INTO [user](username, password, creditscore, studentid, gender) VALUES(#{username}, #{password}, COALESCE(#{creditScore}, 100), #{studentId}, #{gender})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT " + SELECT_FIELDS + " FROM [user] WHERE username = #{username}")
    User findByUsername(String username);

    @Update("UPDATE [user] SET creditscore = #{creditScore} WHERE id = #{id}")
    int updateCreditScore(@Param("id") Integer id, @Param("creditScore") Integer creditScore);
}
