[
    classes: [
    ]
    factories: [
        memory: [
            provider: memory
            parameters: [
                shared: true
            ]
        ]
        file: [
            provider: file
            parameters: [
                path: "../../styx-data/database.st"
                indent: true
            ]
        ]
        derby_local: [
            provider: jdbc
            parameters: [
                connstr: "jdbc:derby:../../styx-data/database.derby.db;create=true"
                dialect: Derby
            ]
        ]
        sqlite_local: [
            provider: jdbc
            parameters: [
                connstr: "jdbc:sqlite:../../styx-data/database.sqlite.db"
                dialect: SQLite
            ]
        ]
        mysql_local: [
            provider: jdbc
            parameters: [
                connstr: "jdbc:mysql://localhost:3306/styx_test?user=test&password=test"
                dialect: MySQL
            ]
        ]
        mssql_isag: [
            provider: jdbc
            parameters: [
                connstr: "jdbc:jtds:sqlserver://SRV6030-CUST-01/styx_test;instance=SQL2005;user=agsb;password=agsbtest"
                dialect: MSSQL
            ]
        ]
        lmdb: [
            provider: lmdb
            parameters: [
                path: "../../styx-data/database.lmdb"
                readonly: false
                nosync: false
            ]
        ]
        mmap: [
            provider: mmap
            parameters: [
                path: "../../styx-data/database.mmap"
                size: 1000000000
            ]
        ]
    ]
]
