package com.example.myrecipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.IntrinsicSize

val AppMagenta = Color(0xFFE91E63)
val BackgroundGray = Color(0xFFF8F9FA)
val PinkBadgeBg = Color(0xFFFCE4EC)
val PinkBadgeText = Color(0xFFC2185B)
val GreenBadgeBg = Color(0xFFC8E6C9)
val GreenBadgeText = Color(0xFF2E7D32)

enum class Difficulty(val displayName: String) {
    EASY("Легко"),
    MEDIUM("Середньо"),
    HARD("Складно")
}

data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val ingredients: String,
    val instructions: String,
    val timeMinutes: Int,
    val difficulty: Difficulty,

    val category: String = "Основна страва",
    val emojiIcon: String = "🍲"
)

class RecipesViewModel : ViewModel() {

    private val _recipes = MutableStateFlow(
        listOf(
            Recipe(
                id = "1",
                title = "Паста Карбонара",
                category = "Італійська класика",
                ingredients = "Паста, яйця, бекон, пармезан",
                instructions = "Відварити пасту. Обсмажити бекон. Змішати яйця з тертим пармезаном. З'єднати пасту з беконом та яєчною сумішшю поза вогнем, ретельно перемішати.",
                timeMinutes = 25,
                difficulty = Difficulty.EASY,
                emojiIcon = "🍝"
            ),
            Recipe(
                id = "2",
                title = "Цезар із куркою",
                category = "Легкий салат",
                ingredients = "Куряче філе, салат ромен, сухарики, соус Цезар, пармезан",
                instructions = "Обсмажити куряче філе до золотистої скоринки. Нарізати листя салату. Змішати всі інгредієнти, заправити фірмовим соусом та посипати сухариками.",
                timeMinutes = 15,
                difficulty = Difficulty.EASY,
                emojiIcon = "🥗"
            ),
            Recipe(
                id = "3",
                title = "Шоколадний торт",
                category = "Десерт",
                ingredients = "Борошно, цукор, какао, яйця, вершкове масло, розпушувач",
                instructions = "Змішати сухі інгредієнти. Збити яйця з цукром та розтопленим маслом. Об'єднати суміші, випікати 45 хвилин при 180°C.",
                timeMinutes = 60,
                difficulty = Difficulty.MEDIUM,
                emojiIcon = "🍰"
            )
        )
    )

    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    fun getRecipeById(id: String): Recipe? {
        return _recipes.value.find { it.id == id }
    }

    fun addRecipe(recipe: Recipe) {
        _recipes.value = _recipes.value + recipe
    }

    fun updateRecipe(updatedRecipe: Recipe) {
        _recipes.value = _recipes.value.map { current ->
            if (current.id == updatedRecipe.id) updatedRecipe else current
        }
    }

    fun deleteRecipe(id: String) {
        _recipes.value = _recipes.value.filterNot { it.id == id }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipesApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(title: String, showBackButton: Boolean = false, onBackClick: () -> Unit = {}) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = if (showBackButton) TextAlign.Start else TextAlign.Center
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppMagenta)
    )
}

@Composable
fun RecipesApp() {
    val navController = rememberNavController()
    val viewModel: RecipesViewModel = viewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundGray
    ) {
        NavHost(navController = navController, startDestination = "recipes_list") {

            composable("recipes_list") {
                RecipesListScreen(navController, viewModel)
            }

            composable(
                route = "add_edit_recipe?recipeId={recipeId}",
                arguments = listOf(navArgument("recipeId") {
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId")
                AddEditRecipeScreen(navController, viewModel, recipeId)
            }

            composable(
                route = "details/{recipeId}",
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                DetailsRecipeScreen(navController, viewModel, recipeId)
            }
        }
    }
}

@Composable
fun RecipesListScreen(navController: NavHostController, viewModel: RecipesViewModel) {
    val recipes by viewModel.recipes.collectAsState()

    Scaffold(
        topBar = { CustomTopAppBar(title = "Мої рецепти") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_edit_recipe?recipeId=") },
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Додати рецепт")
            }
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(recipe = recipe) {
                    navController.navigate("details/${recipe.id}")
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = recipe.emojiIcon, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recipe.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = recipe.category,
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PinkBadgeBg)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⏱ ${recipe.timeMinutes} хв",
                    color = PinkBadgeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AddEditRecipeScreen(
    navController: NavHostController,
    viewModel: RecipesViewModel,
    recipeId: String?
) {
    val isEditing = !recipeId.isNullOrEmpty()
    val existingRecipe = if (isEditing) viewModel.getRecipeById(recipeId!!) else null

    var title by remember { mutableStateOf(existingRecipe?.title ?: "") }
    var category by remember { mutableStateOf(existingRecipe?.category ?: "Основна страва") }
    var ingredients by remember { mutableStateOf(existingRecipe?.ingredients ?: "") }
    var instructions by remember { mutableStateOf(existingRecipe?.instructions ?: "") }
    var timeMinutes by remember { mutableStateOf(existingRecipe?.timeMinutes?.toString() ?: "") }
    var difficulty by remember { mutableStateOf(existingRecipe?.difficulty ?: Difficulty.EASY) }
    var emojiIcon by remember { mutableStateOf(existingRecipe?.emojiIcon ?: "🍲") }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = if (isEditing) "Редагувати рецепт" else "Новий рецепт",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Назва рецепту") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text("Інгредієнти") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Інструкції") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = timeMinutes,
                        onValueChange = { timeMinutes = it.filter { char -> char.isDigit() } },
                        label = { Text("Час (хв)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = emojiIcon,
                        onValueChange = { emojiIcon = it },
                        label = { Text("Емодзі") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text("Складність:", fontSize = 14.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.values().forEach { diff ->
                        val isSelected = difficulty == diff
                        FilterChip(
                            selected = isSelected,
                            onClick = { difficulty = diff },
                            label = { Text(diff.displayName) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val time = timeMinutes.toIntOrNull() ?: 15
                        if (isEditing && existingRecipe != null) {
                            viewModel.updateRecipe(
                                existingRecipe.copy(
                                    title = title.ifBlank { "Без назви" },
                                    category = category,
                                    ingredients = ingredients,
                                    instructions = instructions,
                                    timeMinutes = time,
                                    difficulty = difficulty,
                                    emojiIcon = emojiIcon
                                )
                            )
                        } else {
                            viewModel.addRecipe(
                                Recipe(
                                    title = title.ifBlank { "Новий рецепт" },
                                    category = category,
                                    ingredients = ingredients,
                                    instructions = instructions,
                                    timeMinutes = time,
                                    difficulty = difficulty,
                                    emojiIcon = emojiIcon
                                )
                            )
                        }
                        navController.navigate("recipes_list") {
                            popUpTo("recipes_list") { inclusive = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("Зберегти", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("Скасувати", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailsRecipeScreen(
    navController: NavHostController,
    viewModel: RecipesViewModel,
    recipeId: String
) {
    val recipe = viewModel.getRecipeById(recipeId)

    if (recipe == null) {
        Scaffold(topBar = {
            CustomTopAppBar(
                "Помилка",
                true,
                { navController.popBackStack() })
        }) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Рецепт не знайдено")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Деталі рецепту",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = recipe.emojiIcon, fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = recipe.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = recipe.category,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                StripeCard(label = "Інгредієнти") {
                    Text(
                        text = recipe.ingredients,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                StripeCard(label = "Час приготування") {
                    Text(
                        text = "${recipe.timeMinutes} хвилин",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }

                StripeCard(label = "Складність") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (recipe.difficulty == Difficulty.EASY) GreenBadgeBg else PinkBadgeBg)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = recipe.difficulty.displayName,
                            color = if (recipe.difficulty == Difficulty.EASY) GreenBadgeText else PinkBadgeText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Інструкції:", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = recipe.instructions, fontSize = 14.sp)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { navController.navigate("add_edit_recipe?recipeId=${recipe.id}") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("Редагувати", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.deleteRecipe(recipe.id)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("Видалити", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StripeCard(label: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(AppMagenta)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = label, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                content()
            }
        }
    }
}