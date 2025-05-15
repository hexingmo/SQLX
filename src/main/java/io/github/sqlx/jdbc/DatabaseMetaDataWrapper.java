/*
 *    Copyright 2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.sqlx.jdbc;

import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

/**
 * @author He Xing Mo
 * @since 1.0
 */

public class DatabaseMetaDataWrapper extends WrapperAdapter implements DatabaseMetaData {

    @Getter
    @Setter
    private DatabaseMetaData delegate;

    private final Connection connection;

    public DatabaseMetaDataWrapper(DatabaseMetaData delegate , Connection connection) {
        this.delegate = delegate;
        this.connection = connection;
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return delegate != null && delegate.allProceduresAreCallable();
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return delegate != null && delegate.allTablesAreSelectable();
    }

    @Override
    public String getURL() throws SQLException {
        return delegate != null ? delegate.getURL() : "jdbc:sqlx:~";
    }

    @Override
    public String getUserName() throws SQLException {
        return delegate != null ? delegate.getUserName() : null;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return delegate != null && delegate.isReadOnly();
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return delegate != null && delegate.nullsAreSortedHigh();
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return delegate != null && delegate.nullsAreSortedLow();
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return delegate != null && delegate.nullsAreSortedAtStart();
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return delegate != null && delegate.nullsAreSortedAtEnd();
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return delegate != null ? delegate.getDatabaseProductName() : "SQLX Multiple DataSource Proxy";
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return delegate != null ? delegate.getDatabaseProductVersion() : null;
    }

    @Override
    public String getDriverName() throws SQLException {
        return delegate != null ? delegate.getDriverName() : null;
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return delegate != null ? delegate.getDriverVersion() : null;
    }

    @Override
    public int getDriverMajorVersion() {
        return delegate != null ? delegate.getDriverMajorVersion() : -1;
    }

    @Override
    public int getDriverMinorVersion() {
        return delegate != null ? delegate.getDriverMinorVersion() : -1;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        return delegate != null && delegate.usesLocalFiles();
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return delegate != null && delegate.usesLocalFilePerTable();
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return delegate != null && delegate.supportsMixedCaseIdentifiers();
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return delegate != null && delegate.storesUpperCaseIdentifiers();
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return delegate != null && delegate.storesLowerCaseIdentifiers();
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return delegate != null && delegate.storesMixedCaseIdentifiers();
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return delegate != null && delegate.supportsMixedCaseQuotedIdentifiers();
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return delegate != null && delegate.storesUpperCaseQuotedIdentifiers();
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return delegate != null && delegate.storesLowerCaseQuotedIdentifiers();
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return delegate != null && delegate.storesMixedCaseQuotedIdentifiers();
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        return delegate != null ? delegate.getIdentifierQuoteString() : null;
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        return delegate != null ? delegate.getSQLKeywords() : null;
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        return delegate != null ? delegate.getNumericFunctions() : null;
    }

    @Override
    public String getStringFunctions() throws SQLException {
        return delegate != null ? delegate.getStringFunctions() : null;
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        return delegate != null ? delegate.getSystemFunctions() : null;
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        return delegate != null ? delegate.getTimeDateFunctions() : null;
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        return delegate != null ? delegate.getSearchStringEscape() : null;
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        return delegate != null ? delegate.getExtraNameCharacters() : null;
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return delegate != null && delegate.supportsAlterTableWithAddColumn();
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return delegate != null && delegate.supportsAlterTableWithDropColumn();
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return delegate != null && delegate.supportsColumnAliasing();
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return delegate != null && delegate.nullPlusNonNullIsNull();
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        return delegate != null && delegate.supportsConvert();
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        return delegate != null && delegate.supportsConvert();
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return delegate != null && delegate.supportsTableCorrelationNames();
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return delegate != null && delegate.supportsDifferentTableCorrelationNames();
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return delegate != null && delegate.supportsExpressionsInOrderBy();
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return delegate != null && delegate.supportsOrderByUnrelated();
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        return delegate != null && delegate.supportsGroupBy();
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return delegate != null && delegate.supportsGroupByUnrelated();
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return delegate != null && delegate.supportsGroupByBeyondSelect();
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return delegate != null && delegate.supportsLikeEscapeClause();
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return delegate != null && delegate.supportsMultipleResultSets();
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return delegate != null && delegate.supportsMultipleTransactions();
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return delegate != null && delegate.supportsNonNullableColumns();
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return delegate != null && delegate.supportsMinimumSQLGrammar();
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return delegate != null && delegate.supportsCoreSQLGrammar();
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return delegate != null && delegate.supportsExtendedSQLGrammar();
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return delegate != null && delegate.supportsANSI92EntryLevelSQL();
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return delegate != null && delegate.supportsANSI92IntermediateSQL();
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return delegate != null && delegate.supportsANSI92FullSQL();
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return delegate != null && delegate.supportsIntegrityEnhancementFacility();
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return delegate != null && delegate.supportsOuterJoins();
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return delegate != null && delegate.supportsFullOuterJoins();
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return delegate != null && delegate.supportsLimitedOuterJoins();
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        return delegate != null ? delegate.getSchemaTerm() : null;
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        return delegate != null ? delegate.getProcedureTerm() : null;
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        return delegate != null ? delegate.getCatalogTerm() : null;
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return delegate != null && delegate.isCatalogAtStart();
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        return delegate != null ? delegate.getCatalogSeparator() : null;
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return delegate != null && delegate.supportsSchemasInDataManipulation();
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return delegate != null && delegate.supportsSchemasInProcedureCalls();
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return delegate != null && delegate.supportsSchemasInTableDefinitions();
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return delegate != null && delegate.supportsSchemasInIndexDefinitions();
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return delegate != null && delegate.supportsSchemasInPrivilegeDefinitions();
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return delegate != null && delegate.supportsCatalogsInDataManipulation();
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return delegate != null && delegate.supportsCatalogsInProcedureCalls();
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return delegate != null && delegate.supportsCatalogsInTableDefinitions();
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return delegate != null && delegate.supportsCatalogsInIndexDefinitions();
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return delegate != null && delegate.supportsCatalogsInPrivilegeDefinitions();
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return delegate != null && delegate.supportsPositionedDelete();
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return delegate != null && delegate.supportsPositionedUpdate();
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return delegate != null && delegate.supportsSelectForUpdate();
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return delegate != null && delegate.supportsStoredProcedures();
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return delegate != null && delegate.supportsSubqueriesInComparisons();
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return delegate != null && delegate.supportsSubqueriesInExists();
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return delegate != null && delegate.supportsSubqueriesInIns();
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return delegate != null && delegate.supportsSubqueriesInQuantifieds();
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return delegate != null && delegate.supportsCorrelatedSubqueries();
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        return delegate != null && delegate.supportsUnion();
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        return delegate != null && delegate.supportsUnionAll();
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return delegate != null && delegate.supportsOpenCursorsAcrossCommit();
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return delegate != null && delegate.supportsOpenCursorsAcrossRollback();
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return delegate != null && delegate.supportsOpenStatementsAcrossCommit();
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return delegate != null && delegate.supportsOpenStatementsAcrossRollback();
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return delegate != null ? delegate.getMaxBinaryLiteralLength() : -1;
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return delegate != null ? delegate.getMaxCharLiteralLength() : -1;
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return delegate != null ? delegate.getMaxColumnNameLength() : -1;
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return delegate != null ? delegate.getMaxColumnsInGroupBy() : -1;
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return delegate != null ? delegate.getMaxColumnsInIndex() : -1;
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return delegate != null ? delegate.getMaxColumnsInOrderBy() : -1;
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return delegate != null ? delegate.getMaxColumnsInSelect() : -1;
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return delegate != null ? delegate.getMaxColumnsInTable() : -1;
    }

    @Override
    public int getMaxConnections() throws SQLException {
        return delegate != null ? delegate.getMaxConnections() : -1;
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return delegate != null ? delegate.getMaxCursorNameLength() : -1;
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        return delegate != null ? delegate.getMaxIndexLength() : -1;
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return delegate != null ? delegate.getMaxSchemaNameLength() : -1;
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return delegate != null ? delegate.getMaxProcedureNameLength() : -1;
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return delegate != null ? delegate.getMaxCatalogNameLength() : -1;
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        return delegate != null ? delegate.getMaxRowSize() : -1;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return delegate != null && delegate.doesMaxRowSizeIncludeBlobs();
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        return delegate != null ? delegate.getMaxStatementLength() : -1;
    }

    @Override
    public int getMaxStatements() throws SQLException {
        return delegate != null ? delegate.getMaxStatements() : -1;
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        return delegate != null ? delegate.getMaxTableNameLength() : -1;
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return delegate != null ? delegate.getMaxTablesInSelect() : -1;
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        return delegate != null ? delegate.getMaxUserNameLength() : -1;
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return delegate != null ? delegate.getDefaultTransactionIsolation() : -1;
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        return delegate != null && delegate.supportsTransactions();
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        return delegate != null && delegate.supportsTransactionIsolationLevel(level);
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return delegate != null && delegate.supportsDataDefinitionAndDataManipulationTransactions();
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return delegate != null && delegate.supportsDataManipulationTransactionsOnly();
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return delegate != null && delegate.dataDefinitionCausesTransactionCommit();
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return delegate != null && delegate.dataDefinitionIgnoredInTransactions();
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        return delegate != null ? delegate.getProcedures(catalog , schemaPattern , procedureNamePattern) : null;
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        return delegate != null ? delegate.getProcedureColumns(catalog , schemaPattern , procedureNamePattern , columnNamePattern) : null;
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        return delegate != null ? delegate.getTables(catalog , schemaPattern , tableNamePattern , types) : null;
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        return delegate != null ? delegate.getSchemas() : null;
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        return delegate != null ? delegate.getCatalogs() : null;
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        return delegate != null ? delegate.getTableTypes() : null;
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return delegate != null ? delegate.getColumns(catalog , schemaPattern , tableNamePattern , columnNamePattern) : null;
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        return delegate != null ? delegate.getColumnPrivileges(catalog , schema , table , columnNamePattern) : null;
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return delegate != null ? delegate.getTablePrivileges(catalog , schemaPattern , tableNamePattern) : null;
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
        return delegate != null ? delegate.getBestRowIdentifier(catalog , schema , table , scope , nullable) : null;
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        return delegate != null ? delegate.getVersionColumns(catalog , schema , table) : null;
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        return delegate != null ? delegate.getPrimaryKeys(catalog , schema , table) : null;
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        return delegate != null ? delegate.getImportedKeys(catalog , schema , table) : null;
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        return delegate != null ? delegate.getExportedKeys(catalog , schema , table) : null;
    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        return delegate != null ? delegate.getCrossReference(parentCatalog , parentSchema , parentTable , foreignCatalog , foreignSchema , foreignTable) : null;
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        return delegate != null ? delegate.getTypeInfo() : null;
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        return delegate != null ? delegate.getIndexInfo(catalog , schema , table , unique , approximate) : null;
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        return delegate != null && delegate.supportsResultSetType(type);
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        return delegate != null && delegate.supportsResultSetConcurrency(type , concurrency);
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return delegate != null && delegate.ownUpdatesAreVisible(type);
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return delegate != null && delegate.ownDeletesAreVisible(type);
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return delegate != null && delegate.ownInsertsAreVisible(type);
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return delegate != null && delegate.othersUpdatesAreVisible(type);
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return delegate != null && delegate.othersDeletesAreVisible(type);
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return delegate != null && delegate.othersInsertsAreVisible(type);
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        return delegate != null && delegate.updatesAreDetected(type);
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        return delegate != null && delegate.deletesAreDetected(type);
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        return delegate != null && delegate.insertsAreDetected(type);
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return delegate != null && delegate.supportsBatchUpdates();
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        return delegate != null ? delegate.getUDTs(catalog , schemaPattern , typeNamePattern , types) : null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.connection;
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        return delegate != null && delegate.supportsSavepoints();
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return delegate != null && delegate.supportsNamedParameters();
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return delegate != null && delegate.supportsMultipleOpenResults();
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return delegate != null && delegate.supportsGetGeneratedKeys();
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        return delegate != null ? delegate.getSuperTypes(catalog , schemaPattern , typeNamePattern) : null;
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return delegate != null ? delegate.getSuperTables(catalog , schemaPattern , tableNamePattern) : null;
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
        return delegate != null ? delegate.getAttributes(catalog , schemaPattern , typeNamePattern , attributeNamePattern) : null;
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return delegate != null && delegate.supportsResultSetHoldability(holdability);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return delegate != null ? delegate.getResultSetHoldability() : -1;
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return delegate != null ? delegate.getDatabaseMajorVersion() : -1;
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return delegate != null ? delegate.getDatabaseMinorVersion() : -1;
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return delegate != null ? delegate.getJDBCMajorVersion() : -1;
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return delegate != null ? delegate.getJDBCMinorVersion() : -1;
    }

    @Override
    public int getSQLStateType() throws SQLException {
        return delegate != null ? delegate.getSQLStateType() : DatabaseMetaData.sqlStateSQL;
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return delegate != null && delegate.locatorsUpdateCopy();
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return delegate != null && delegate.supportsStatementPooling();
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return delegate != null ? delegate.getRowIdLifetime() : null;
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        return delegate != null ? delegate.getSchemas(catalog , schemaPattern) : null;
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return delegate != null && delegate.supportsStoredFunctionsUsingCallSyntax();
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return delegate != null && delegate.autoCommitFailureClosesAllResultSets();
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return delegate != null ? delegate.getClientInfoProperties() : null;
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        return delegate != null ? delegate.getFunctions(catalog , schemaPattern , functionNamePattern) : null;
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        return delegate != null ? delegate.getFunctionColumns(catalog , schemaPattern , functionNamePattern , columnNamePattern) : null;
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return delegate != null ? delegate.getPseudoColumns(catalog , schemaPattern , tableNamePattern , columnNamePattern) : null;
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return delegate != null && delegate.generatedKeyAlwaysReturned();
    }
}
