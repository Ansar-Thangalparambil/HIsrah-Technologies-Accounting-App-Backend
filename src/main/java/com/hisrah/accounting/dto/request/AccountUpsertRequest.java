package com.hisrah.accounting.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.hisrah.accounting.entity.AccountType;

public class AccountUpsertRequest {

	@NotBlank(message = "account code is required")
	@Pattern(regexp = "^\\d{4,7}$", message = "account code must be a numeric string between 4 and 7 digits")
	private String code;

	@NotBlank(message = "name is required")
	@Size(max = 100, message = "name must be at most 100 characters")
	private String name;

	@NotNull(message = "type is required")
	private AccountType type;

	private Long parentId;

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

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
}
