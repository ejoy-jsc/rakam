web: java $JAVA_OPTS -cp rakam/target/classes:rakam/target/dependency/* -Dstore.adapter=postgresql -Dstore.adapter.postgresql.url=${DATABASE_URL} -Dhttp.server.address=0.0.0.0:${PORT} -Dplugin.geoip.enabled=${RAKAM_GEOIP_ENABLE} -Dstore.adapter=postgresql -Duser.storage.identifier_column=id -Dplugin.user.mailbox.enable=true -Dplugin.user.mailbox.adapter=postgresql org.rakam.ServiceStarter