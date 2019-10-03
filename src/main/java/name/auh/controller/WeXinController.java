package name.auh.controller;

import name.auh.service.WeXinQiYeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weixin")
public class WeXinController {

    @Autowired
    private WeXinQiYeService weXinQiYeService;

    @PostMapping("/send/msg")
    public String sendMsg(@RequestParam("msg") String msg, @RequestParam("toUser") String toUser) {
        weXinQiYeService.sendMessageTo(toUser, msg);
        return "ok";
    }

}
