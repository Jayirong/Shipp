@file:Suppress("SpellCheckingInspection")

package com.example.shipp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.launch

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
                HomeScreen(navController = navController, nombre = it)
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController){
    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}
    var passwordVisible by remember { mutableStateOf(false) }
    var loginFailed by remember { mutableStateOf(false) } //indicador de error de login
    val coroutineScope = rememberCoroutineScope() //corrutina para la navegacion

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
                coroutineScope.launch {
                    val isValid = UserManager.validateUser(context, email, password)
                    if (isValid) {
                        navController.navigate("home/$email")
                    } else {
                        loginFailed = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión")
        }

        if (loginFailed) {
            Text(
                text = "Credenciales Incorrectas. Inténtelo de nuevo.",
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

    var user by remember { mutableStateOf("")}
    var nombre by remember { mutableStateOf("")}
    var apellido by remember { mutableStateOf("")}
    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}
    var confirmPassword by remember { mutableStateOf("")}

    var registerFailed by remember { mutableStateOf(false) } //indicador de error de registro

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
            value = user,
            onValueChange = { user = it},
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth()
        )

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

        Button(
            onClick = {
                val users = UserManager.getUsers(context)
                if (users.none { it.email == email }){ //verificamos que el email no exista antes
                    if (password == confirmPassword) {
                        //si el parametro es correcto registra el usuario con todos los campos
                        val newUser = User(user, nombre, apellido, email, password)
                        UserManager.addUser(context, newUser)
                        navController.navigate("login")
                    }
                } else {
                    registerFailed = true
                }
               
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Registrar")
        }

        if (registerFailed) {
            Text(text = "El correo indicado ya está en uso para una cuenta",
                color = MaterialTheme.colorScheme.error)
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
fun HomeScreen(navController: NavController, nombre: String) {
    //el drawer es para el boton sandwich de la esquina
    val drawerState = rememberDrawerState(DrawerValue.Closed) //controla si el drawer esta abierto o cerrado
    val coroutineScope = rememberCoroutineScope() //esto es para recordar el alcance de la corrutina, es para ejecutar funcionces tipo suspend

    val context = LocalContext.current

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
                            SessionManager.logoutUser(context)
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
                        text = "Bienvenido $nombre",
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

                IconButtonWithText(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Lista de sonidos",
                    onClick = { /* Nav a lista de sonidos */ }
                )
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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController(), nombre = "Usuario")
}

//@Composable
//fun RegisterScreenPreview(){
//    //aqui basicamente hacemos un mock para poder ejecutar un preview del register, porque necesitamos un navcontroller, el cual solo esta disponible en el tiempo de ejecucion
//    val navController = rememberNavController()
//    RegisterScreen(navController = navController)
//}