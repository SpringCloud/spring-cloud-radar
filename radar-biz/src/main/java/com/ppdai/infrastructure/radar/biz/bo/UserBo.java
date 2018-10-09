package com.ppdai.infrastructure.radar.biz.bo;
/**
 * UserBo
 *
 * @author wanghe
 * @date 2018/03/21
 */
public class UserBo {

	private String userId;
	private String name;
	private String email;
	private String department;
	private boolean isAdmin;

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof UserBo) {

			if (o == this) {
				return true;
			}

			UserBo anotherUser = (UserBo) o;
			return userId.equals(anotherUser.userId);
		} else {
			return false;
		}

	}
}
