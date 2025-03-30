package com.paidora.framework.utils.uri;


/**
 * Парсит URI.
 * <p/>
 * <ul><li>[protocol://][username[:password]@]hostname[:port][/path][?query][#fragment]</li>
 * <li>protocol:[/]path[?query][#fragment]</li>
 */
public class UriParser {

    private String source;
    private Uri uri;
    private int begin;
    private int end;

    private void reset(String source) {
        this.source = source;
        this.uri = new Uri();
        this.begin = 0;
        this.end = source.length();
    }

    /**
     * Сокращённые схемы типа "foo.com:80/path" воспринимает как название сайта без протокола.
     *
     * @param source
     * @return
     * @throws UriParsingException
     */
    public Uri parseWithAuthority(String source) throws UriParsingException {
        return parse(source, true);
    }

    public Uri parse(String source) throws UriParsingException {
        return parse(source, false);
    }

    private Uri parse(String source, boolean considerSchemelessAuthority) throws UriParsingException {
        if (source == null) {
            return null;
        }
        try {
            reset(source);

            parseFragment();
            parseQuery();

            {
                int colonSlashSlashPos = source.indexOf("://", begin);
                if (colonSlashSlashPos >= begin && colonSlashSlashPos < end) {
                    // вариант "а"
                    parseAuthorityWithScheme(colonSlashSlashPos);
                    return uri;
                }
            }

            {
                int colonSlashPos = source.indexOf(":/", begin);
                if (colonSlashPos >= begin && colonSlashPos < end) {
                    // вариант "б"
                    parseWithoutAuthority(colonSlashPos);
                    return uri;
                }
            }

            if (considerSchemelessAuthority) {
                parseAuthorityWithoutScheme();
                return uri;
            }

            {
                int colonPos = source.indexOf((int) ':', begin);
                if (colonPos >= begin && colonPos < end) {
                    parseWithoutAuthority(colonPos);
                } else {
                    parseAuthorityWithoutScheme();
                }
                return uri;
            }

        } catch (Throwable e) {
            throw new UriParsingException("Error parsing URI string " + source, e);
        }
    }

    private void parseAuthorityWithScheme(int colonSlashSlashPos) {
        String protocol = source.substring(begin, colonSlashSlashPos);
        uri.setScheme(protocol);
        begin = colonSlashSlashPos + 3;
        parseAuthorityWithoutScheme();
    }

    private void parseWithoutAuthority(int colonPos) {
        String protocol = source.substring(begin, colonPos);
        uri.setScheme(protocol);
        begin = colonPos + 1;

        String path = source.substring(begin, end);
        uri.setPath(path);
    }

    private void parseAuthorityWithoutScheme() {
        int dogPos = source.lastIndexOf((int) '@', end);
        if (dogPos >= begin) {
            int colonPos = source.lastIndexOf((int) ':', dogPos);
            if (colonPos >= begin) {
                uri.setUsername(source.substring(begin, colonPos));
                uri.setPassword(source.substring(colonPos + 1, dogPos));
            } else {
                uri.setUsername(source.substring(begin, dogPos));
            }
            begin = dogPos + 1;
        }

        int slashPos = source.indexOf((int) '/', begin);
        if (slashPos > 0 && slashPos <= end) {
            String path = source.substring(slashPos, end);
            uri.setPath(path);
            end = slashPos;
        }

        int commaPos = source.lastIndexOf((int) ':', end);
        if (commaPos >= begin) {
            Integer port = Integer.valueOf(source.substring(commaPos + 1, end));
            uri.setPort(port);
            end = commaPos;
        }

        uri.setHostname(source.substring(begin, end));
    }

    private void parseFragment() {
        int hashPos = source.indexOf((int) '#', begin);
        if (hashPos >= begin && hashPos < end) {
            String fragment = source.substring(hashPos + 1, end);
            uri.setFragment(fragment);
            end = hashPos;
        }
    }

    private void parseQuery() {
        int qmPos = source.indexOf((int) '?', begin);
        if (qmPos >= begin && qmPos < end) {
            String query = source.substring(qmPos + 1, end);
            uri.setQuery(query);
            end = qmPos;
        }
    }
}
