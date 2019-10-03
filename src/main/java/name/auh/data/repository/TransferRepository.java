package name.auh.data.repository;

import name.auh.data.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

@Transactional
public interface TransferRepository extends JpaRepository<Transfer, String> {

    @Modifying
    @Query(nativeQuery = true, value = "update t_transfer set notify=:notify where id=:id")
    int updateNotify(@Param("id") String id, @Param("notify") boolean notify);

    @Modifying
    @Query(nativeQuery = true, value = "update t_transfer set onSale=:onSale where id=:id")
    int updateOnSaleById(@Param("id") String id,
                         @Param("onSale") boolean onSale);
}
