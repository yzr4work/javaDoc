package com.yzr.speechmatching.controller;

import com.yzr.speechmatching.model.RespModel;
import com.yzr.speechmatching.model.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.Year;

/**
 * 测试controller
 * @author yzr11
 */
@RestController
@RequestMapping(value = {"/matching"})
public class MatchingController {

    /**
     * test方法
     *
     * @param uid 用户uid
     * @param status 状态
     *
     * @nullable status,uid
     * @status false
     *
     * @return  返回信息 {@link User }
     */
    @PostMapping(value = {"/test"})
    public RespModel<User> test(HttpServletRequest request){
        RespModel respModel = new RespModel();
        return respModel.success(new User());
    }



    /**
     * test21234方法注释
     *
	 * @param user 用户信息  {@link User user}
     * @status true
     * @nullable
     *
	* @return  返回信息 {@link User }
    **/
    @PostMapping(value = {"/test2"})
    public RespModel<User> test2(HttpServletRequest request){
        RespModel respModel = new RespModel();
        return respModel.success(new User());
    }

    /**
     * 请填写方法描述
     * @author yzr
     * @author yzr2
     *
	 * @param request 描述 {@link request }
	 * @param id 描述
     *
     * @nullable
     * @status false
     *
	 * @return  返回信息
     */
    @PostMapping(value = {"/test3"})
    public RespModel test3(HttpServletRequest request,int id){
        RespModel respModel = new RespModel();
        return respModel.success(new User());
    }

}
