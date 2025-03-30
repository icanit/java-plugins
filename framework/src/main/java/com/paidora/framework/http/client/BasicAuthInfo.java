package com.paidora.framework.http.client;

import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import com.paidora.framework.utils.Base64;
import com.paidora.framework.utils.uri.Uri;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * Контейнер с логином и паролем.
 */
public class BasicAuthInfo {

    public static final String BASIC_PREFIX = "Basic ";

    @Getter
    private final String login;
    @Getter
    private final String password;
    private String compiled = null;

    public BasicAuthInfo(String login, String password) {
        this.login = login;
        this.password = password;
    }

    private BasicAuthInfo(String login, String password, String compiled) {
        this.login = login;
        this.password = password;
        this.compiled = compiled;
    }


    public static BasicAuthInfo parse(String kludge) {
        if (kludge == null || !kludge.startsWith(BASIC_PREFIX)) {
            return null;
        }
        String authString = kludge.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.base64ToByteArray(authString));
        int semicolonPos = decoded.indexOf(':');
        if (semicolonPos == -1) {
            return new BasicAuthInfo(decoded, null, authString);
        } else {
            return new BasicAuthInfo(
                    decoded.substring(0, semicolonPos),
                    decoded.substring(semicolonPos + 1),
                    kludge
            );
        }
    }

    public static BasicAuthInfo parse(Uri url) throws UnexpectedBehaviourException {
        if (!url.hasUsername()) {
            throw new UnexpectedBehaviourException("No username specified in url " + url);
        }
        return new BasicAuthInfo(url.getUsername(), url.getPassword());
    }

    public String getCompiled() {
        if (compiled == null) {
            StringBuilder sb = new StringBuilder(32);
            sb.append(login);
            if (password != null) {
                sb.append(':');
                sb.append(password);
            }
            compiled = BASIC_PREFIX + Base64.byteArrayToBase64(sb.toString().getBytes(StandardCharsets.UTF_8));
        }
        return compiled;
    }

    @Override
    public int hashCode() {
        return 31 * (login != null ? login.hashCode() : 0) + (password != null ? password.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BasicAuthInfo && equals((BasicAuthInfo) obj);
    }

    public boolean equals(BasicAuthInfo bai) {
        return StringUtils.equals(this.login, bai.login) && StringUtils.equals(this.password, bai.password);
    }
}
