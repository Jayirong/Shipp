@file:Suppress("SpellCheckingInspection")

package com.example.shipp

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DrawerValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.shipp.ui.theme.ShippTheme
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShippTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }
}

//Por las bondades de JetPack Compose no es necesario crear otro activity, asi que procedo a hacer las pantallas bajo los composbales en adelante
//Esta gracias al NavHost, que nos permite navegar entre pantallas sin necesidad de crear otro activity

@Composable
fun Navigation(){
    val navController = rememberNavController()

    //si mal no entendi, el navController nos permite hacer como una especie de indexacion entre composables
    NavHost(navController = navController, startDestination = "login"){
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("PasswordRecovery"){
            PasswordRecoveryScreen(navController = navController)
        }
        composable("home/{nombre}") { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre")
            nombre?.let{
                HomeScreen(navController = navController)
            }
        }
        composable("geoCosas") {
            GeoCosasScreen(navController = navController)
        }

        composable("listaUbicaciones") {
            ListaUbicacionesScreen(navController = navController)
        }

    }
}

@Composable
fun LoginScreen(navController: NavController){
    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}
    var passwordVisible by remember { mutableStateOf(false) }
    var loginFailed by remember { mutableStateOf(false) } //indicador de error de login
    var errmess by remember { mutableStateOf<String?>(null)}

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo de app",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp),
            contentScale= ContentScale.Fit
        )

        Text(text = "Shipp", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {password = it},
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if(passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Cambiar Visibilidad Contraseña"
                )
            }
        })

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navController.navigate("home/${auth.currentUser?.email}")
                        } else {
                            loginFailed = true
                            errmess = task.exception?.message
                            Toast.makeText(context, "Error de autenticacion: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión")
        }

        if (loginFailed) {
            Text(
                text = errmess ?: "Credenciales Incorrectas. Inténtelo de nuevo.",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("No tienes una cuenta? Regístrate aquí")
        }

        TextButton(onClick = { navController.navigate("passwordRecovery") }) {
            Text("Olvidaste tu contraseña?")
        }

    }
}

@Composable
fun RegisterScreen(navController: NavController){
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("")}
    var apellido by remember { mutableStateOf("")}
    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}
    var confirmPassword by remember { mutableStateOf("")}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Registro", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it},
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it},
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email")},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it},
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it},
            label = { Text("Confirmar Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        val auth = FirebaseAuth.getInstance()

        Button(
            onClick = {
                if (nombre.isNotEmpty() && apellido.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //info adicional para firebase por si es necesario
                                val userId = task.result?.user?.uid
                                val database = FirebaseDatabase.getInstance()
                                val ref = database.getReference("usuarios").child(userId!!)

                                val usuario = User(nombre = nombre, apellido = apellido, email = email)

                                ref.setValue(usuario).addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        Toast.makeText(context, "Buena data", Toast.LENGTH_LONG).show()
                                        navController.navigate("login")
                                    } else {
                                        Toast.makeText(context, "Error al guardar datos: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}",Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                            Toast.makeText(context, "Mala data", Toast.LENGTH_LONG).show()
                        }
                },
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Registrar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("login")}
        ){
            Text("Ya tienes una cuenta? Inicia sesión")
        }
    }

}

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val database = FirebaseDatabase.getInstance().getReference("usuarios")
    var userName by remember { mutableStateOf<String?>(null) }

    //obetenr el nombre de usuario de firebase
    LaunchedEffect(currentUser) {
        currentUser?.let {
            database.child(it.uid).get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                userName = user?.nombre ?: "Usuario"
            }.addOnFailureListener{
                Toast.makeText(context, "Error al obtener el nombre de usuario", Toast.LENGTH_LONG).show()
            }
        }
    }

    //el drawer es para el boton sandwich de la esquina
    val drawerState = rememberDrawerState(DrawerValue.Closed) //controla si el drawer esta abierto o cerrado
    val coroutineScope = rememberCoroutineScope() //esto es para recordar el alcance de la corrutina, es para ejecutar funcionces tipo suspend

    //ModalDrawer que despliega el emnu laternal
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            //Contenido del drawer
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "MENU", fontSize = 20.sp, modifier = Modifier.padding(8.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    //aqui va el boton perfil
                    Row(
                        modifier = Modifier
                            .clickable { /*Nav al perfil*/ }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Perfil", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                //aqui va el boton pa cerrar sesion
                Row(
                    modifier = Modifier
                        .clickable {
                            FirebaseAuth
                                .getInstance()
                                .signOut()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cerrar Sesión",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Cerrar Sesión", fontSize = 18.sp)
                }
            }
        },

        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Header, con el menu sandwich y saludo al user
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                coroutineScope.launch { //lanzamos la corrutina
                                    drawerState.open()
                                }
                            } //Abrimos el drawer
                    )

                    Text(
                        text = "Bienvenido ${userName ?: "Usuario"}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                //botones de accion con sus iconos correspondientes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButtonWithText(
                        icon = Icons.Default.Settings,
                        label = "Ajustes",
                        onClick = { /*Nav a ajustes*/ }
                    )

                    IconButtonWithText(
                        icon = Icons.Default.Add,
                        label = "Añadir Sonidos",
                        onClick = { /*Nav a annadir sonidos*/ }
                    )

                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButtonWithText(
                        icon = Icons.AutoMirrored.Filled.List,
                        label = "Lista de sonidos",
                        onClick = { /* Nav a lista de sonidos */ }
                    )

                    IconButtonWithText(
                        icon = Icons.Default.LocationOn,
                        label = "GeoCosas",
                        onClick = { navController.navigate("geoCosas") }
                    )

                }

            }
        }
    )
}

@Composable
fun PasswordRecoveryScreen(navController: NavController) {
    var email by remember { mutableStateOf("")}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Recuperación de contraseña",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Ingrese el correo electrónico con el que creó su cuenta, un email con las intstrucciones para el cabio de contraseña le llegarán a su correo.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("ENVIAR")
        }
    }
}

//Composable reutilizable para los botones con iconos y texto
@Composable
fun IconButtonWithText(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Black,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GeoCosasScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    //estas van a ser las variales de estado para almacenar la ubicacion
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationName by remember { mutableStateOf("") }

    //permisos
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(permissionState.status) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    if (permissionState.status.isGranted) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "GeoCosas", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    getLastLocation(context, fusedLocationClient) { lat, lon ->
                        latitude = lat
                        longitude = lon
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Obtener Ubicación Actual")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (latitude != null && longitude != null) {
                Text(text = "Latitud: $latitude")
                Text(text = "Longitud: $longitude")

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Nombre del Lugar") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        saveLocationToFirebase(context, latitude!!, longitude!!, locationName)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Ubicación")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("listaUbicaciones")},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mostrar Ubicaciones Guardadas")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigateUp() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    } else {
        //solicitar permiso
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
           Text("La aplicacion necesita permiso de ubicacion para funcionar.")
           Spacer(modifier = Modifier.height(8.dp))
           Button(onClick = { permissionState.launchPermissionRequest() }) {
               Text("Solicitar Permiso")
           }
        }
    }


}

//funcion auxiliar para geocosas, esta obtiene la localizacion mediante coordenadas
fun getLastLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Double?, Double?) -> Unit
) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                onLocationReceived(location.latitude, location.longitude)
            } else {
                onLocationReceived(null, null)
                Toast.makeText(context, "No se pudo obtener la ubicacion", Toast.LENGTH_LONG).show()
            }
        }
        .addOnFailureListener {
            onLocationReceived(null, null)
            Toast.makeText(context, "Error al obtener la ubicacion", Toast.LENGTH_LONG).show()
        }
}

//guardar la ubicacion en firebase
fun saveLocationToFirebase(context: Context, latitude: Double, longitude: Double, locationName: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val database = FirebaseDatabase.getInstance()
    val ref = database.getReference("locations").child(currentUser?.uid ?: "unknown_user")
    
    val locationData = mapOf(
        "name" to locationName,
        "latitude" to latitude,
        "longitude" to longitude
    )

    ref.push().setValue(locationData)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Ubicacion guardada", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error al guardar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}

//Aqu[i creamos la pantalla para la lista de ubicaciones
//Esta anotacion es para permitir el uso de apis experimentales, en este caso concreto sale como api experimental el TopAppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaUbicacionesScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val database =FirebaseDatabase.getInstance()
    val locationsRef = database.getReference("locations").child(currentUser?.uid ?: "unknown_user")

    var locationsList by remember { mutableStateOf<List<LocationData>>(emptyList())}
    var isLoading by remember { mutableStateOf(true) }

    //recuperar las ubicaciones desde Firebasec
    LaunchedEffect(Unit) {
        locationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locations = mutableListOf<LocationData>()
                for (locationSnapshot in snapshot.children) {
                    val location = locationSnapshot.getValue(LocationData::class.java)
                    location?.let { locations.add(it) }
                }
                locationsList = locations
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar ubicaciones: ${error.message}", Toast.LENGTH_LONG).show()
                isLoading = false
            }

        })
    }

    //UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ubicaciones Guardadas") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")

                    }
                }
            )
        }
    ){ paddingValues ->
        if (isLoading) {
            //esto es pa mostrar un indicador de carga >:)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (locationsList.isEmpty()) {
              //si no hay ubicaciones mostramos un mensaje
              Box(
                  modifier = Modifier
                      .fillMaxSize()
                      .padding(paddingValues),
                  contentAlignment = Alignment.Center
              ){
                  Text("No hay ubicaciones guardadas.")
              }
            } else {
                //Mostrar lista de ubicaciones
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                    ) {
                    items(locationsList){ location ->
                      LocationItem(locationData = location)
                    }
                }
            }
        }
    }
}

//Usamos LocationItem como una especie de objeto para ir mostrando la data de las ubicaciones guardadas
@Composable
fun LocationItem(locationData: LocationData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: ${locationData.name}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Latitud: ${locationData.latitude}")
            Text(text = "Longitud: ${locationData.longitude}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GeoCosasPreview() {
    GeoCosasScreen(navController = rememberNavController())
}