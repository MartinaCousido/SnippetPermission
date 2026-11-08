package com.snippetsearcher.permissions.permission;

// Qué puede hacer un usuario con un snippet. El owner lee y escribe; con quien
// se compartió solo lee; cualquier otro, nada.
public record Access(boolean canRead, boolean canWrite) {

	static final Access NONE = new Access(false, false);

	static Access of(PermissionType type) {
		return switch (type) {
			case OWNER -> new Access(true, true);
			case LECTURA -> new Access(true, false);
		};
	}
}
