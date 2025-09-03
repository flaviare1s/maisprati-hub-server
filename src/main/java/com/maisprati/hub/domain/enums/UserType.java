package com.maisprati.hub.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserType {
	ADMIN("admin"),
	STUDENT("student");
	
	private final String name;

  @JsonCreator
    public static UserType fromString(String value) {
        if (value == null) return null;
        for (UserType type : UserType.values()) {
            if (type.name.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.name;
    }
}
