package com.ppdai.infrastructure.ui.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ppdai.infrastructure.radar.biz.bo.OrganizationBo;
import com.ppdai.infrastructure.radar.biz.bo.UserBo;
import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.service.UserService;
import com.ppdai.infrastructure.ui.utils.DesUtil;


public class LdapUserService implements UserService {
    private final Logger LOG = LoggerFactory.getLogger(LdapUserService.class);
    @Autowired
    private SoaConfig soaConfig;
    private AtomicReference<String> adServer = new AtomicReference<>();
    private AtomicReference<String[]> searchBase = new AtomicReference<>();
    private final AtomicReference<Map<String, UserBo>> mapUserRef = new AtomicReference<>(
            new HashMap<String, UserBo>());
    private final AtomicReference<Map<String, OrganizationBo>> orgsRef = new AtomicReference<>(new HashMap<>());
    private final Timer managerTimer = new Timer();

    public LdapUserService() {
    }

    @PostConstruct
    private void init() {
        doInitParm();
        managerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    doInitParm();
                    initUser();
                } catch (Throwable t) {

                }
            }
        }, 5000, 1000 * 60);
        // }, 30000, 1000 * 6);
    }

    private void doInitParm() {
        String adServerTemp = soaConfig.getADServer();
        if(!StringUtils.isEmpty(adServerTemp)){
            adServer.set(adServerTemp);
        }
        String searchBaseTemp = soaConfig.getSearchBase();
        if(!StringUtils.isEmpty(searchBaseTemp)){
            String[] searchBaseTemp1 = searchBaseTemp.split("\\|");
            searchBase.set(searchBaseTemp1);
        }

    }

    private boolean initUser() {
        try {
            Map<String, UserBo> mapUser = new HashMap<>();
            Map<String, OrganizationBo> orgMap = new HashMap<>();
            String[] searchBase1 = searchBase.get();
            if(!StringUtils.isEmpty(searchBase1)){
                for (String serverPath : searchBase1) {
                    try {
                        doInitUser(mapUser, orgMap, serverPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Thread.sleep(10);
                }
            }
            addRadar(mapUser);
            mapUserRef.set(mapUser);
            orgsRef.set(orgMap);
            return true;
        } catch (Exception e) {
            LOG.error("ldap初始化失败", e);
            return false;
        }
    }

    private void doInitUser(Map<String, UserBo> userInfos, Map<String, OrganizationBo> orgMap, String serverPath)
            throws NamingException {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, soaConfig.getRadarLdapUser());
        env.put(Context.SECURITY_CREDENTIALS, soaConfig.getRadarLdapPass());
        env.put(Context.PROVIDER_URL, adServer.get());

        LdapContext ctx = new InitialLdapContext(env, null);
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String searchFilter = String
                .format("(&(objectClass=top)(objectClass=user)(objectClass=person)(objectClass=organizationalPerson))");

        String returnedAtts[] = { "memberOf", "sAMAccountName", "cn", "distinguishedName", "mail" };
        searchCtls.setReturningAttributes(returnedAtts);
        NamingEnumeration<SearchResult> answer = ctx.search(serverPath, searchFilter, searchCtls);
        while (answer.hasMoreElements()) {
            SearchResult sr = (SearchResult) answer.next();
            Attributes at = sr.getAttributes();
            UserBo userBo = new UserBo();
            userBo.setDepartment(getDValue(at.get("distinguishedName")));
            userBo.setEmail(getValue(at.get("mail")));
            userBo.setUserId(getValue(at.get("sAMAccountName")));
            userBo.setName(getValue(at.get("cn")));
            userBo.setAdmin(false);
            if ((","+soaConfig.getAdminUsers()+",").indexOf(","+userBo.getUserId()+",") != -1) {
                userBo.setAdmin(true);
            }
            userInfos.put(userBo.getUserId(), userBo);
            if (!StringUtils.isEmpty(userBo.getDepartment())) {
                OrganizationBo organization = new OrganizationBo();
                organization.setOrgId(userBo.getDepartment());
                organization.setOrgName(userBo.getDepartment());
                orgMap.put(userBo.getDepartment(), organization);
            }
        }
        ctx.close();
    }

    private void addRadar(Map<String, UserBo> mapUser) {
        if (!mapUser.containsKey(soaConfig.getRadarAdminUser())) {
            UserBo userInfo = new UserBo();
            userInfo.setAdmin(true);
            userInfo.setDepartment("基础框架");
            userInfo.setEmail("radar@radar.com");
            userInfo.setName("radar");
            userInfo.setUserId("radar");
            mapUser.put("radar", userInfo);
        }

    }

    @Override
    public boolean login(String username, String password) {
        doInitParm();
        if (username.equals(soaConfig.getRadarAdminUser()) && password.equals(soaConfig.getRadarAdminPass())) {
            return true;
        }
        return doLogin(username, password);
    }

    private boolean doLogin(String username, String password) {
        LdapContext ctx = null;
        try {
            Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, "corp\\" + username);
            env.put(Context.SECURITY_CREDENTIALS, password);
            env.put(Context.PROVIDER_URL, adServer.get());

            ctx = new InitialLdapContext(env, null);
            return true;
        } catch (NamingException e) {
            e.printStackTrace();
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {

                }
            }
        }
        return false;
    }

    private String getDValue(Attribute attribute) {
        String value = getValue(attribute);
        if (value.indexOf(",") > -1) {
            value = value.split(",")[1];
            value = value.replaceAll("OU=", "").trim();
        }
        return value;
    }

    @Override
    public Map<String, UserBo> getUsers() {
        return mapUserRef.get();
    }

    @Override
    public Map<String, OrganizationBo> getOrgs() {
        return orgsRef.get();
    }

    private String getValue(Attribute attribute) {
        if (attribute == null) {
            return "";
        }
        String value = attribute.toString();
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        if (value.indexOf(":") != -1) {
            value = value.replaceAll(value.split(":")[0], "").trim();
            value = value.substring(1, value.length()).trim();
        }
        return value;
    }

    /**
     * 从token中获取用户名
     *
     * @return
     */
    @Override
    public UserBo getCurrentUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        //获取一个cookie数组
        Cookie[] cookies = request.getCookies();
        String userId = "";
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("userSessionId")) {
                    try {
                        userId = DesUtil.decrypt(cookie.getValue());
                    } catch (Exception e) {
                        userId = "";
                    }
                }
            }
        }
        return getUsers().get(userId);
    }
}
