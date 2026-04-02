# 📚 Diccionario Náhuat - App Android

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7F52FF.svg?logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-14-3DDC84.svg?logo=android)](https://developer.android.com)
[![Material Design](https://img.shields.io/badge/Material%20Design-3-757575.svg?logo=material-design)](https://material.io)
[![SQLite](https://img.shields.io/badge/SQLite-3-003B57.svg?logo=sqlite)](https://www.sqlite.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> Aplicación móvil para aprender y consultar palabras en idioma Náhuat, con pronunciación, imágenes, favoritos y sistema de progreso de aprendizaje.

## 🚀 Tecnologías Utilizadas

| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| **IDE** | Android Studio Otter (2025.2.3) | Entorno de desarrollo oficial |
| **Kotlin** | 1.9.0 | Lenguaje principal de desarrollo |
| **Android SDK** | API 34 (Android 14) | SDK mínimo: API 24 (Android 7.0) |
| **Material Design 3** | 1.11.0 | Componentes de interfaz moderna |
| **SQLite** | - | Base de datos local para palabras y progreso |
| **RecyclerView** | - | Listas optimizadas y eficientes |
| **MediaPlayer** | - | Reproducción de audio de palabras |

## 📋 Requisitos Previos

- **Android Studio** Hedgehog (2023.1.1) o superior
- **JDK** 17 o superior
- **Android SDK** API 24+ (Android 7.0)
- **Dispositivo o Emulador** con Android 7.0+

## 🔧 Instalación y Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/diccionario-nahuat.git
cd diccionario-nahuat
```

## 📁 Estructura de Proyecto
```bash
app/
├── src/main/
│   ├── java/com/example/diccionario/
│   │   ├── adapter/
│   │   │   └── PalabraAdapter.kt          # Adaptador para RecyclerView de palabras
│   │   ├── db/
│   │   │   └── DBHelper.kt                # Gestión de base de datos SQLite
│   │   ├── modelo/
│   │   │   └── Palabra.kt                 # Modelo de datos de palabra
│   │   ├── ui/
│   │   │   ├── MainActivity.kt            # Actividad principal con navegación inferior
│   │   │   ├── InicioFragment.kt          # Pantalla de inicio con categorías y palabra del día
│   │   │   ├── CategoriaFragment.kt       # Lista de palabras por categoría
│   │   │   ├── BusquedaFragment.kt        # Búsqueda de palabras
│   │   │   ├── DetallePalabraFragment.kt  # Detalle completo de palabra
│   │   │   └── FavoritosFragment.kt       # Lista de palabras favoritas
│   ├── res/
│   │   ├── layout/                        # Layouts XML de las pantallas
│   │   │   ├── activity_main.xml
│   │   │   ├── fragment_inicio.xml
│   │   │   ├── fragment_categoria.xml
│   │   │   ├── fragment_busqueda.xml
│   │   │   ├── fragment_detalle_palabra.xml
│   │   │   ├── fragment_favoritos.xml
│   │   │   └── item_palabra.xml          # Layout individual para cada palabra (cards)
│   │   ├── drawable/                      # Iconos (xml) y recursos gráficos (png)
│   │   │   ├── ic_familia.png
│   │   │   ├── ic_vestimenta.png
│   │   │   ├── ic_escuela.png
│   │   │   ├── ic_personas.png
│   │   │   ├── ic_alimentos.png
│   │   │   ├── ic_animales.png
│   │   │   ├── ic_favorite_filled.xml
│   │   │   ├── ic_favorite_border.xml
│   │   │   └── .....(más archivos)
│   │   ├── raw/                           # Archivos de audio (.m4a)
│   │   │   ├── palabra1.m4a
│   │   │   └── ... (más audios)
│   │   ├── font/                          # Fuentes personalizadas
│   │   │   ├── montserrat_bold.ttf
│   │   │   ├── montserrat_medium.ttf
│   │   │   └── montserrat_regular.ttf
│   │   ├── menu/                          # Menú de navegación inferior
│   │   │   └── bottom_menu.xml
│   │   └── values/                        # Colores, strings, estilos
│   │       ├── colors.xml
│   │       ├── strings.xml
│   │       └── themes.xml
│   └── assets/
│       └── diccionario_nahuat.db          # Base de datos precargada
```

## 📝 Descripción de Archivos Principales

| Archivo | Descripción |
| :--- | :--- |
| **PalabraAdapter.kt** | Adaptador que maneja el RecyclerView, vincula datos de palabras, maneja clics, favoritos y reproducción de audio. |
| **DBHelper.kt** | Gestiona la base de datos SQLite, copia desde assets y proporciona conexión. |
| **Palabra.kt** | Modelo de datos con propiedades: id, español, náhuat, categoría, imagen, audio, favorito. |
| **MainActivity.kt** | Actividad principal con BottomNavigationView para navegar entre fragments. |
| **InicioFragment.kt** | Pantalla de inicio con categorías y palabra aleatoria del día que cambia cada 24 horas. |
| **CategoriaFragment.kt** | Muestra lista de palabras por categoría con conteo real desde BD. |
| **BusquedaFragment.kt** | Búsqueda en tiempo real con sugerencias, muestra primeras 5 palabras al inicio. |
| **DetallePalabraFragment.kt** | Vista detallada con imagen, pronunciación, categoría y botón de favorito. |
| **FavoritosFragment.kt** | Lista de palabras marcadas como favoritas, con opción de eliminar. |

### ✨ Características Principales Del Proyecto
* **Navegación intuitiva:** Implementada con `BottomNavigationView` para un acceso rápido.
* **Categorías organizadas:** Secciones de Familia, Vestimenta, Escuela, Personas, Alimentos y Animales.
* **Palabra del día:** Sistema aleatorio que actualiza el contenido automáticamente cada 24 horas.
* **Búsqueda en tiempo real:** Filtro dinámico para encontrar términos tanto en náhuat como en español.
* **Reproducción de audio:** Integración con `MediaPlayer` para escuchar la pronunciación correcta.
* **Sistema de favoritos:** Persistencia de datos en SQLite para guardar palabras de interés.
* **Conteo real de palabras:** Indicadores dinámicos que muestran la cantidad de términos por categoría desde la BD.
* **Diseño Material Design 3:** Interfaz moderna, accesible y visualmente atractiva.
* **Primera letra capitalizada:** Formateo automático de texto para una mejor presentación visual.
