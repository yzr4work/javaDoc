package com.yzr.speechmatching.controller;

import com.yzr.speechmatching.model.RespModel;
import com.yzr.speechmatching.model.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 匹配controller
 * @author yzr
 */
@RestController
@RequestMapping(value = {"/matching","web/webApi"})
public class MatchingController {
    /**
     *  test方法注释
     * @param uid 用户uid
     * @param status 状态
     * @param user 用户信息 {@link User user}
     * @return 返回信息 {@link User resp}
     *
     */
    @PostMapping(value = {"/test","/test2"})
    public RespModel test(@RequestParam("uid") String uid, int status, User user){
        return RespModel.success(null);
    }


}
