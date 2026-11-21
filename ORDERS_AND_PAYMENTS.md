# Carrito, órdenes y pasarela de pagos

Implementación del flujo de compra de ShopEasy:

- Carrito persistido en **Firestore** (`carts/{userId}`).
- Órdenes e historial persistidos en **Firestore** (`orders/{orderId}`).
- Pago con **ePayco Standard Checkout** dentro de un `WebView`.
- Confirmación de la transacción consultando la API pública de validación de ePayco.
- Modo local de respaldo si Firebase todavía no está configurado.

---

## 1. Configuración de Firebase (Firestore)

1. Crea un proyecto en <https://console.firebase.google.com>.
2. Habilita **Firestore Database** (modo producción).
3. Habilita **Authentication → Sign-in method → Anonymous**.
   La app usa una sesión anónima de Firebase únicamente para que las reglas de
   seguridad de Firestore tengan un `request.auth.uid`.
4. En **Configuración del proyecto → Tus apps → Android**, registra la app con
   el paquete `com.example.shopeasy`.
5. Copia estos valores de `google-services.json` a `local.properties`
   (puedes partir de `local.properties.example`):

```properties
FIREBASE_PROJECT_ID=tu-project-id
FIREBASE_APP_ID=1:1234567890:android:abcdef123456
FIREBASE_API_KEY=AIzaSy...
```

> No se aplica el plugin `com.google.gms.google-services`: Firebase se inicializa
> programáticamente en `FirebaseProvider`, de modo que el proyecto compila incluso
> antes de tener las llaves. Si están vacías, el carrito y las órdenes usan
> almacenamiento local (`SharedPreferences`) como respaldo.
>
> La sesión anónima de Firebase es independiente del login de Supabase: el
> historial en Firestore queda asociado al UID anónimo del dispositivo. Si se
> borran los datos de la app, se genera un UID nuevo y ese historial deja de ser
> accesible. Para producción conviene vincular ambos usuarios con un custom token
> emitido por el backend.

6. Publica las reglas de `firestore.rules`:

```bash
firebase deploy --only firestore:rules
```

También puedes pegarlas manualmente en **Firestore → Reglas**.

### Estructura de datos

`carts/{userId}`

```json
{
  "userId": "uid-anonimo-de-firebase",
  "updatedAt": 1763500000000,
  "items": [
    {
      "productId": "phone:12",
      "name": "iPhone 15",
      "price": 3500000.0,
      "imageUrl": "https://...",
      "quantity": 2
    }
  ]
}
```

`orders/{orderId}`

```json
{
  "id": "3f0b9c2d4e5a46f8a1b2c3d4e5f60718",
  "userId": "uid-anonimo-de-firebase",
  "customerName": "Ana Pérez",
  "customerEmail": "ana@correo.com",
  "items": [],
  "total": 7000000.0,
  "status": "PENDING",
  "createdAt": 1763500000000,
  "updatedAt": 1763500000000,
  "payment": {
    "refPayco": "68fb83729d094878e015be00",
    "transactionId": "123456",
    "response": "Aceptada",
    "approvalCode": "000000",
    "franchise": "VISA",
    "signatureValid": true
  }
}
```

Estados: `PENDING`, `PAID`, `FAILED`, `CANCELLED`.

---

## 2. Configuración de ePayco

1. Crea una cuenta de comercio en <https://epayco.com>.
2. En el dashboard entra a **Integraciones → Llaves API**.
3. Copia las llaves a `local.properties`:

```properties
EPAYCO_P_CUST_ID_CLIENTE=tu_customer_id
EPAYCO_P_KEY=tu_p_key
EPAYCO_PUBLIC_KEY=tu_public_key
EPAYCO_TEST=true
EPAYCO_CONFIRMATION_URL=
```

`EPAYCO_TEST=true` usa el ambiente de pruebas de ePayco. Cambia a `false` para
producción.

`EPAYCO_CONFIRMATION_URL` es la URL HTTPS del backend que recibirá el webhook
de confirmación. Es **opcional en la demo**: si está vacía no se envía
`p_url_confirmation` y la confirmación se hace consultando la API de validación
de ePayco desde la app. En producción es obligatoria y debe apuntar a tu
servidor.

### Datos que se envían al checkout

| Campo | Valor |
| --- | --- |
| `p_cust_id_cliente` | `EPAYCO_P_CUST_ID_CLIENTE` |
| `p_key` | `EPAYCO_P_KEY` |
| `p_id_invoice` | ID de la orden (32 caracteres, sin guiones) |
| `p_description` | Descripción del pedido |
| `p_amount` | Total de la orden |
| `p_amount_base` / `p_base_tax` | Total sin IVA (el proyecto no maneja IVA) |
| `p_tax` | `0.00` |
| `p_email` / `p_billing_email` | Correo del comprador |
| `p_currency_code` | `COP` |
| `p_signature` | Firma MD5 (ver abajo) |
| `p_test_request` | `TRUE` o `FALSE` según `EPAYCO_TEST` |
| `p_url_response` | `https://shopeasy.local/payment/result` (interceptada por el `WebView`) |
| `p_url_confirmation` | Solo si `EPAYCO_CONFIRMATION_URL` está configurada |
| `p_extra1` | ID de la orden |

La firma del formulario se calcula así:

```text
p_signature = MD5(p_cust_id_cliente ^ p_key ^ p_id_invoice ^ p_amount ^ p_currency_code)
```

> Para el Standard Checkout se usan `p_cust_id_cliente` y `p_key`. La llave
> privada (`PRIVATE_KEY`) **no** se usa en la app y nunca debe incluirse en el
> cliente.

---

## 3. Flujo de compra

1. **Agregar al carrito:** el botón *Añadir* de cada producto llama
   `CartRepository.addItem`. Si el producto ya existe, incrementa la cantidad.
2. **Carrito:** `ShoppingCardFragment` permite cambiar cantidades, eliminar
   productos, vaciar el carrito y ver el total.
3. **Crear orden:** al pagar se crea un documento en `orders` con estado
   `PENDING` y un ID de 32 caracteres (UUID sin guiones, longitud que acepta
   ePayco como `p_id_invoice`). La orden conserva una copia de los productos y
   del precio para que el historial no cambie si luego se edita el producto.
4. **Pago:** `PaymentActivity` carga el formulario de ePayco con
   `WebView.postUrl(...)`.
5. **Retorno:** ePayco redirige a `https://shopeasy.local/payment/result?ref_payco=...`.
   El `WebViewClient` intercepta esa URL (no se carga realmente).
6. **Confirmación:** se consulta
   `https://secure.epayco.co/validation/v1/reference/{ref_payco}` y se valida:
   - que la factura (`x_id_invoice`) corresponda a la orden,
   - que el monto aprobado coincida con el total,
   - que la moneda sea `COP`,
   - la firma `x_signature` cuando ePayco la devuelve.
7. **Actualización:** la orden pasa a `PAID`, `FAILED` o `PENDING`. Si fue
   aprobada, se vacía el carrito. El comprobante queda en el historial.
8. **Historial:** `OrderHistoryFragment` y `OrderDetailFragment` permiten
   consultar, reintentar el pago de una orden pendiente, cancelarla o eliminarla
   (los pedidos pagados no se eliminan para conservar el historial).

---

## 4. CRUD implementado

Carrito (`CartRepository`):

| Operación | Método |
| --- | --- |
| Crear / agregar ítem | `addItem` |
| Leer | `getCart` |
| Actualizar cantidad | `updateQuantity` |
| Eliminar ítem | `removeItem` |
| Vaciar carrito | `clear` |

Órdenes (`OrderRepository`):

| Operación | Método |
| --- | --- |
| Crear | `createOrder` |
| Leer historial | `getOrders` |
| Leer detalle | `getOrder` |
| Actualizar estado / pago | `updateStatus` |
| Eliminar | `deleteOrder` |

---

## 5. Archivos principales

```
data/
  FirebaseProvider.kt        Inicialización de Firebase y UID anónimo
  CartItem.kt                Modelo del carrito
  CartRepository.kt          CRUD del carrito
  Order.kt                   Modelos de orden y pago
  OrderRepository.kt         CRUD de órdenes
  LocalStore.kt              Respaldo local
payments/
  EpaycoConfig.kt            Llaves y URLs
  EpaycoPaymentService.kt    Checkout + validación de transacción
ui/main/cart/                Carrito
ui/main/orders/              Historial y detalle de pedidos
ui/payment/PaymentActivity   WebView y confirmación
```

---

## 6. Nota de seguridad para producción

La confirmación desde el cliente sirve para la demo y para el flujo de la
actividad, pero **no reemplaza el webhook de confirmación**. En producción:

- Define `EPAYCO_CONFIRMATION_URL` con un endpoint HTTPS de tu backend.
- Valida `x_signature` con `SHA256(p_cust_id_cliente^p_key^x_ref_payco^x_transaction_id^x_amount^x_currency_code)`.
- Actualiza la orden en el servidor, no en el dispositivo.
- Nunca expongas `P_KEY` ni la llave privada en el APK.
