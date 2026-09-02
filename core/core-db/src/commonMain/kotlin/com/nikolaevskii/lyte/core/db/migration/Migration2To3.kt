package com.nikolaevskii.lyte.core.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * v2 → v3: у строки состояния запуска появляется флаг «обучение пройдено».
 *
 * Два шага:
 * 1. `ALTER TABLE ... ADD COLUMN` с тем же `DEFAULT`, что заявлен сущностью — иначе Room не примет
 *    схему при открытии БД. `NOT NULL` без `DEFAULT` SQLite добавить не позволит.
 * 2. Бэкфилл из `has_completed_first_launch`. Строка существует ровно у тех, кто уже запускал
 *    приложение: сид библиотеки выставляет флаг первого запуска на сплэше. Без бэкфилла обучение в
 *    день обновления вылезло бы всей установленной базе — поверх реальных данных и, возможно, поверх
 *    активной сессии. На чистой установке строки нет, `UPDATE` меняет ноль строк, и новый
 *    пользователь обучение увидит.
 */
internal val MIGRATION_2_3: Migration = object : Migration(2, 3) {

    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE app_launch_state ADD COLUMN has_completed_onboarding INTEGER NOT NULL DEFAULT 0",
        )
        connection.execSQL(
            "UPDATE app_launch_state SET has_completed_onboarding = has_completed_first_launch",
        )
    }
}
