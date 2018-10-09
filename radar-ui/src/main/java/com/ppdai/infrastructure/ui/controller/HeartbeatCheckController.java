package com.ppdai.infrastructure.ui.controller;

import com.ppdai.infrastructure.radar.biz.common.trace.Tracer;
import com.ppdai.infrastructure.radar.biz.common.trace.spi.Transaction;
import com.ppdai.infrastructure.radar.biz.common.util.HttpClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class HeartbeatCheckController {

    private HttpClient client = new HttpClient(1, 1);
    private RestTemplate restTemplate = new RestTemplate();

    @RequestMapping("/heart/confirm")
    @ResponseBody
    public String confirmHeartBeat(@RequestParam("checkUrl") String checkUrl) {
        String checkResult = "";
        boolean flag = client.check(checkUrl);
        if (flag) {
//            checkResult = call(checkUrl, String.class);
            checkResult = "心跳正常";
        } else {
            checkResult = "心跳异常";
        }
        return checkResult;
    }

    @RequestMapping("/app/heartbeatCheck")
    public String checkResult(@RequestParam("checkResult") String checkResult,@RequestParam("checkUrl") String checkUrl, Model model) {
        model.addAttribute("checkResult",checkResult);
        model.addAttribute("checkUrl",checkUrl);
        return "app/heartbeatCheck";
    }

    private <T> T call(String url, Class<T> t) {
        Transaction transaction = Tracer.newTransaction("cat", url);
        try {
            ResponseEntity<T> responseEntity = restTemplate.getForEntity(url, t);
            transaction.setStatus(Transaction.SUCCESS);
            return responseEntity.getBody();
        } catch (Exception e) {
            transaction.setStatus(e);
        } finally {
            transaction.complete();
        }
        return null;
    }
}
