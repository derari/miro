/**
 * Database abstraction interfaces.
 * <p>
 * This is not so much for plugging different database technologies,
 * but to make it easier to insert layers of abstraction.
 */
package org.cthul.miro.db;

/*
 * 
 * EntityQuery <|- Mapped Views ------+
 *  v                                 |
 * QueryComposer <|- SqlTemplates     |
 * |                                  |
 * |   Results <|---------------------+
 * v    ^
 * Database Syntax Abstraction <|- SqlSyntax
 *  |
 * Database Connection Abstraction <|- JdbcConnection
 */
