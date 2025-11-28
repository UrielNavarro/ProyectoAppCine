package org.utl.miappdecine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CineViewModel(private val dao: CineDao) : ViewModel() {

    // Listas observables
    val cartelera = dao.getCartelera()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val proximamente = dao.getProximamente()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val snacks = dao.getSnacks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Funciones para agregar y borrar
    fun agregarPelicula(titulo: String, esEstreno: Boolean) {
        viewModelScope.launch {
            dao.insertarPelicula(Pelicula(titulo = titulo, esEstreno = esEstreno))
        }
    }

    fun borrarPelicula(pelicula: Pelicula) {
        viewModelScope.launch {
            dao.borrarPelicula(pelicula)
        }
    }

    fun agregarSnack(nombre: String, precio: String) {
        viewModelScope.launch {
            dao.insertarSnack(Snack(nombre = nombre, precio = precio))
        }
    }

    fun borrarSnack(snack: Snack) {
        viewModelScope.launch {
            dao.borrarSnack(snack)
        }
    }

    // Factory para crear el ViewModel
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CineApplication)
                CineViewModel(app.db.cineDao())
            }
        }
    }
}