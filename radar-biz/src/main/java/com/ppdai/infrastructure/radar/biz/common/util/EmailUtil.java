package com.ppdai.infrastructure.radar.biz.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.ppdai.infrastructure.radar.biz.common.SoaConfig;
import com.ppdai.infrastructure.radar.biz.common.thread.SoaThreadFactory;

@Component
public class EmailUtil {
	@Autowired
	private SoaConfig soaConfig;
	private final String[] arrInfo = { "info", "warn", "error" };
	private volatile String adminEmail = "";
	private volatile List<String> adminEmailLst = new ArrayList<>();
	private final Object lokObj = new Object();
	private LinkedBlockingDeque<EmailVo> emailVos = new LinkedBlockingDeque<>(1000);
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 3L, TimeUnit.SECONDS,
			new ArrayBlockingQueue<>(50), SoaThreadFactory.create("EmailUtil", true),
			new ThreadPoolExecutor.DiscardPolicy());

	@PostConstruct
	private void init() {
		System.setProperty("mail.mime.charset","UTF-8"); 
		executor.submit(new Runnable() {
			@Override
			public void run() {
				EmailVo emailVo = null;
				while (true) {
					try {
						emailVo = emailVos.take();
					} catch (Exception e) {

					}
					if (emailVo != null) {
						doSendMail(emailVo);
					}
				}

			}
		});
	}

	private List<String> getAdminEmail() {
		if (!adminEmail.equals(soaConfig.getAdminEmail())) {
			synchronized (lokObj) {
				if (!adminEmail.equals(soaConfig.getAdminEmail())) {
					adminEmail = soaConfig.getAdminEmail();
					adminEmailLst = Arrays.asList(adminEmail.split(","));
				}
			}
		}
		return adminEmailLst;
	}

	public void sendInfoMail(String title, String content, List<String> rev) {
		try {
			sendMail(title, content, rev, 0);
		} catch (Exception e) {

		}
	}

	public void sendInfoMail(String title, String content, String rev) {
		try {
			rev = rev + "";
			sendMail(title, content, Arrays.asList(rev.split(",")), 0);
		} catch (Exception e) {

		}
	}

	public void sendInfoMail(String title, String content) {
		try {
			sendMail(title, content, new ArrayList<>(), 0);
		} catch (Exception e) {

		}
	}

	public void sendWarnMail(String title, String content, List<String> rev) {
		try {
			sendMail(title, content, rev, 1);
		} catch (Exception e) {

		}
	}

	public void sendWarnMail(String title, String content, String rev) {
		try {
			rev = rev + "";
			sendMail(title, content, Arrays.asList(rev.split(",")), 1);
		} catch (Exception e) {

		}
	}

	public void sendWarnMail(String title, String content) {
		try {
			sendMail(title, content, new ArrayList<>(), 1);
		} catch (Exception e) {

		}
	}

	public void sendErrorMail(String title, String content, List<String> rev) {
		try {
			sendMail(title, content, rev, 2);
		} catch (Exception e) {

		}
	}

	public void sendErrorMail(String title, String content, String rev) {
		try {
			rev = rev + "";
			sendMail(title, content, Arrays.asList(rev.split(",")), 2);
		} catch (Exception e) {

		}
	}

	public void sendErrorMail(String title, String content) {
		try {
			sendMail(title, content, new ArrayList<>(), 2);
		} catch (Exception e) {

		}
	}

	private void sendMail(String title, String content, List<String> rev, int type) {
		if (!soaConfig.isEmailEnable()) {
			return;
		}
		if (!CollectionUtils.isEmpty(rev) || !CollectionUtils.isEmpty(getAdminEmail())) {
			try {
				emailVos.add(new EmailVo(title, content, rev, type));
			} catch (Exception e) {
				// TODO: handle exception
			}
			// doSendMail(title, content, rev, type);
		}
	}

	private void doSendMail(EmailVo emailVo) {
		try {
			Email email = new SimpleEmail();
			email.setHostName(soaConfig.getEmailHost());
			email.setSmtpPort(soaConfig.getEmailPort());
			email.setAuthenticator(new DefaultAuthenticator(soaConfig.getEmailAuName(), soaConfig.getEmailAuPass()));
			email.setSSLOnConnect(false);
			email.setFrom(soaConfig.getEmailAuName(), "Radar管理员");
			email.setSubject("[Radar-" + arrInfo[emailVo.getType()] + "-" + soaConfig.getEnvName() + "环境]:"
					+ emailVo.getTitle() + " ,send by " + IPUtil.getLocalIP());
			email.setMsg(emailVo.getContent());
			emailVo.getRev().forEach(t1 -> {
				try {
					email.addTo(t1);
				} catch (EmailException e) {

				}
			});
			getAdminEmail().forEach(t1 -> {
				try {
					if (!StringUtils.isEmpty(t1)) {
						email.addTo(t1);
					}
				} catch (EmailException e) {

				}
			});
			email.send();
		} catch (Exception e) {
		}
	}

	class EmailVo {
		String title;
		String content;
		List<String> rev;
		int type;

		public EmailVo() {

		}

		public EmailVo(String title, String content, List<String> rev, int type) {
			this.title = title;
			this.content = content;
			this.rev = rev;
			this.type = type;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public List<String> getRev() {
			return rev;
		}

		public void setRev(List<String> rev) {
			this.rev = rev;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

	}
}
