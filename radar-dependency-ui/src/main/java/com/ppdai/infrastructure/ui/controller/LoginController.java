package com.ppdai.infrastructure.ui.controller;

import com.ppdai.infrastructure.radar.biz.bo.OrganizationBo;
import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.radar.biz.bo.UserBo;
import com.ppdai.infrastructure.radar.biz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LoginController
 *
 * @author wanghe
 * @date 2018/03/21
 */

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public String first(HttpServletRequest request, HttpServletResponse response) {
        // TODO: 自行添加登录逻辑
        return "redirect:/login";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("index")
    public String index( Model model) {
        model.addAllAttributes(setModelMap());
        return "index";
    }


    /**
     * 用户退出
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // TODO: 自行添加清除token逻辑
        return "redirect:/login";
    }

    @RequestMapping(value = "/users",method = RequestMethod.POST)
    @ResponseBody
    public UiResponse getUsers(String userId){
        UiResponse uiResponse=new UiResponse();
        Map<String, UserBo> users = userService.getUsers();
        List<UserBo> userBoList=new ArrayList<>();
        if(users.get(userId)!=null){
            userBoList.add(users.get(userId));
        }else{
            for(Map.Entry<String,UserBo> entry:users.entrySet()){
                UserBo userBo=entry.getValue();
                userBoList.add(userBo);
            }
        }
        uiResponse.setCount((long) userBoList.size());
        uiResponse.setData(userBoList);
        return uiResponse;
    }

    @RequestMapping(value = "/organizations",method = RequestMethod.POST)
    @ResponseBody
    public UiResponse getOrganization(){
        UiResponse uiResponse=new UiResponse();
        Map<String, OrganizationBo> users = userService.getOrgs();
        List<OrganizationBo> organizationBoList=new ArrayList<>();
        for(Map.Entry<String,OrganizationBo> entry:users.entrySet()){
            OrganizationBo organizationBo=entry.getValue();
            organizationBoList.add(organizationBo);
        }
        uiResponse.setCount((long) organizationBoList.size());
        uiResponse.setData(organizationBoList);
        return uiResponse;
    }


    private Map setModelMap(){
        Map modelMap=new HashMap();
        modelMap.put("userId", userService.getCurrentUser().getUserId());
        return modelMap;
    }
}
