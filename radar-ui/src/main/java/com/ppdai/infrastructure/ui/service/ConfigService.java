package com.ppdai.infrastructure.ui.service;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;
import com.ppdai.infrastructure.ui.bo.ConfigBo;
import com.ppdai.infrastructure.ui.service.common.UiResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
public class ConfigService {
    @Autowired
    SoaConfig soaConfig;

    public UiResponse getConfigData(){
        UiResponse uiResponse = new UiResponse();
        List<ConfigBo> configList =new ArrayList<>();
        Map<String,String> keyMap=new LinkedHashMap<>();
        Map<String,String> innerKeyMap=new LinkedHashMap<>();
        Map<String,String> defaultValueMap=new HashMap<>();
        Map<String,String> desMap=new HashMap<>();
        String fieldsName="";
        String keyPre="";
        String methodName="";

        Field[] fields = SoaConfig.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fieldsName=fields[i].getName();
            if(fieldsName.startsWith("env_")){
                fields[i].setAccessible(true);
                try {
                    if(fieldsName.endsWith("_key")){
                        keyMap.put(fieldsName,fields[i].get(soaConfig).toString());
                    }else if(fieldsName.endsWith("_keyInner")){
                        innerKeyMap.put(fieldsName,fields[i].get(soaConfig).toString());
                    }
                    else if(fieldsName.endsWith("_defaultValue")){
                        defaultValueMap.put(fieldsName,fields[i].get(soaConfig).toString());
                    }else if(fieldsName.endsWith("_des")){
                        desMap.put(fieldsName,fields[i].get(soaConfig).toString());
                    }

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        keyMap.putAll(innerKeyMap);

        for(String key:keyMap.keySet()){
            ConfigBo configBo=new ConfigBo();
            methodName=key.substring(key.indexOf("_") + 1, key.lastIndexOf("_"));
            keyPre=key.substring(0,key.lastIndexOf("_"));
            try {
                Method method = SoaConfig.class.getDeclaredMethod(methodName);
                method.setAccessible(true);
                configBo.setKey(keyMap.get(key));
                configBo.setDefaultValue(defaultValueMap.get(keyPre+"_defaultValue"));
                configBo.setDescription(desMap.get(keyPre+"_des"));
                configBo.setCurrentValue(method.invoke(soaConfig).toString());
                configList.add(configBo);

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }


        }
        return UiResponseHelper.setUiResponse(uiResponse, new Long(configList.size()), configList);
    }
}
