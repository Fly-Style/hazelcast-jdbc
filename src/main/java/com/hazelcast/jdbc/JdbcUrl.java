/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.jdbc;

import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JdbcUrl {

    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("^jdbc:hazelcast://"
            + "(?<host>\\S+?(?=[:/]))(:(?<port>\\d+))?"
            + "/(?<schema>\\S+?)"
            + "(\\?(?<parameters>\\S*))?$");

    private final String host;
    private int port = -1;
    private final String schema;
    private Properties properties = new Properties();
    private final String rawUrl;

    private JdbcUrl(String host, String schema, String rawUrl) {
        this.host = host;
        this.schema = schema;
        this.rawUrl = rawUrl;
    }

    public String getAuthority() {
        if (port == -1) {
            return host;
        }
        return host + ":" + port;
    }

    public String getSchema() {
        return schema;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JdbcUrl jdbcUrl = (JdbcUrl) o;
        return Objects.equals(properties, jdbcUrl.properties) && Objects.equals(rawUrl, jdbcUrl.rawUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties, rawUrl);
    }

    private void parseProperties(String parametersString) {
        if (parametersString == null) {
            return;
        }
        for (String parameter : parametersString.split("&")) {
            String[] paramAndValue = parameter.split("=");
            if (paramAndValue.length != 2) {
                continue;
            }
            properties.setProperty(paramAndValue[0], paramAndValue[1]);
        }
    }

    static boolean acceptsUrl(String url) {
        return JDBC_URL_PATTERN.matcher(url).matches();
    }

    static JdbcUrl valueOf(String url, Properties info) {
        Matcher matcher = JDBC_URL_PATTERN.matcher(url);
        if (!matcher.matches()) {
            return null;
        }
        JdbcUrl jdbcUrl = new JdbcUrl(matcher.group("host"), matcher.group("schema"), url);
        if (info != null) {
            jdbcUrl.properties = info;
        }
        String port = matcher.group("port");
        if (port != null) {
            jdbcUrl.port = Integer.parseInt(port);
        }
        jdbcUrl.parseProperties(matcher.group("parameters"));
        return jdbcUrl;
    }
}
