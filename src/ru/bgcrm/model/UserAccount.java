package ru.bgcrm.model;

public interface UserAccount {
    public String getLogin();

    public String getPassword();

    public static class Default implements UserAccount {
        private final String login;
        private final String password;

        public Default(String login, String pswd) {
            this.login = login;
            this.password = pswd;
        }

        @Override
        public String getLogin() {
            return login;
        }

        @Override
        public String getPassword() {
            return password;
        }
    }
}
