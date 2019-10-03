package name.auh.data.repository;

import name.auh.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Transactional
public interface UserRepository extends JpaRepository<User, String> {

    @Modifying
    @Query(nativeQuery = true, value = "update t_user set amount=:amount where account=:account")
    int updateAmount(@Param("account") String account, @Param("amount") BigDecimal amount);

    @Modifying
    @Query(nativeQuery = true, value = "update t_user set login=:login,loginTime=:loginTime where account=:account")
    int updateLogin(@Param("account") String account, @Param("login") boolean login, @Param("loginTime") Long loginTime);

    List<User> findByLoginIsTrue();

    User findFirstByInvestOpenTrue();

    List<User> findByInvestOpenTrue();

}
