package com.hisrah.accounting.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.hisrah.accounting.entity.AccountType;

public class AccountTreeResponse {

	private Long id;
	private String code;
	private String name;
	private AccountType type;
	private boolean active;
	private List<AccountTreeResponse> children = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AccountType getType() {
		return type;
	}

	public void setType(AccountType type) {
		this.type = type;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<AccountTreeResponse> getChildren() {
		return children;
	}

	public void setChildren(List<AccountTreeResponse> children) {
		this.children = children;
	}
}
