package com.example.database.migration

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.example.database.tables.Users

object AddAdminField {
    fun migrate() {
        transaction {
            // Добавляем поле isAdmin, если его еще нет
            exec("""
                DO $$ 
                BEGIN 
                    IF NOT EXISTS (
                        SELECT 1 
                        FROM information_schema.columns 
                        WHERE table_name = 'user' 
                        AND column_name = 'is_admin'
                    ) THEN
                        ALTER TABLE "user" ADD COLUMN is_admin BOOLEAN NOT NULL DEFAULT false;
                    END IF;
                END $$;
            """.trimIndent())
        }
    }
} 