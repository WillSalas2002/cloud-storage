package com.will.cloud.storage.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class AppConstants {

    public static final String BUCKET_NAME = "user-files";
    public static final String PERSONAL_FOLDER_NAME_TEMPLATE = "user-%d-files";

    public static final String MDC_USERNAME_KEY = "username";

    public static final String SIGN_SLASH = "/";
}
