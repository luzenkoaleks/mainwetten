CREATE UNIQUE INDEX app_user_username_lower_unique
    ON app_user (lower(username));

CREATE UNIQUE INDEX app_user_email_lower_unique
    ON app_user (lower(email));
