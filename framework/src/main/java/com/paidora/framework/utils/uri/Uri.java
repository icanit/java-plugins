package com.paidora.framework.utils.uri;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Класс, содержащий URI в виде составных частей.
 * <ul><li>[<code>scheme</code>:/][/[<code>username</code>[:<code>password</code>]@]<code>hostname</code>[:<code>port</code>]][/<code>path</code>][?<code>query</code>][#<code>fragment</code>]</li>
 * <li><code>scheme</code>:[/]<code>path</code>[?<code>query</code>][#<code>fragment</code>]</li></ul>
 */
public class Uri {
    private String scheme;
    private String username;
    private String password;
    private String hostname;
    private Integer port;
    private String path;
    private String query;
    private String fragment;

    private Map<String, String> queryProp;
    private boolean queryUpToDate;

    public Uri() {
    }

    public Uri(Uri uri) {
        this.scheme = uri.getScheme();
        this.username = uri.getUsername();
        this.password = uri.getPassword();
        this.hostname = uri.getHostname();
        this.port = uri.getPort();
        this.path = uri.path;
        this.query = uri.getQuery();
        this.queryProp = null;
        this.fragment = uri.getFragment();
    }

    /**
     * Генерирует объекты на основе текстового представления.
     * <p/>
     * [scheme<code>://</code>][username[<code>:</code>password]<code>@</code>]hostname[<code>:</code>port][<code>/</code>path][<code>?</code>query][<code>#</code>fragment]
     * <p/>
     * Парсер всеядный, сожрёт практически любую строку.
     *
     * @param uriStrings строки с url-ом.
     * @return Собранный объект.
     */
    public static Uri[] parseUris(String[] uriStrings) throws UriParsingException {
        Uri[] result = new Uri[uriStrings.length];
        UriParser uriParser = new UriParser();
        for (int i = 0; i < uriStrings.length; i++) {
            result[i] = uriParser.parse(uriStrings[i]);
        }
        return result;
    }

    /**
     * Генерирует объект на основе текстового представления.
     * <p/>
     * [scheme<code>://</code>][username[<code>:</code>password]<code>@</code>]hostname[<code>:</code>port][<code>/</code>path][<code>?</code>query][<code>#</code>fragment]
     * <p/>
     * Парсер всеядный, сожрёт практически любую строку.
     *
     * @param uriString строка с url-ом.
     * @return Собранный объект.
     */
    @SneakyThrows
    public static Uri parseUri(String uriString) {
        return new UriParser().parse(uriString);
    }

    /**
     * Собирает url query из значений карты и урлэнкодит ключи и параметры в UTF-8.
     * <p/>
     * В карте не должно быть null-ключей.
     *
     * @param map
     * @return query
     */
    public static String compileQuery(Map<String, String> map) {
        try {
            return compileQuery(map, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Собирает url query из значений карты и урлэнкодит ключи и параметры в UTF-8.
     * <p/>
     * В карте не должно быть null-ключей.
     *
     * @param map
     * @return query
     */
    public static String compileQuerySkipKey(Map<String, String> map) {
        try {
            return compileQuerySkipKey(map, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Собирает url query из значений карты и урлэнкодит ключи и параметры в указанной кодировке.
     * <p/>
     * В карте не должно быть null-ключей.
     *
     * @param map
     * @param charsetName
     * @return query
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings({"StringBufferWithoutInitialCapacity"})
    public static String compileQuery(Map<String, String> map, String charsetName) throws UnsupportedEncodingException {
        if (map == null || map.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() != 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(entry.getKey(), charsetName));
            String value = entry.getValue();
            if (value != null) {
                sb.append('=').append(URLEncoder.encode(value, charsetName));
            }
        }
        return sb.toString();
    }

    /**
     * Собирает url query из значений карты и урлэнкодит ключи и параметры в указанной кодировке.
     * <p/>
     * В карте не должно быть null-ключей.
     *
     * @param map
     * @param charsetName
     * @return query
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings({"StringBufferWithoutInitialCapacity"})
    public static String compileQuerySkipKey(Map<String, String> map, String charsetName) throws UnsupportedEncodingException {
        if (map.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() != 0) {
                sb.append('&');
            }
            sb.append(entry.getKey());
            String value = entry.getValue();
            if (value != null) {
                sb.append('=').append(URLEncoder.encode(value, charsetName));
            }
        }
        return sb.toString();
    }

    /**
     * Разбирает url query в карту.
     * <p/>
     * Правила такие. Каждый элемент: ключ=значение. Если знака равно нету,
     * то значение нулл. Если ключ повторяется, то значения складываются через
     * точку с запятой. Элементы разделяются амперсандом.
     * <p/>
     * Ключи и элементы урл-декодятся из UTF-8.
     *
     * @param query
     * @return карта
     */
    public static Map<String, String> parseQuery(String query) throws UriParsingException {
        try {
            return parseQuery(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Разбирает url query в карту.
     * <p/>
     * Правила такие. Каждый элемент: ключ=значение. Если знака равно нету,
     * то значение нулл. Если ключ повторяется, то значения складываются через
     * точку с запятой. Элементы разделяются амперсандом.
     * <p/>
     * Ключи и элементы урл-декодятся из указанной кодировки.
     *
     * @param query
     * @param charsetName
     * @return карта
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> parseQuery(String query, String charsetName) throws UnsupportedEncodingException, UriParsingException {
        Map<String, String> map = new LinkedHashMap<String, String>();
        if (query != null) {
            StringTokenizer st = new StringTokenizer(query, "?&");
            while (st.hasMoreTokens()) {
                String line = st.nextToken();
                int pos = line.indexOf((int) '=');
                if (pos == -1) {
                    String title = URLDecoder.decode(line, charsetName);
                    String prev = map.put(title, null);
                    if (prev != null) {
                        map.put(title, prev);
                    }
                } else {
                    String title;
                    try {
                        title = URLDecoder.decode(line.substring(0, pos), charsetName);
                    } catch (Exception e) {
                        throw new UriParsingException("Error URL-decoding query item title " + line.substring(0, pos), e);
                    }
                    String value;
                    try {
                        value = URLDecoder.decode(line.substring(pos + 1), charsetName);
                    } catch (Exception e) {
                        throw new UriParsingException("Error URL-decoding query item titled " + title + ' ' + line.substring(pos + 1), e);
                    }
                    String prev = map.put(title, value);
                    if (prev != null) {
                        throw new UriParsingException("Duplicate query parameter " + title
                                + ", specified values " + prev + " and then " + value);
                    }
                }
            }
        }
        return map;
    }

    public Uri reset() {
        set(null, null, null, null, null, null, null, null);
        return this;
    }

    public Uri set(String scheme,
                   String username, String password, String hostname, Integer port,
                   String path, String query, String anchor) {
        this.scheme = scheme;
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.port = port;
        this.path = path;
        this.query = query;
        this.queryProp = null;
        this.fragment = anchor;
        return this;
    }

    public boolean hasScheme() {
        return scheme != null;
    }

    public String getScheme() {
        return scheme;
    }

    public Uri setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public boolean hasUsername() {
        return username != null;
    }

    public String getUsername() {
        return username;
    }

    public Uri setUsername(String username) {
        this.username = username;
        return this;
    }

    public boolean hasPassword() {
        return password != null;
    }

    public String getPassword() {
        return password;
    }

    public Uri setPassword(String password) {
        this.password = password;
        return this;
    }

    public boolean hasHostname() {
        return hostname != null;
    }

    public String getHostname() {
        return hostname;
    }

    public Uri setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public boolean hasPort() {
        return port != null;
    }

    public Integer getPort() {
        return port;
    }

    public Uri setPort(Integer port) {
        this.port = port;
        return this;
    }

    public int getPortIntValue() {
        return port.intValue();
    }

    public int getPortIntValue(int defaultValue) {
        return hasPort() ? port.intValue() : defaultValue;
    }

    public boolean hasPath() {
        return path != null;
    }

    public String getPath() {
        return path;
    }

    public Uri setPath(String path) {
        this.path = path;
        return this;
    }

    public Uri setRelativePath(String path) {
        if (path == null) {
            return this;
        }
        if (this.path != null) {
            if (!path.startsWith("/") && !this.path.endsWith("/")) {
                this.path += "/" + path;
            } else {
                this.path += path;
            }
        } else {

            this.path = path;
        }
        return this;
    }

    public boolean hasQuery() {
        if (queryProp != null && !queryUpToDate) {
            return !queryProp.isEmpty();
        } else {
            return query != null;
        }
    }

    /**
     * Возвращает откомпилированный в строчку запрос.
     *
     * @return компилированный запрос
     * @see #parseQuery(String)
     * @see #parseQuery(String, String)
     */
    public String getQuery() {
        if (queryProp != null && !queryUpToDate) {
            query = compileQuery(queryProp);
            queryUpToDate = true;
        }
        return query;
    }

    /**
     * Устанавливает запрос.
     *
     * @param query запрос
     * @see #compileQuery(Map)
     * @see #compileQuery(Map, String)
     */
    public Uri setQuery(String query) {
        this.query = query;
        this.queryProp = null;
        return this;
    }

    public Map<String, String> getQueryMap() throws UriParsingException {
        if (queryProp == null) {
            queryProp = new HashMap<>(parseQuery(query));
        }
        queryUpToDate = false;
        return queryProp;
    }

    public Uri setQueryMap(Map<String, String> queryParams) {
        this.query = Uri.compileQuery(queryParams);
        this.queryProp = null;
        return this;
    }

    public String getFile() {
        return hasQuery() ? getPath() + '?' + getQuery() : getPath();
    }

    public boolean hasFragment() {
        return fragment != null;
    }

    public String getFragment() {
        return fragment;
    }

    public Uri setFragment(String anchor) {
        this.fragment = anchor;
        return this;
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof Uri && equals((Uri) that);
    }

    /**
     * Сравнивает два Uri-объекта. Они равны, если все их соответствующие поля либо оба null, либо равны друг другу.
     * <p/>
     * Все поля сравниваются с учётом регистра. Query сравнивается в виде строки,
     * так что различное расположение одних и тех же аргументов при прочих равных не даст равенства Uri.
     *
     * @param that
     * @return
     */
    public boolean equals(Uri that) {
        return this == that || that != null &&
                StringUtils.equals(this.getScheme(), that.getScheme())
                && StringUtils.equals(this.getUsername(), that.getUsername())
                && StringUtils.equals(this.getPassword(), that.getPassword())
                && StringUtils.equals(this.getHostname(), that.getHostname())
                && Objects.equals(this.getPort(), that.getPort())
                && StringUtils.equals(this.getPath(), that.getPath())
                && StringUtils.equals(this.getQuery(), that.getQuery())
                && StringUtils.equals(this.getFragment(), that.getFragment());

    }

    /**
     * Проверяет, указывает ли переданный ури на тот же сервер.
     * <p/>
     * Возвращает true, соответствующие схемы, хосты и порты либо оба null, либо равны друг другу.
     *
     * @param that
     * @return true, если переданный ури указывает на тот же сервер, иначе false
     */
    public boolean isSameServer(Uri that) {
        return this == that || that != null
                && StringUtils.equals(this.scheme, that.scheme)
                && StringUtils.equals(this.hostname, that.hostname)
                && Objects.equals(this.port, that.port);
    }

    @Override
    public int hashCode() {
        int result = getScheme() != null ? getScheme().hashCode() : 0;
        result = 31 * result + (getUsername() != null ? getUsername().hashCode() : 0);
        result = 31 * result + (getPassword() != null ? getPassword().hashCode() : 0);
        result = 31 * result + (getHostname() != null ? getHostname().hashCode() : 0);
        result = 31 * result + (getPort() != null ? getPort().hashCode() : 0);
        result = 31 * result + (getPath() != null ? getPath().hashCode() : 0);
        result = 31 * result + (getQuery() != null ? getQuery().hashCode() : 0);
        result = 31 * result + (getFragment() != null ? getFragment().hashCode() : 0);
        return result;
    }

    /**
     * Возвращает форматированный урл. Хоть в браузер суй.
     *
     * @return форматированный урл
     */
    @SuppressWarnings({"StringBufferWithoutInitialCapacity"})
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (hasScheme()) {
            sb.append(getScheme()).append(':');
        }
        if (hasHostname()) {
            if (hasScheme()) {
                sb.append("//");
            }
            if (hasUsername()) {
                sb.append(getUsername());
                if (hasPassword()) {
                    sb.append(":(hidden)");
                }
                sb.append('@');
            }
            sb.append(getHostname());
            if (hasPort()) {
                sb.append(':').append(getPort());
            }
        }
        if (hasPath()) {
            if (hasHostname() && !getPath().startsWith("/")) {
                sb.append('/');
            }
            sb.append(getPath());
        }
        if (hasQuery()) {
            sb.append('?').append(getQuery());
        }
        if (hasFragment()) {
            sb.append('#').append(getFragment());
        }
        return sb.toString();
    }
}
