/**
 * Database abstraction interfaces.
 * <p>
 * This is not so much for plugging different database technologies,
 * but to make it easier to insert layers of abstraction.
 */
package org.cthul.miro.db;

/*
 *                 AnnotationReader
 *                        v
 * EntityQuery <|- Mapped Views ------+
 *  v                                 |
 * QueryComposer <|- SqlTemplates     |
 * |                                  |
 * |   Results <|---------------------+
 * v    ^
 * Database Syntax Abstraction <|- SqlSyntax
 *  |
 * Database Connection Abstraction <|- JdbcConnection

  util/futures
  util/misc
  db/api
  db/jdbc
  syntax/api
  syntax/sql-ansi
  syntax/sql-mysql
  result/entity
  result/graph
  query/api
  query/sql
  mapping/api
  mapping/sql
  at/api
  at/query
  at/mapping
  it-test

 */
