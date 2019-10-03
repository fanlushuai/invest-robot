package name.auh.data.repository;

import name.auh.data.entity.RedReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

@Transactional
public interface RedRewardRepository extends JpaRepository<RedReward, String> {

}
