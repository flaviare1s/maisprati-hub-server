package com.maisprati.hub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserType {
	ADMIN("admin"),
	STUDENT("student");
	
	private final String name;
}
