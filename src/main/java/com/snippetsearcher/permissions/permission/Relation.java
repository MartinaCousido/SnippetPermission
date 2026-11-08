package com.snippetsearcher.permissions.permission;

// Qué relación tiene el usuario con los snippets que pide: los propios, los
// que le compartieron o todos.
public enum Relation {
	OWNER,
	SHARED,
	ALL;

	public static Relation fromValue(String value) {
		for (var relation : values()) {
			if (relation.name().equalsIgnoreCase(value)) {
				return relation;
			}
		}
		throw new UnknownRelationException(value);
	}
}
