package com.demo.security;

public enum AppPermission {
    READ_TASK,
    WRITE_TASK,
    DELETE_TASK,
    MANAGE_USERS,
    MANAGE_PERMISSIONS;

    public static final class Names {
        private Names() {}

        public static final String READ_TASK          = "READ_TASK";
        public static final String WRITE_TASK         = "WRITE_TASK";
        public static final String DELETE_TASK        = "DELETE_TASK";
        public static final String MANAGE_USERS       = "MANAGE_USERS";
        public static final String MANAGE_PERMISSIONS = "MANAGE_PERMISSIONS";
    }

    static {
        for (AppPermission permission : values()) {
            try {
                String declared = (String) Names.class
                        .getField(permission.name())
                        .get(null);

                if (!permission.name().equals(declared)) {
                    throw new ExceptionInInitializerError(
                            "AppPermission.Names." + permission.name()
                            + " value \"" + declared
                            + "\" does not match enum constant \""
                            + permission.name() + "\""
                    );
                }
            } catch (NoSuchFieldException e) {
                throw new ExceptionInInitializerError(
                        "AppPermission.Names is missing constant for: " + permission.name()
                );
            } catch (IllegalAccessException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }
}
