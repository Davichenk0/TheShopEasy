# Manual Técnico - ShopEasy Android

## 1. Introducción
ShopEasy es una aplicación móvil de comercio electrónico desarrollada en Kotlin, diseñada para conectar con un backend en .NET mediante una arquitectura robusta y escalable.

## 2. Arquitectura
La aplicación sigue el patrón **MVVM (Model-View-ViewModel)**, separando la lógica de negocio de la interfaz de usuario.

- **Capa de Datos (Data):** Contiene los modelos (`Device`, `Product`), repositorios (`DeviceRepository`) y la gestión de persistencia local (`TokenManager`).
- **Capa de Red (Network):** Implementada con **Retrofit**, gestiona las llamadas a la API REST. Incluye un `AuthInterceptor` para adjuntar tokens JWT y un bypass de SSL para entornos de desarrollo.
- **Capa de Interfaz (UI):** Fragmentos y Actividades que utilizan `ViewModel` para observar cambios en los datos.

## 3. Tecnologías y Librerías
- **Kotlin:** Lenguaje principal.
- **Retrofit 2 & OkHttp:** Comunicación con la API REST.
- **Coroutines:** Manejo de tareas asíncronas.
- **Glide:** Carga y caché de imágenes.
- **Biometric API:** Autenticación segura mediante huella dactilar o reconocimiento facial.
- **Gson:** Serialización y deserialización de JSON.

## 4. Integración con Backend (.NET API)
La comunicación se realiza mediante los endpoints definidos en el `swagger.json`.
- **Autenticación:** `/api/Auth/login` y `/api/Auth/register`.
- **Productos (Devices):** CRUD completo (GET, POST, PUT, DELETE) en `/api/Device`.
- **Filtros:** Obtención de marcas (`/api/Brand`) y categorías (`/api/Category`).

## 5. Configuración de Desarrollo
El archivo `RetrofitClient.kt` contiene la `BASE_URL`. Para pruebas locales, se utiliza la IP de la máquina host (ej. `https://192.168.1.8:7276/api/`). Se ha implementado un `X509TrustManager` personalizado para permitir certificados de desarrollo no firmados.

## 6. Estructura de Paquetes
- `com.example.shopeasy.data`: Modelos de datos y Repositorios.
- `com.example.shopeasy.network`: Configuración de Retrofit e Interceptores.
- `com.example.shopeasy.ui`: Componentes de UI organizados por módulos (Auth, Main, Products, Profile).
