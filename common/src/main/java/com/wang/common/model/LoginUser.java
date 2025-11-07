package com.wang.common.model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    /**
     * 用户ID
     */
    private Integer id;
    /**
     * 用户名称
     */
    private String name;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 邮箱
     */
    private String account;

    /*// Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    // toString method
    @Override
    public String toString() {
        return "LoginUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", account='" + account + '\'' +
                '}';
    }

    // equals method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginUser loginUser = (LoginUser) o;
        return java.util.Objects.equals(id, loginUser.id) &&
                java.util.Objects.equals(name, loginUser.name) &&
                java.util.Objects.equals(avatar, loginUser.avatar) &&
                java.util.Objects.equals(account, loginUser.account);
    }

    // hashCode method
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, avatar, account);
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer id;
        private String name;
        private String avatar;
        private String account;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder account(String account) {
            this.account = account;
            return this;
        }

        public LoginUser build() {
            LoginUser loginUser = new LoginUser();
            loginUser.id = this.id;
            loginUser.name = this.name;
            loginUser.avatar = this.avatar;
            loginUser.account = this.account;
            return loginUser;
        }
    }

    // Constructors
    public LoginUser() {
    }

    private LoginUser(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.avatar = builder.avatar;
        this.account = builder.account;
    }*/
}

