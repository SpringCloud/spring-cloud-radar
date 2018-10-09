package com.ppdai.infrastructure.ui.service.common;

import com.ppdai.infrastructure.radar.biz.dto.ui.UiResponse;

import java.util.List;

/**
 * SetParameters
 *公共静态类：设置参数
 * @author wanghe
 * @date 2018/03/21
 */

public class UiResponseHelper {
   public static UiResponse setUiResponse(UiResponse uiResponse, Long count, List list){
       uiResponse.setCode("0");
       uiResponse.setMsg("");
       uiResponse.setCount(count);
       uiResponse.setData(list);
       return uiResponse;
   }

}
