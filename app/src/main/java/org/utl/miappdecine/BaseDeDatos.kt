package org.utl.miappdecine

import android.app.Application
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// 1. Entidad para Películas (Sirve para Cartelera y Próximamente)
@Entity(tableName = "peliculas")
data class Pelicula(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val esEstreno: Boolean // true = Proximamente, false = Cartelera
)

// 2. Entidad para Snacks
@Entity(tableName = "snacks")
data class Snack(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val precio: String
)

// 3. DAO (Data Access Object) - Las consultas SQL
@Dao
interface CineDao {
    // --- Películas ---
    @Query("SELECT * FROM peliculas WHERE esEstreno = 0")
    fun getCartelera(): Flow<List<Pelicula>>

    @Query("SELECT * FROM peliculas WHERE esEstreno = 1")
    fun getProximamente(): Flow<List<Pelicula>>

    @Insert
    suspend fun insertarPelicula(pelicula: Pelicula)

    @Delete
    suspend fun borrarPelicula(pelicula: Pelicula)

    // --- Snacks ---
    @Query("SELECT * FROM snacks")
    fun getSnacks(): Flow<List<Snack>>

    @Insert
    suspend fun insertarSnack(snack: Snack)

    @Delete
    suspend fun borrarSnack(snack: Snack)
}

// 4. La Base de Datos
@Database(entities = [Pelicula::class, Snack::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cineDao(): CineDao
}

// 5. La Aplicación (Para iniciar la BD una sola vez)
class CineApplication : Application() {
    val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "cine-database"
        ).build()
    }
}