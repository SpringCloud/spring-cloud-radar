package com.ppdai.infrastructure.ui.service.common;
/**
 * Constant
 *常量类
 * @author wanghe
 * @date 2018/03/21
 */
public class Constant {
    //0表示超级用户，1表示app管理员，2表示普通用户
    private static final int superUser = 0;
    private static final int appManager = 1;
    private static final int generalUser = 2;
    private static final String pubDescription = "(1表示在线，0表示下线)";
    private static final String subDescription = "(1表示在线，-1表示下线，0表示关闭超级槽位)";
    //远程访问radar-rest的url
    private final static String statUrl = "/app/stat";
    private final static String cache1Url = "/app/cache1";
    private final static String cacheUrl = "/app/cache";
    private final static String traceUrl = "/app/trace";
    private final static String testUrl = "/test";
    private final static String testSynAtlasUrl="/test/atlas";

    public static String getTestSynAtlasUrl() {
        return testSynAtlasUrl;
    }

    public static String getPubDescription() {
        return pubDescription;
    }

    public static String getSubDescription() {
        return subDescription;
    }

    public static int getSuperUser() {
        return superUser;
    }

    public static int getAppManager() {
        return appManager;
    }

    public static int getGeneralUser() {
        return generalUser;
    }

    public static String getStatUrl() {
        return statUrl;
    }

    public static String getCache1Url() {
        return cache1Url;
    }

    public static String getCacheUrl() {
        return cacheUrl;
    }

    public static String getTraceUrl() {
        return traceUrl;
    }

    public static String getTestUrl() {
        return testUrl;
    }
}
