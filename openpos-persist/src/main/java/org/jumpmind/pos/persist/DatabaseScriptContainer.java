package org.jumpmind.pos.persist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.db.platform.h2.H2DatabasePlatform;
import org.jumpmind.db.platform.oracle.OracleDatabasePlatform;
import org.jumpmind.db.sql.ISqlTemplate;
import org.jumpmind.db.sql.SqlScript;
import org.jumpmind.exception.IoException;
import org.jumpmind.symmetric.io.data.DbImport;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseScriptContainer {
    protected final Log logger = LogFactory.getLog(getClass());

    final static String IMPORT_PREFIX = "-- import:";

    private List<DatabaseScript> preInstallScripts = new ArrayList<DatabaseScript>();
    private List<DatabaseScript> postInstallScripts = new ArrayList<DatabaseScript>();

    private JdbcTemplate jdbcTemplate;
    private IDatabasePlatform platform;

    private Map<String, String> replacementTokens;

    private String scriptLocation;

    public DatabaseScriptContainer(String scriptLocation, IDatabasePlatform platform) {
        try {
            this.scriptLocation = scriptLocation;
            this.platform = platform;
            this.jdbcTemplate = new JdbcTemplate(platform.getDataSource());

            replacementTokens = new HashMap<String, String>();
            // Add any replacement tokens

            Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader())
                    .getResources(String.format("classpath*:%s/*.*", scriptLocation));
            for (Resource r : resources) {
                DatabaseScript script = new DatabaseScript(r.getFilename());
                script.setResource(r);

                if (script.getWhen() == DatabaseScript.WHEN_PREINSTALL) {
                    preInstallScripts.add(script);
                } else if (script.getWhen() == DatabaseScript.WHEN_POSTINSTALL) {
                    postInstallScripts.add(script);
                }
            }
        } catch (IOException e) {
            throw new IoException(e);
        }
    }

    public void executePreInstallScripts(String fromVersion, String toVersion, boolean failOnError) {
        executeScripts(fromVersion, toVersion, this.preInstallScripts, failOnError);
    }

    public void executePostInstallScripts(String fromVersion, String toVersion, boolean failOnError) {
        executeScripts(fromVersion, toVersion, this.postInstallScripts, failOnError);
    }

    public void executeScripts(String fromVersion, String toVersion, List<DatabaseScript> scripts, boolean failOnError) {
        if (scripts != null) {
            Collections.sort(scripts);
            DatabaseScript from = new DatabaseScript();
            from.parseVersion(fromVersion);

            DatabaseScript to = new DatabaseScript();
            to.parseVersion(toVersion);

            for (DatabaseScript s : scripts) {
                if (isDatabaseMatch(s) && ((s.compareVersionTo(from) > 0 && s.compareVersionTo(to) <= 0) || s.getMajor() == 999)) {
                    try {
                        executeImports(s, s.getResource(), failOnError);
                        execute(s, s.getResource(), failOnError);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    protected void executeImports(DatabaseScript databaseScript, Resource resource, boolean failOnError) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {

            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith(IMPORT_PREFIX)) {
                    String file = line.substring(IMPORT_PREFIX.length()).trim();
                    Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader())
                            .getResources(String.format("classpath*:%s/%s", scriptLocation, file));
                    for (Resource resource2 : resources) {
                        execute(databaseScript, resource2, failOnError);
                    }
                }
                line = reader.readLine();
            }
        }
    }

    public void execute(DatabaseScript databaseScript, final Resource resource, boolean failOnError) throws IOException {
        String compareString = resource.getFilename().toLowerCase();
        if (compareString.endsWith(".sql")) {
            loadSql(resource.getURL(), failOnError);
        } else if (compareString.endsWith(".csv")) {
            loadCsv(databaseScript, resource, failOnError);
        }
    }

    void loadSql(URL script, boolean failOnError) {
        logger.info("Executing script " + script.toString());
        jdbcTemplate.execute(new ConnectionCallback<Object>() {
            public Object doInConnection(Connection c) throws SQLException, DataAccessException {
                ISqlTemplate template = platform.getSqlTemplate();
                SqlScript sqlscript = new SqlScript(script, template, true, ";", replacementTokens);
                sqlscript.setFailOnSequenceCreate(true);
                sqlscript.setFailOnDrop(false);
                sqlscript.setFailOnError(true);
                try {
                    sqlscript.execute();
                } catch (Exception ex) {
                    String message = "Failed to execute script: " + script;
                    if (failOnError) {
                        throw new SQLException(message, ex);
                    }
                    logger.warn(message, ex);
                }
                return null;
            }
        });
    }

    void loadCsv(DatabaseScript script, Resource resource, boolean failOnError) throws IOException {
        logger.info("Loading file " + script.toString());
        try (InputStream is = resource.getInputStream()) {
            DbImport importer = new DbImport(platform);
            importer.setFormat(DbImport.Format.CSV);
            importer.setCommitRate(1000);
            importer.setForceImport(!failOnError);
            importer.setAlterCaseToMatchDatabaseDefaultCase(true);
            importer.importTables(is, script.getDescription().replaceAll("-", "_"));
        }
    }

    public boolean isDatabaseMatch(DatabaseScript script) {
        if (script.getDescription().equals("H2Only")) {
            return platform instanceof H2DatabasePlatform;
        } else if (script.getDescription().equals("OracleOnly")) {
            return platform instanceof OracleDatabasePlatform;
        } else {
            return true;
        }
    }
}
