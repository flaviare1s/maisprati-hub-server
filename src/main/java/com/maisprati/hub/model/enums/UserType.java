package com.maisprati.hub.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserType {
	STUDENT,
	ADMIN;

	@JsonCreator
	public static UserType fromString(String value) {
		if (value == null) return null;
		return UserType.valueOf(value.toUpperCase());
	}
}
