package com.jazzcinema.app.database

internal object DbConfig {
    private const val HOST     = "sql12.freesqldatabase.com"
    private const val PORT     = 3306
    private const val DATABASE = "sql12824264"
    const val USER             = "sql12824264"
    const val PASSWORD         = "zx2faqegWz"

    const val JDBC_URL =
        "jdbc:mysql://$HOST:$PORT/$DATABASE" +
        "?useSSL=false" +
        "&allowPublicKeyRetrieval=true" +
        "&connectTimeout=15000" +
        "&socketTimeout=30000" +
        "&characterEncoding=UTF-8" +
        "&autoReconnect=true"
}
