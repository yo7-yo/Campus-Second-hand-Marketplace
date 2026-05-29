package com.example.secondhand.mapper;

import com.example.secondhand.entity.Product;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ProductMapper {
    @Select("""
            SELECT p.id AS id, p.title AS title, p.description AS description, p.price AS price,
                   p.imagepath AS imagePath, p.ownerid AS ownerId, p.categoryid AS categoryId,
                   p.status AS status, p.location AS location, c.name AS categoryName, u.username AS ownerName
            FROM product p
            LEFT JOIN category c ON p.categoryid = c.id
            LEFT JOIN [user] u ON p.ownerid = u.id
            WHERE (#{keyword} IS NULL OR #{keyword} = '' OR p.title LIKE CONCAT('%', #{keyword}, '%') OR p.description LIKE CONCAT('%', #{keyword}, '%'))
              AND (#{categoryId} IS NULL OR p.categoryid = #{categoryId})
            ORDER BY p.id DESC
            """)
    List<Product> search(@Param("keyword") String keyword, @Param("categoryId") Integer categoryId);

    @Select("""
            SELECT p.id AS id, p.title AS title, p.description AS description, p.price AS price,
                   p.imagepath AS imagePath, p.ownerid AS ownerId, p.categoryid AS categoryId,
                   p.status AS status, p.location AS location, c.name AS categoryName, u.username AS ownerName
            FROM product p
            LEFT JOIN category c ON p.categoryid = c.id
            LEFT JOIN [user] u ON p.ownerid = u.id
            WHERE p.id = #{id}
            """)
    Product findById(Integer id);

    @Insert("""
            INSERT INTO product(title, description, price, imagepath, ownerid, categoryid, status, location)
            VALUES(#{title}, #{description}, #{price}, #{imagePath}, #{ownerId}, #{categoryId}, COALESCE(#{status}, 'available'), #{location})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE product SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Update("""
            UPDATE product SET title = #{title}, description = #{description}, price = #{price},
                   imagepath = #{imagePath}, categoryid = #{categoryId}, location = #{location}
            WHERE id = #{id}
            """)
    int update(Product product);

    @Delete("DELETE FROM product WHERE id = #{id}")
    int delete(Integer id);

    @Select("""
            SELECT p.id AS id, p.title AS title, p.description AS description, p.price AS price,
                   p.imagepath AS imagePath, p.ownerid AS ownerId, p.categoryid AS categoryId,
                   p.status AS status, p.createdtime AS createdTime, p.location AS location,
                   c.name AS categoryName, u.username AS ownerName
            FROM product p
            LEFT JOIN category c ON p.categoryid = c.id
            LEFT JOIN [user] u ON p.ownerid = u.id
            WHERE p.ownerid = #{ownerId}
            ORDER BY p.id DESC
            """)
    List<Product> findByOwnerId(Integer ownerId);
}
