package name.auh;

import lombok.extern.slf4j.Slf4j;
import name.auh.config.AccountConfig;
import name.auh.config.AccountsConfig;
import name.auh.data.entity.User;
import name.auh.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
public class InitDBApplicationRunner implements ApplicationRunner {

    @Autowired
    private AccountsConfig accountsConfig;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {

        if (CollectionUtils.isEmpty(accountsConfig.getAccount())) {
            log.warn("no user be added");
            return;
        }

        for (AccountConfig accountConfig : accountsConfig.getAccount()) {
            User user = new User();
            user.setAccount(accountConfig.getUsername());
            user.setPassword(accountConfig.getPassword());
            user.setInvestOpen(accountConfig.getInvest().getBorrow().isOpen());
            user.setCreateTime(System.currentTimeMillis());
            user.setInvestMinMoney(accountConfig.getInvest().getBorrow().getMinInvestAmount());
            user.setInvestMinRate(accountConfig.getInvest().getBorrow().getMinRate());

            userRepository.save(user);
            log.info("init user ok {}", user);
        }

    }
}
