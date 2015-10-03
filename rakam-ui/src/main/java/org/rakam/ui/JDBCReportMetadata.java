package org.rakam.ui;

import com.google.inject.name.Named;
import org.rakam.analysis.JDBCPoolDataSource;
import org.rakam.util.JsonHelper;
import org.rakam.util.RakamException;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public class JDBCReportMetadata {
    private final DBI dbi;

    ResultSetMapper<Report> mapper = (index, r, ctx) ->
            new Report(r.getString(1), r.getString(2),r.getString(3), r.getString(4), JsonHelper.read(r.getString(5), Map.class));

    @Inject
    public JDBCReportMetadata(@Named("report.metadata.store.jdbc") JDBCPoolDataSource dataSource) {
        dbi = new DBI(dataSource);
        setup();
    }

    public void setup() {
        try(Handle handle = dbi.open()) {
            handle.createStatement("CREATE TABLE IF NOT EXISTS reports (" +
                    "  project VARCHAR(255) NOT NULL," +
                    "  slug VARCHAR(255) NOT NULL," +
                    "  name VARCHAR(255) NOT NULL," +
                    "  query TEXT NOT NULL," +
                    "  options TEXT," +
                    "  PRIMARY KEY (project, slug)" +
                    "  )")
                    .execute();
        }
    }

    public List<Report> getReports(String project) {
        try(Handle handle = dbi.open()) {
            return handle.createQuery("SELECT project, slug, name, query, options FROM reports WHERE project = :project")
                    .bind("project", project).map(mapper).list();
        }
    }

    public void delete(String project, String name) {
        try(Handle handle = dbi.open()) {
            handle.createStatement("DELETE FROM reports WHERE project = :project AND name = :name")
                    .bind("project", project).bind("name", name).execute();
        }
    }

    public void save(Report report) {
        try(Handle handle = dbi.open()) {
            handle.createStatement("INSERT INTO reports (project, slug, name, query, options) VALUES (:project, :slug, :name, :query, :options)")
                    .bind("project", report.project)
                    .bind("name", report.name)
                    .bind("query", report.query)
                    .bind("slug", report.slug)
                    .bind("options", JsonHelper.encode(report.options, false))
                    .execute();
        } catch (UnableToExecuteStatementException e) {
            if(e.getCause() instanceof SQLException && ((SQLException) e.getCause()).getSQLState().equals("23505")) {
                // TODO: replace
                throw new RakamException("Report already exists", 400);
            }
        }
    }

    public Report get(String project, String slug) {
        try(Handle handle = dbi.open()) {
            return handle.createQuery("SELECT project, slug, name, query, options FROM reports WHERE project = :project AND slug = :slug")
                    .bind("project", project)
                    .bind("slug", slug).map(mapper).first();
        }
    }
}
