package name.auh.data.repository;

import name.auh.data.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Transactional
public interface BorrowRepository extends JpaRepository<Borrow, String> {

    @Modifying
    @Query(nativeQuery = true, value = "update t_borrow set realId=:realId where id=:id")
    int updateRealIdById(@Param("realId") String realId, @Param("id") String id);

    @Modifying
    @Query(nativeQuery = true, value = "update t_borrow set updateTime=:updateTime,leftAmount=:leftAmount where id=:id")
    int updateLeftAmountById(@Param("id") String id,
                             @Param("leftAmount") BigDecimal leftAmount,
                             @Param("updateTime") long updateTime
    );

    @Modifying
    @Query(nativeQuery = true, value = "update t_borrow set notify=:notify where id=:id")
    int updateNotify(@Param("id") String id, @Param("notify") boolean notify);

    @Modifying
    @Query(nativeQuery = true, value = "update t_borrow set canTransfer=:canTransfer where id=:id")
    int updateCanTransfer(@Param("id") String id, @Param("canTransfer") boolean canTransfer);

    @Modifying
    @Query(nativeQuery = true, value = "update t_borrow set upSpeedFlush=:upSpeedFlush where id=:id and upSpeedFlush is not true")
    int updateUpSpeedFlush(@Param("id") String id, @Param("upSpeedFlush") boolean upSpeedFlush);

    @Modifying
    @Query(nativeQuery = true, value = "update t_borrow set onSale=:onSale where id=:id")
    int updateOnsaleById(@Param("id") String id,
                         @Param("onSale") boolean onSale);


}
