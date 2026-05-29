package com.example.secondhand.mapper;

import com.example.secondhand.entity.TradeOrder;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TradeOrderMapper {

    String SELECT_FIELDS = """
            o.id AS id, o.productid AS productId, o.buyerid AS buyerId, o.sellerid AS sellerId,
            o.status AS status, o.createdtime AS createdTime,
            o.refund_reason AS refundReason, o.refund_response AS refundResponse, o.refund_status AS refundStatus,
            p.title AS productTitle, p.price AS price, bu.username AS buyerName, su.username AS sellerName
            """;

    String JOIN_CLAUSE = """
            FROM tradeorder o
            LEFT JOIN product p ON o.productid = p.id
            LEFT JOIN [user] bu ON o.buyerid = bu.id
            LEFT JOIN [user] su ON o.sellerid = su.id
            """;

    @Select("SELECT " + SELECT_FIELDS + " " + JOIN_CLAUSE + " ORDER BY o.id DESC")
    List<TradeOrder> findAll();

    @Insert("""
            INSERT INTO tradeorder(productid, buyerid, sellerid, status)
            VALUES(#{productId}, #{buyerId}, #{sellerId}, COALESCE(#{status}, 'pending'))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TradeOrder order);

    @Update("UPDATE tradeorder SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Update("UPDATE tradeorder SET status = 'refund_pending', refund_reason = #{reason}, refund_status = 'pending' WHERE id = #{id}")
    int requestRefund(@Param("id") Integer id, @Param("reason") String reason);

    @Update("UPDATE tradeorder SET refund_response = #{response}, refund_status = 'seller_responded' WHERE id = #{id}")
    int respondRefund(@Param("id") Integer id, @Param("response") String response);

    @Update("UPDATE tradeorder SET status = 'refunded', refund_status = 'approved' WHERE id = #{id}")
    int approveRefund(@Param("id") Integer id);

    @Update("UPDATE tradeorder SET status = 'pending', refund_status = 'rejected', refund_reason = NULL, refund_response = NULL WHERE id = #{id}")
    int rejectRefund(@Param("id") Integer id);

    @Select("SELECT " + SELECT_FIELDS + " " + JOIN_CLAUSE + " WHERE o.id = #{id}")
    TradeOrder findById(Integer id);

    @Select("SELECT " + SELECT_FIELDS + " " + JOIN_CLAUSE + " WHERE o.buyerid = #{buyerId} ORDER BY o.id DESC")
    List<TradeOrder> findByBuyerId(Integer buyerId);

    @Select("SELECT " + SELECT_FIELDS + " " + JOIN_CLAUSE + " WHERE o.sellerid = #{sellerId} ORDER BY o.id DESC")
    List<TradeOrder> findBySellerId(Integer sellerId);

    @Select("SELECT " + SELECT_FIELDS + " " + JOIN_CLAUSE + " WHERE o.refund_status IN ('pending', 'seller_responded') ORDER BY o.id DESC")
    List<TradeOrder> findPendingRefunds();
}
