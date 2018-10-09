package com.ppdai.infrastructure.radar.biz.common.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ppdai.infrastructure.radar.biz.dal.SoaLockRepository;
import com.ppdai.infrastructure.radar.biz.entity.InstanceEntity;

@Component
public class Util {
	@Autowired
	private SoaLockRepository checkSkRep;
	private final static String DEFAULT_FORMATE = "yyyy-MM-dd HH:mm:ss:SSS";
	/**
	 * 获取进程Id
	 *
	 * @return
	 */
	public static Integer getProcessId() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		String name = runtime.getName();
		return Integer.parseInt(name.substring(0, name.indexOf("@")));
	}

	public Date getDbNow() {
		return checkSkRep.getDbNow();
	}

	public static String formateDate(Date date, String formate) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formate);
		return simpleDateFormat.format(date);
	}

	public static String formateDate(Date date) {
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_FORMATE);
			return simpleDateFormat.format(date);
		} catch (Exception e) {
			return null;
		}
	}

	public static void sleep(long millisecondes) {
		try {
			TimeUnit.MILLISECONDS.sleep(millisecondes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}	
	private static String LogTempate = "app_{}_{}_instance_{}_{}";

	public static void log(Logger log, InstanceEntity t1, String action) {
		log.info(LogTempate + "_{}", t1.getAppId(), t1.getCandAppId(), t1.getId(), t1.getCandInstanceId(),
				action.replaceAll(" ", "_"));
	}

	public static void log(Logger log, InstanceEntity t1, String action, String info) {
		log.info(LogTempate + "_{},{}", t1.getAppId(), t1.getCandAppId(), t1.getId(), t1.getCandInstanceId(),
				action.replaceAll(" ", "_"), info);
	}
	//列表分组
	public static <T> List<List<T>> split(List<T> lst,int count){
		List<List<T>> lstRs=new ArrayList<>();		
		List<T> countLst=new ArrayList<>(count);
		for(T t:lst){
			if(countLst.size()==count){
				lstRs.add(countLst);
				countLst=new ArrayList<>(count);
			}
			countLst.add(t);
		}
		if(countLst.size()>0){
			lstRs.add(countLst);
		}
		return lstRs;
		
	}
}
