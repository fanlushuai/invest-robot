package name.auh.data.repository;

import name.auh.data.entity.CouponRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

@Transactional
public interface CouponRateRepository extends JpaRepository<CouponRate, String> {

    @Query(nativeQuery = true, value = "select * from t_coupon_rate order by labelStrBigDecimal desc limit 1")
    CouponRate findMaxRate();

}
