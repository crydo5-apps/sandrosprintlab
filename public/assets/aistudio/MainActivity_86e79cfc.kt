package de.sandrosprintlabor

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LabBlack = Color(0xFF080B12)
private val LabSurface = Color(0xFF101722)
private val LabCard = Color(0xFF151F2C)
private val LabRaised = Color(0xFF1B2938)
private val LaserCyan = Color(0xFF38E8FF)
private val ElectricBlue = Color(0xFF537CFF)
private val PlasmaMagenta = Color(0xFFD65CFF)
private val NeonMint = Color(0xFF43E6A2)
private val Amber = Color(0xFFFFB84D)
private val SignalRed = Color(0xFFFF5D73)
private val IceWhite = Color(0xFFF1F7FF)
private val ColdGray = Color(0xFFA7B5C8)
private val SteelGray = Color(0xFF68778A)
private val DividerColor = Color(0xFF263547)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SandrosTheme { SandrosApp() } }
    }
}

private enum class Page(val title: String, val icon: ImageVector) {
    HOME("Start", Icons.Default.Home),
    CREATE("Neues Modell", Icons.Default.AddAPhoto),
    EDITOR("3D-Modell", Icons.Default.ViewInAr),
    PREP("Druckvorbereitung", Icons.Default.Tune),
    EXPORT("G-Code & Export", Icons.Default.SaveAlt),
    PROJECTS("Projekte", Icons.Default.FolderOpen),
    SETTINGS("Einstellungen", Icons.Default.Settings)
}

private data class Project(val name: String, val status: String, val icon: ImageVector)

@Composable
private fun SandrosApp() {
    var currentPage by remember { mutableStateOf(Page.HOME) }
    var photos by remember { mutableStateOf(emptyList<Uri>()) }
    var projectName by remember { mutableStateOf("Mein 3D-Modell") }
    var selectedMaterial by remember { mutableStateOf("PLA") }
    var layerHeight by remember { mutableStateOf(0.20f) }
    var infill by remember { mutableFloatStateOf(20f) }
    var supports by remember { mutableStateOf(true) }
    var modelFile by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // Echte Fotoauswahl; das Erzeugen des 3D-Modells braucht noch einen angebundenen Rekonstruktionsdienst.
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { selected ->
        photos = selected
        if (selected.isNotEmpty()) Toast.makeText(context, "${selected.size} Foto(s) ausgewählt. 3D-Rekonstruktion ist noch nicht angebunden.", Toast.LENGTH_LONG).show()
    }
    val modelPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            modelFile = uri
            Toast.makeText(context, "Modell ausgewählt. Eine lokale 3D-Ansicht ist im MVP noch Demo.", Toast.LENGTH_LONG).show()
        }
    }
    val savePicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) Toast.makeText(context, "Exportziel ausgewählt. Zuerst muss ein echter Slicer G-Code erzeugen.", Toast.LENGTH_LONG).show()
    }

    val navigate: (Page) -> Unit = { currentPage = it }
    Scaffold(
        containerColor = LabBlack,
        topBar = { LabTopBar(title = if (currentPage == Page.HOME) "Sandros Print Labor" else currentPage.title) },
        bottomBar = { LabBottomBar(current = currentPage, onNavigate = navigate) }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(LabBlack, LabSurface))).padding(padding)) {
            when (currentPage) {
                Page.HOME -> DashboardScreen(onNew = { navigate(Page.CREATE) }, onProjects = { navigate(Page.PROJECTS) }, onSettings = { navigate(Page.SETTINGS) }, onResume = { navigate(Page.EDITOR) })
                Page.CREATE -> CreateScreen(
                    projectName = projectName,
                    onNameChange = { projectName = it },
                    photoCount = photos.size,
                    onChoosePhotos = { photoPicker.launch("image/*") },
                    onImportModel = { modelPicker.launch(arrayOf("model/stl", "application/sla", "application/octet-stream", "model/3mf", "*/*")) },
                    onContinue = { navigate(Page.EDITOR) },
                    hasModel = modelFile != null
                )
                Page.EDITOR -> ModelEditorScreen(hasImportedModel = modelFile != null, onImport = { modelPicker.launch(arrayOf("model/stl", "application/sla", "application/octet-stream", "model/3mf", "*/*")) }, onContinue = { navigate(Page.PREP) })
                Page.PREP -> PrintPreparationScreen(material = selectedMaterial, onMaterial = { selectedMaterial = it }, layer = layerHeight, onLayer = { layerHeight = it }, infill = infill, onInfill = { infill = it }, supports = supports, onSupports = { supports = it }, onSlice = { Toast.makeText(context, "Slicer noch nicht verbunden — es wurde kein G-Code erzeugt.", Toast.LENGTH_LONG).show() }, onContinue = { navigate(Page.EXPORT) })
                Page.EXPORT -> ExportScreen(onChooseLocation = { savePicker.launch("${projectName.replace(" ", "_")}.gcode") })
                Page.PROJECTS -> ProjectsScreen(onOpen = { navigate(Page.EDITOR) }, onNew = { navigate(Page.CREATE) })
                Page.SETTINGS -> SettingsScreen(context = context)
            }
        }
    }
}

@Composable
private fun SandrosTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = LaserCyan, onPrimary = LabBlack, secondary = ElectricBlue,
        background = LabBlack, surface = LabCard, onBackground = IceWhite,
        onSurface = IceWhite, error = SignalRed
    )
    MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
}

@Composable
private fun LabTopBar(title: String) {
    Row(
        Modifier.fillMaxWidth().background(LabBlack).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, color = IceWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (title == "Sandros Print Labor") Text("DIGITAL FABRICATION LAB", color = SteelGray, fontSize = 9.sp, letterSpacing = 2.sp)
        }
        Row(Modifier.background(LabRaised, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, tint = LaserCyan, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text("Demo-Modus", color = LaserCyan, fontSize = 10.sp)
        }
    }
}

@Composable
private fun LabBottomBar(current: Page, onNavigate: (Page) -> Unit) {
    NavigationBar(containerColor = LabSurface, contentColor = ColdGray, tonalElevation = 0.dp) {
        listOf(Page.HOME, Page.PROJECTS, Page.SETTINGS).forEach { page ->
            NavigationBarItem(
                selected = current == page,
                onClick = { onNavigate(page) },
                icon = { Icon(page.icon, contentDescription = page.title) },
                label = { Text(page.title, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = LaserCyan, selectedTextColor = LaserCyan, indicatorColor = LabRaised, unselectedIconColor = SteelGray, unselectedTextColor = ColdGray)
            )
        }
    }
}

@Composable
private fun DashboardScreen(onNew: () -> Unit, onProjects: () -> Unit, onSettings: () -> Unit, onResume: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Willkommen im Print-Labor", color = ColdGray, fontSize = 14.sp)
            Text("Deine nächste Idee\nbekommt Form.", color = IceWhite, fontSize = 29.sp, fontWeight = FontWeight.Bold, lineHeight = 35.sp)
        }
        item { PrimaryButton("Neues Modell erstellen", Icons.Default.Add, onNew) }
        item {
            SectionHeader("Dein Workflow", "01 / 04")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Fotos", "Modell", "Slicen", "Export").forEachIndexed { index, label ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().height(4.dp).background(if (index == 0) LaserCyan else DividerColor, CircleShape))
                        Spacer(Modifier.height(7.dp)); Text(label, color = if (index == 0) LaserCyan else SteelGray, fontSize = 10.sp)
                    }
                }
            }
        }
        item {
            SectionHeader("Zuletzt bearbeitet", "Alle ansehen", onProjects)
            LabCard(Modifier.fillMaxWidth().clickable { onResume() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ModelThumb(Modifier.size(58.dp))
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Mein 3D-Modell", color = IceWhite, fontWeight = FontWeight.SemiBold)
                        Text("Demo-Projekt · zuletzt geöffnet", color = ColdGray, fontSize = 12.sp)
                    }
                    StatusPill("Demo", Amber)
                }
            }
        }
        item {
            SectionHeader("Druckumgebung")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoTile(Modifier.weight(1f), Icons.Default.Print, "Druckerprofil", "CR-10S Pro", ElectricBlue)
                InfoTile(Modifier.weight(1f), Icons.Default.SdStorage, "SD-Karte", "Nicht verbunden", Amber)
            }
        }
        item {
            LabCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = LaserCyan)
                    Spacer(Modifier.width(10.dp))
                    Text("3D-Rekonstruktion und Slicer müssen noch angebunden werden. Demo-Status wird deutlich markiert.", color = ColdGray, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun CreateScreen(projectName: String, onNameChange: (String) -> Unit, photoCount: Int, onChoosePhotos: () -> Unit, onImportModel: () -> Unit, onContinue: () -> Unit, hasModel: Boolean) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { StepHeader("01", "Fotos & Projekt", "Fotos aus mehreren Blickwinkeln liefern die beste Grundlage.") }
        item {
            LabCard(Modifier.fillMaxWidth()) {
                Text("Projektname", color = ColdGray, fontSize = 12.sp)
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(value = projectName, onValueChange = onNameChange, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Name") }, colors = fieldColors())
            }
        }
        item {
            LabCard(Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AddAPhoto, null, tint = LaserCyan, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(8.dp))
                Text("Fotos hinzufügen", color = IceWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(if (photoCount > 0) "$photoCount Foto(s) ausgewählt" else "Wähle mehrere Aufnahmen desselben Objekts.", color = ColdGray, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                SecondaryButton("Bilder auswählen", Icons.Default.Collections, onChoosePhotos)
            }
        }
        item {
            LabCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lightbulb, null, tint = Amber)
                    Spacer(Modifier.width(9.dp))
                    Column {
                        Text("Aufnahme-Tipps", color = IceWhite, fontWeight = FontWeight.SemiBold)
                        Text("Gleichmäßiges Licht · ruhiger Hintergrund · Objekt vollständig zeigen · unterschiedliche Blickwinkel", color = ColdGray, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
        item { SecondaryButton("Vorhandene STL/3MF importieren", Icons.Default.FileOpen, onImportModel) }
        item {
            Text("Datenschutz: Fotos werden in diesem Prototyp nicht hochgeladen. Eine externe Verarbeitung wird erst nach Zustimmung und Backend-Anbindung möglich.", color = SteelGray, fontSize = 11.sp, lineHeight = 16.sp)
        }
        item { PrimaryButton(if (hasModel || photoCount > 0) "Weiter zum Modell (Demo)" else "Zum Modellbereich", Icons.Default.ArrowForward, onContinue) }
    }
}

@Composable
private fun ModelEditorScreen(hasImportedModel: Boolean, onImport: () -> Unit, onContinue: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { StepHeader("02", "3D-Modell prüfen", "Ansicht und Maße kontrollieren, bevor du den Druck vorbereitest.") }
        item {
            Box(Modifier.fillMaxWidth().height(300.dp).background(Brush.verticalGradient(listOf(LabRaised, LabBlack)), RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ViewInAr, null, tint = LaserCyan, modifier = Modifier.size(90.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(if (hasImportedModel) "Datei ausgewählt" else "3D-Vorschau · Demo", color = IceWhite, fontWeight = FontWeight.Bold)
                    Text("Interaktive STL/3MF-Ansicht noch nicht angebunden", color = ColdGray, fontSize = 11.sp)
                }
                StatusPill("DEMO", Amber, Modifier.align(Alignment.TopEnd).padding(12.dp))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoTile(Modifier.weight(1f), Icons.Default.Straighten, "Maße", "Nicht berechnet", ElectricBlue)
                InfoTile(Modifier.weight(1f), Icons.Default.CenterFocusStrong, "Druckbett", "Nicht geprüft", Amber)
            }
        }
        item { SecondaryButton("STL/3MF-Datei wählen", Icons.Default.FileOpen, onImport) }
        item {
            LabCard(Modifier.fillMaxWidth()) {
                Text("Modellprüfung", color = IceWhite, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                StatusLine(Icons.Default.Warning, "Mesh-Prüfung verfügbar nach 3D-Engine-Integration", Amber)
            }
        }
        item { PrimaryButton("Druck vorbereiten", Icons.Default.Tune, onContinue) }
    }
}

@Composable
private fun PrintPreparationScreen(material: String, onMaterial: (String) -> Unit, layer: Float, onLayer: (Float) -> Unit, infill: Float, onInfill: (Float) -> Unit, supports: Boolean, onSupports: (Boolean) -> Unit, onSlice: () -> Unit, onContinue: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { StepHeader("03", "Druckvorbereitung", "Einstellungen vor dem Slicing sorgfältig prüfen.") }
        item {
            LabCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Print, null, tint = LaserCyan, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Column { Text("Creality CR-10S Pro", color = IceWhite, fontWeight = FontWeight.Bold); Text("Profilvorschlag · bitte Druckerversion prüfen", color = Amber, fontSize = 11.sp) }
                }
            }
        }
        item {
            LabCard(Modifier.fillMaxWidth()) {
                Text("Material", color = IceWhite, fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, DividerColor)) { Text(material, color = IceWhite); Spacer(Modifier.weight(1f)); Icon(Icons.Default.ExpandMore, null, tint = LaserCyan) }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) { listOf("PLA", "PETG", "ABS", "TPU").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onMaterial(it); menuOpen = false }) } }
                }
            }
        }
        item {
            LabCard(Modifier.fillMaxWidth()) {
                Text("Schichthöhe · ${"%.2f".format(layer)} mm", color = IceWhite)
                Slider(value = layer, onValueChange = onLayer, valueRange = 0.08f..0.32f, colors = SliderDefaults.colors(thumbColor = LaserCyan, activeTrackColor = LaserCyan))
                Text("Füllung · ${infill.toInt()} %", color = IceWhite)
                Slider(value = infill, onValueChange = onInfill, valueRange = 0f..100f, colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue))
                Row(verticalAlignment = Alignment.CenterVertically) { Text("Stützstrukturen", color = IceWhite, modifier = Modifier.weight(1f)); Switch(checked = supports, onCheckedChange = onSupports) }
            }
        }
        item { StatusLine(Icons.Default.Info, "Düsen-, Temperatur- und Firmwarewerte sind nicht bestätigt.", Amber) }
        item { SecondaryButton("Slicer-Status prüfen", Icons.Default.PlayArrow, onSlice) }
        item { PrimaryButton("Zum Exportbereich", Icons.Default.ArrowForward, onContinue) }
        item { Text("Zeit und Materialverbrauch erscheinen erst nach echtem Slicing als Schätzwerte.", color = SteelGray, fontSize = 11.sp) }
    }
}

@Composable
private fun ExportScreen(onChooseLocation: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { StepHeader("04", "G-Code & Export", "Speichere eine tatsächlich erzeugte G-Code-Datei auf erreichbarem Speicher.") }
        item {
            LabCard(Modifier.fillMaxWidth()) {
                StatusLine(Icons.Default.ErrorOutline, "Slicer nicht verbunden", Amber)
                Spacer(Modifier.height(8.dp))
                Text("Es wurde noch kein druckfertiger G-Code erzeugt. Der Export bleibt deshalb deaktiviert.", color = ColdGray, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
        item { PrimaryButton("SD-Kartenexport noch nicht verfügbar", Icons.Default.SdStorage, onClick = {}, enabled = false) }
        item {
            LabCard(Modifier.fillMaxWidth()) {
                Text("Technischer Integrationspunkt", color = IceWhite, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(5.dp))
                Text("Nach erfolgreichem Slicing kann Androids Dateiauswahl (Storage Access Framework) verwendet werden. Für SD-Karten/USB-OTG muss Android den Speicher freigeben.", color = ColdGray, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
        item { SecondaryButton("Speicherziel auswählen (Demo)", Icons.Default.FolderOpen, onChooseLocation) }
        item { Text("Das Speichern einer Datei startet den Druck nicht. Der Druck wird am Gerät gestartet.", color = SteelGray, fontSize = 11.sp, lineHeight = 16.sp) }
    }
}

@Composable
private fun ProjectsScreen(onOpen: () -> Unit, onNew: () -> Unit) {
    val projects = listOf(Project("Mein 3D-Modell", "Demo · Fotos fehlen", Icons.Default.ViewInAr), Project("Werkzeughalter", "Import erforderlich", Icons.Default.Build), Project("Miniatur-Raumschiff", "Demo-Projekt", Icons.Default.RocketLaunch))
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Projektbibliothek", color = IceWhite, fontSize = 25.sp, fontWeight = FontWeight.Bold) }
        item { PrimaryButton("Neues Projekt", Icons.Default.Add, onNew) }
        items(projects) { project ->
            LabCard(Modifier.fillMaxWidth().clickable { onOpen() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ModelThumb(Modifier.size(54.dp), project.icon)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(project.name, color = IceWhite, fontWeight = FontWeight.SemiBold); Text(project.status, color = ColdGray, fontSize = 11.sp) }
                    Icon(Icons.Default.ChevronRight, null, tint = LaserCyan)
                }
            }
        }
        item { Text("Projekte werden in diesem MVP nur lokal im UI-Zustand angezeigt.", color = SteelGray, fontSize = 11.sp) }
    }
}

@Composable
private fun SettingsScreen(context: Context) {
    var unit by remember { mutableStateOf("Millimeter (mm)") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Einstellungen", color = IceWhite, fontSize = 25.sp, fontWeight = FontWeight.Bold) }
        item { SettingRow(Icons.Default.Print, "Standarddrucker", "Creality CR-10S Pro · Profil prüfen") }
        item { SettingRow(Icons.Default.Science, "Materialprofile", "PLA, PETG, ABS, TPU") }
        item { SettingRow(Icons.Default.Straighten, "Maßeinheit", unit, onClick = { unit = if (unit.startsWith("Millimeter")) "Zoll (in)" else "Millimeter (mm)" }) }
        item { SettingRow(Icons.Default.Security, "Datenschutz", "Keine Fotos werden im Demo hochgeladen") }
        item { SettingRow(Icons.Default.HelpOutline, "Hilfe & Integrationen", "Rekonstruktion · Slicer · Export") }
        item { LabCard(Modifier.fillMaxWidth()) { Text("Über die App", color = IceWhite, fontWeight = FontWeight.SemiBold); Text("Sandros Print Labor · Android UI-Prototyp", color = ColdGray, fontSize = 12.sp) } }
    }
}

@Composable
private fun StepHeader(number: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(number, color = LaserCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
        Spacer(Modifier.width(10.dp))
        Column { Text(title, color = IceWhite, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = ColdGray, fontSize = 12.sp, lineHeight = 17.sp) }
    }
}

@Composable
private fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = IceWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
        if (action != null) Text(action, color = LaserCyan, fontSize = 11.sp, modifier = Modifier.clickable { onAction?.invoke() })
    }
}

@Composable
private fun LabCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.background(LabCard, RoundedCornerShape(18.dp)).then(Modifier.padding(15.dp)), content = content)
}

@Composable
private fun PrimaryButton(text: String, icon: ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = LaserCyan, contentColor = LabBlack, disabledContainerColor = LabRaised, disabledContentColor = SteelGray)) {
        Icon(icon, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(9.dp)); Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SecondaryButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(13.dp), border = BorderStroke(1.dp, DividerColor), colors = ButtonDefaults.outlinedButtonColors(contentColor = IceWhite)) {
        Icon(icon, null, tint = LaserCyan, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(9.dp)); Text(text)
    }
}

@Composable
private fun InfoTile(modifier: Modifier, icon: ImageVector, title: String, value: String, tint: Color) {
    Column(modifier.background(LabCard, RoundedCornerShape(16.dp)).padding(13.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)); Spacer(Modifier.height(8.dp)); Text(title, color = ColdGray, fontSize = 10.sp); Text(value, color = IceWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ModelThumb(modifier: Modifier, icon: ImageVector = Icons.Default.ViewInAr) {
    Box(modifier.background(Brush.linearGradient(listOf(LabRaised, LabBlack)), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = LaserCyan, modifier = Modifier.size(29.dp)) }
}

@Composable
private fun StatusPill(text: String, tint: Color, modifier: Modifier = Modifier) {
    Text(text, color = tint, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = modifier.background(tint.copy(alpha = 0.11f), RoundedCornerShape(50)).padding(horizontal = 9.dp, vertical = 5.dp))
}

@Composable
private fun StatusLine(icon: ImageVector, text: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(8.dp)); Text(text, color = ColdGray, fontSize = 12.sp) }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, value: String, onClick: (() -> Unit)? = null) {
    LabCard(Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = LaserCyan, modifier = Modifier.size(21.dp)); Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(title, color = IceWhite, fontWeight = FontWeight.SemiBold); Text(value, color = ColdGray, fontSize = 11.sp) }
            if (onClick != null) Icon(Icons.Default.ChevronRight, null, tint = SteelGray)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = IceWhite, unfocusedTextColor = IceWhite,
    focusedBorderColor = LaserCyan, unfocusedBorderColor = DividerColor,
    focusedLabelColor = LaserCyan, unfocusedLabelColor = ColdGray,
    cursorColor = LaserCyan
)
