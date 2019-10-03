package name.auh.config;

import lombok.Data;

@Data
public class AccountConfig {

    private String username;

    private String weChat;

    private String password;

    private InvestConfig invest;

}
