package org.utl.miappdecine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.utl.miappdecine.ui.theme.MiAppDeCineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiAppDeCineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppCine()
                }
            }
        }
    }
}

// --- Definición de las rutas y pantallas ---
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Cartelera : Screen("cartelera", "Cartelera", Icons.Default.Theaters)
    object Proximamente : Screen("proximamente", "Próximamente", Icons.Default.Movie)
    object Snacks : Screen("snacks", "Snacks", Icons.Default.Fastfood)
}

val navigationItems = listOf(
    Screen.Cartelera,
    Screen.Proximamente,
    Screen.Snacks
)

// --- Composable Principal de la App ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCine() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            CineTopAppBar(
                navController = navController,
                currentRoute = currentRoute
            )
        }
    ) { paddingValues ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

// --- Componente: Barra Superior Personalizada ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CineTopAppBar(navController: NavHostController, currentRoute: String?) {
    val title = navigationItems.find { it.route == currentRoute }?.title ?: "MiAppDeCine"

    TopAppBar(
        title = { Text(text = title) },
        actions = {
            navigationItems.forEach { screen ->
                IconButton(
                    onClick = {
                        navController.navigate(screen.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = if (currentRoute == screen.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

// --- Host de Navegación (MODIFICADO PARA USAR VIEWMODEL) ---
@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    // Aquí instanciamos el ViewModel una sola vez
    val viewModel: CineViewModel = viewModel(factory = CineViewModel.Factory)

    NavHost(
        navController = navController,
        startDestination = Screen.Cartelera.route,
        modifier = modifier
    ) {
        composable(Screen.Cartelera.route) {
            // Le pasamos el viewModel a la pantalla
            CarteleraScreen(viewModel)
        }
        composable(Screen.Proximamente.route) {
            ProximamenteScreen(viewModel)
        }
        composable(Screen.Snacks.route) {
            SnacksScreen(viewModel)
        }
    }
}

// --- PANTALLAS CON LÓGICA DE BASE DE DATOS ---

@Composable
fun CarteleraScreen(viewModel: CineViewModel) {
    // 1. Observamos los datos en tiempo real
    val listaPeliculas by viewModel.cartelera.collectAsState()
    var textoNuevo by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDE7E7)) // Mantuve tu color rojo claro
            .padding(16.dp)
    ) {
        // Formulario de Agregar
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = textoNuevo,
                onValueChange = { textoNuevo = it },
                label = { Text("Nueva en Cartelera") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (textoNuevo.isNotBlank()) {
                    // false = Cartelera
                    viewModel.agregarPelicula(textoNuevo, esEstreno = false)
                    textoNuevo = ""
                }
            }) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Elementos
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaPeliculas) { pelicula ->
                ItemCard(texto = pelicula.titulo) {
                    viewModel.borrarPelicula(pelicula)
                }
            }
        }
    }
}

@Composable
fun ProximamenteScreen(viewModel: CineViewModel) {
    val listaProximas by viewModel.proximamente.collectAsState()
    var textoNuevo by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA)) // Mantuve tu color azul claro
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = textoNuevo,
                onValueChange = { textoNuevo = it },
                label = { Text("Próximo Estreno") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (textoNuevo.isNotBlank()) {
                    // true = Próximamente
                    viewModel.agregarPelicula(textoNuevo, esEstreno = true)
                    textoNuevo = ""
                }
            }) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listaProximas) { pelicula ->
                ItemCard(texto = pelicula.titulo) {
                    viewModel.borrarPelicula(pelicula)
                }
            }
        }
    }
}

@Composable
fun SnacksScreen(viewModel: CineViewModel) {
    val listaSnacks by viewModel.snacks.collectAsState()
    var nombreSnack by remember { mutableStateOf("") }
    var precioSnack by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9C4)) // Mantuve tu color amarillo claro
            .padding(16.dp)
    ) {
        // Formulario de Snacks (Nombre y Precio)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                TextField(
                    value = nombreSnack,
                    onValueChange = { nombreSnack = it },
                    label = { Text("Snack") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = precioSnack,
                    onValueChange = { precioSnack = it },
                    label = { Text("Precio ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (nombreSnack.isNotBlank()) {
                        viewModel.agregarSnack(nombreSnack, precioSnack)
                        nombreSnack = ""
                        precioSnack = ""
                    }
                },
                modifier = Modifier.height(120.dp) // Botón alto para cubrir los dos inputs
            ) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listaSnacks) { snack ->
                ItemCard(texto = "${snack.nombre} - $${snack.precio}") {
                    viewModel.borrarSnack(snack)
                }
            }
        }
    }
}

// --- Componente Reutilizable (Tarjeta de Item) ---
@Composable
fun ItemCard(texto: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar",
                    tint = Color.Red
                )
            }
        }
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MiAppDeCineTheme {
        // En preview no cargamos AppCine porque requiere el ViewModel,
        // pero puedes hacer mocks si quieres visualizarlo.
        Text("Vista Previa no disponible con ViewModel en vivo")
    }
}