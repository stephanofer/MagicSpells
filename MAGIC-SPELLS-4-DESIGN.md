# MagicSpells 4.0 — Diseño de la actualización mayor

## 1. Estado del documento

- **Estado:** Aprobado para implementación
- **Versión objetivo:** 4.0.0
- **Servidor objetivo:** PandaSpigot 1.8.8
- **Java objetivo:** Java 8
- **Naturaleza del producto:** Plugin privado para la infraestructura del servidor
- **Integración principal:** RPGItems mediante API en memoria

Este documento formaliza las decisiones arquitectónicas y funcionales acordadas para MagicSpells 4.0. Su propósito es definir el alcance de la actualización mayor, los límites entre componentes, el contrato público de integración y los criterios que deberá cumplir la implementación.

MagicSpells 4.0 no pretende conservar funcionalidades por compatibilidad histórica cuando estas no pertenecen al uso real del servidor. Sí debe conservar el formato de configuración y las capacidades útiles para construir y ejecutar los hechizos actuales y futuros.

---

## 2. Visión del producto

MagicSpells dejará de ser un sistema completo de progresión mágica para jugadores y se convertirá en:

> Un motor privado de carga, validación y ejecución de hechizos para PandaSpigot 1.8.8, consumido por plugins confiables mediante una API en memoria y administrado mediante comandos restringidos.

El sistema tendrá dos entradas válidas para ejecutar hechizos:

1. La API pública utilizada por plugins confiables, principalmente RPGItems.
2. Los comandos administrativos de MagicSpells.

No existirá ninguna ruta pública para que un jugador lance directamente un hechizo mediante comandos, chat, objetos vinculados o sistemas internos de progresión.

### 2.1 Objetivos

- Integración directa, segura y tipada con RPGItems.
- Compatibilidad con el formato YAML de los hechizos existentes.
- Conservación de las capacidades útiles del motor de hechizos.
- Eliminación de subsistemas ajenos al uso real del servidor.
- Ejecución sin comandos, chat simulado ni permisos temporales.
- Cero acceso a disco en el camino normal de lanzamiento.
- Recargas globales, transaccionales y auditables.
- Runtime determinista y completamente limpiable.
- Soporte exclusivo para PandaSpigot 1.8.8.
- API pública estable y separada de la implementación.

### 2.2 No objetivos

- Mantener compatibilidad con otras versiones de Minecraft.
- Mantener compatibilidad con distribuciones públicas genéricas de MagicSpells.
- Proporcionar progresión, aprendizaje o spellbooks.
- Permitir extensiones dinámicas mediante JARs externos.
- Permitir mutaciones del registro de hechizos durante runtime.
- Modificar el codebase de RPGItems como parte de este proyecto.
- Crear servidores simulados, harnesses automáticos o entornos Docker para validar el plugin.

---

## 3. Límites de responsabilidad

### 3.1 RPGItems

RPGItems será responsable de:

- Detectar la activación del objeto.
- Verificar el RPGItem, su propietario y sus condiciones de uso.
- Determinar el trigger aplicable.
- Administrar y mostrar el cooldown.
- Administrar durabilidad, usos y demás estado propio del objeto.
- Solicitar el lanzamiento mediante la API de MagicSpells.
- Confirmar el cooldown únicamente cuando MagicSpells responda `ACCEPTED`.

Este proyecto no modificará RPGItems. Se producirá posteriormente una especificación de integración para el equipo responsable de ese plugin.

### 3.2 MagicSpells

MagicSpells será responsable de:

- Descubrir, leer y validar archivos de hechizos.
- Construir un registro inmutable de definiciones válidas.
- Resolver hechizos por su ID interno.
- Validar caster, mundo, regiones, modificadores y reactivos.
- Ejecutar hechizos y subhechizos.
- Administrar proyectiles, cast times, buffs, efectos y tareas propias.
- Devolver resultados tipados al solicitante.
- Emitir eventos públicos de integración.
- Administrar comandos exclusivamente administrativos.
- Cancelar y limpiar todo su runtime durante reload y disable.

MagicSpells no administrará cooldowns de RPGItems ni verificará qué RPGItem originó una solicitud. La autorización para activar el objeto pertenece a RPGItems.

---

## 4. Compatibilidad del formato YAML

La estructura actual de los archivos `spells-*.yml` se conservará. La actualización no impondrá una reescritura del catálogo de hechizos.

### 4.1 Elementos conservados

Permanecerán compatibles:

- IDs internos actuales, incluidas cadenas ofuscadas o hashes.
- `spell-class` y sus nombres relativos.
- Referencias entre hechizos.
- `MultiSpell` y `TargetedMultiSpell`.
- Entradas `DELAY`.
- Efectos declarados como listas o secciones.
- Partículas y sonidos.
- Proyectiles y colisiones.
- Targeting de entidades y ubicaciones.
- Line of sight.
- Modificadores y condiciones.
- Cast times e interrupciones.
- Buffs, debuffs y hechizos canalizados.
- Variables.
- Restricciones de mundo y región.
- Reactivos distintos de mana.
- Nombres de clase relativos como `.instant.ParticleProjectileSpell`.

El renombrado de hashes a IDs semánticos será opcional y no formará parte de la migración obligatoria.

### 4.2 Propiedades retiradas

Una propiedad que pertenecía a una funcionalidad eliminada:

- Será reconocida como propiedad heredada.
- No tendrá efecto en runtime.
- No impedirá la carga por sí sola.
- Generará una advertencia agregada durante startup, validate o reload.
- Podrá localizarse con precisión mediante `/ms validate`.

El log normal no deberá emitir una línea por cada ocurrencia. Debe agrupar propiedades por tipo y cantidad para evitar ruido. El comando de validación proporcionará archivo y ruta YAML cuando se requiera el detalle.

Ejemplo de resumen:

```text
[MagicSpells] Found 37 deprecated properties:
- cooldown: 21 occurrences
- bindable: 6 occurrences
- mana cost: 10 occurrences
Run /ms validate for file and path details.
```

### 4.3 Propiedades desconocidas

Las propiedades desconocidas que no correspondan a una propiedad heredada se reportarán como advertencias con su ubicación. La compatibilidad con archivos existentes tiene prioridad sobre un rechazo automático por claves adicionales, pero ninguna clave desconocida deberá ignorarse silenciosamente.

### 4.4 Errores que cancelan la carga

La carga completa se rechazará ante cualquiera de estas condiciones:

- Clase de hechizo inexistente o eliminada.
- ID duplicado.
- Referencia a un hechizo inexistente.
- Tipo inválido para una propiedad conocida.
- Valor obligatorio ausente.
- Dependencia requerida ausente.
- Ciclo de referencias no permitido.
- Error al construir o validar una definición.

No se permitirá un registro parcialmente válido. En startup, un error impedirá habilitar MagicSpells. En reload, el registro anterior permanecerá activo.

---

## 5. Funcionalidades eliminadas

### 5.1 Mana

Se eliminarán:

- Sistema de mana.
- Barras de mana.
- Regeneración de mana.
- Pociones de mana.
- Mana como reactivo.
- Comandos y mensajes exclusivos de mana.
- Permisos relacionados con mana.
- `ManaSpell`, `ManaRegenSpell` y cualquier clase cuya única responsabilidad dependa del mana.

Si una lista de costos heredada contiene mana junto a otros reactivos, la entrada de mana se ignorará y reportará; los demás reactivos conservarán su comportamiento.

### 5.2 Progresión y spellbooks

Se eliminarán:

- Aprendizaje de hechizos.
- Enseñanza y olvido.
- Prerrequisitos de aprendizaje.
- XP mágica.
- Rangos mágicos.
- Spellbooks por jugador.
- Persistencia de hechizos aprendidos.
- Permisos `grant`, `tempgrant`, `learn`, `teach` y `cast.<spell>`.
- Clases como `TeachSpell`, `ForgetSpell`, `TomeSpell`, `SpellbookSpell` y equivalentes.

### 5.3 Vinculación y activación interna

Se eliminarán:

- Bind y unbind.
- Selección y ciclo de hechizos.
- Cast items como mecanismo de activación.
- Clic izquierdo o derecho administrado por MagicSpells.
- Activación por consumo administrada por MagicSpells.
- Incantaciones mediante chat.
- Dance casting.
- Listeners destinados exclusivamente a estas rutas.
- Clases de comando que solo existen para operar estas funcionalidades.

### 5.4 Hechizos pasivos

Se eliminarán completamente:

- `PassiveSpell`.
- `PassiveManager`.
- `PassiveTrigger`.
- Triggers por daño, muerte, movimiento, bloques, pesca, ticks u otros eventos.
- Listeners cuya única función sea sostener el sistema pasivo.

Los buffs y efectos temporales iniciados explícitamente por un lanzamiento no son hechizos pasivos y permanecerán disponibles.

### 5.5 Cooldowns de MagicSpells

Se eliminarán:

- Cooldown por hechizo.
- Cooldown global.
- Cooldown compartido.
- Cooldown de servidor.
- Persistencia en `cooldowns.txt`.
- Estado `ON_COOLDOWN`.
- Mensajes y sonidos exclusivos de cooldown.
- Permiso `magicspells.nocooldown`.
- Clases como `ModifyCooldownSpell`, `OffhandCooldownSpell` y equivalentes.

Los `DELAY`, intervalos, duraciones, cast times y períodos de efectos no son cooldowns y permanecerán intactos.

### 5.6 Addons y compatibilidad generalista

Se eliminarán del proyecto y del build:

- MagicSpellsTeams.
- MagicSpellsMemory.
- MagicSpellsShop.
- MagicSpellsTowny.
- MagicSpellsFactions.
- Compatibilidad con Minecraft 1.9, 1.10, 1.11 y 1.12.
- Compatibilidad NMS diferente de PandaSpigot 1.8.8.
- Detección dinámica de versiones de servidor.
- Carga de JARs de hechizos desde la carpeta del plugin.
- Descarga o actualización remota de archivos de configuración.
- Métricas o telemetría externas.

### 5.7 Datos heredados

MagicSpells 4.0 no borrará automáticamente:

- Carpetas de spellbooks.
- `cooldowns.txt`.
- Archivos de mana.
- JARs de addons antiguos.

La actualización no realizará eliminaciones destructivas. Los datos retirados dejarán de leerse y escribirse, y su eliminación manual podrá documentarse durante la migración.

---

## 6. Capacidades conservadas

Se mantendrán todas las clases y utilidades que proporcionen capacidades válidas para construir y ejecutar hechizos actuales o futuros, excepto cuando su responsabilidad completa dependa de un subsistema eliminado.

Esto incluye:

- Hechizos instantáneos.
- Hechizos dirigidos.
- Hechizos de ubicación.
- Proyectiles y proyectiles con tracking.
- Rayos, partículas, sonidos y efectos visuales.
- Multi-spells.
- Buffs y debuffs explícitos.
- Cast times e interrupciones.
- Hechizos canalizados.
- Targeting y line of sight.
- Variables y valores dinámicos.
- Modificadores y condiciones.
- Zonas y restricciones regionales.
- Efectos sobre entidades, bloques, inventarios y mundo.
- Reactivos de objetos, salud, hambre, experiencia, niveles, durabilidad, dinero y variables.

---

## 7. Organización del proyecto

El proyecto se dividirá en dos módulos Gradle.

```text
magicspells-api
magicspells-plugin
```

### 7.1 `magicspells-api`

Contendrá exclusivamente contratos públicos estables:

- `MagicSpellsApi`.
- `SpellCastRequest`.
- `SpellCastResult`.
- `SpellCastStatus`.
- `SpellDescriptor`.
- Eventos públicos.

Generará un artefacto independiente para dependencias `compileOnly` de otros proyectos.

### 7.2 `magicspells-plugin`

Contendrá:

- Bootstrap de Bukkit.
- Loader y validator.
- Registro de hechizos.
- Motor de ejecución.
- Clases de hechizos.
- Efectos y modificadores.
- Integraciones externas.
- Runtime tracker.
- Comandos administrativos.

El JAR instalable de MagicSpells incluirá las clases de `magicspells-api`. El servidor no requerirá instalar un JAR adicional para la API.

### 7.3 Identidad y compatibilidad de paquetes

Se conservarán:

- Nombre del plugin: `MagicSpells`.
- Carpeta de datos: `plugins/MagicSpells`.
- Descubrimiento de `spells-*.yml`.
- Namespace interno necesario para resolver nombres relativos de `spell-class`.

La API pública residirá en un paquete dedicado, por ejemplo `com.nisovin.magicspells.api`, y no expondrá tipos de implementación.

---

## 8. Arquitectura interna

La implementación se organizará alrededor de componentes concretos con responsabilidades delimitadas, sin introducir capas ceremoniales innecesarias.

### 8.1 Loader

Responsable de:

- Descubrir archivos.
- Leer YAML.
- Construir definiciones candidatas.
- Registrar propiedades heredadas.
- Mantener información de archivo y ruta para diagnósticos.

La construcción de un candidato no deberá activar listeners, tareas ni efectos.

### 8.2 Validator

Responsable de:

- Verificar IDs únicos.
- Resolver referencias.
- Validar tipos y valores.
- Detectar ciclos inválidos.
- Determinar integraciones requeridas.
- Rechazar clases retiradas.
- Generar un reporte estructurado.

### 8.3 Spell registry

El registro activo será un snapshot inmutable con búsqueda O(1) por ID interno. Las definiciones solo cambiarán mediante un reload global exitoso.

### 8.4 Spell engine

Responsable de:

- Procesar `SpellCastRequest`.
- Ejecutar validaciones previas.
- Emitir eventos.
- Iniciar la ejecución.
- Convertir resultados internos al contrato público.
- Capturar y contextualizar errores.

### 8.5 Runtime tracker

Toda ejecución activa pertenecerá a una generación de runtime. El tracker registrará:

- Tareas de Bukkit.
- Listeners temporales.
- Cast times.
- Proyectiles administrados.
- Buffs y debuffs.
- Efectos repetitivos.
- Recursos que requieran limpieza.

Reload y disable deberán poder cancelar una generación completa sin depender de limpiezas parciales dispersas.

### 8.6 Integration registry

Responsable de detectar integraciones opcionales una sola vez durante startup o reload y exponer sus capacidades al validator y al runtime.

---

## 9. API pública

### 9.1 Contrato principal

El contrato conceptual será:

```java
public interface MagicSpellsApi {

    Optional<SpellDescriptor> findSpell(String spellId);

    Collection<SpellDescriptor> getSpells();

    SpellCastResult cast(SpellCastRequest request);
}
```

Los nombres finales podrán ajustarse durante la implementación sin alterar las responsabilidades definidas en este documento.

### 9.2 `SpellCastRequest`

Será inmutable y contendrá como mínimo:

- Plugin solicitante.
- Jugador que actuará como caster.
- ID interno exacto del hechizo.
- Multiplicador de poder.
- Lista inmutable de argumentos.

La API no dependerá de clases de RPGItems ni aceptará un RPGItem como argumento.

### 9.3 `SpellCastResult`

Será inmutable e incluirá:

- Estado tipado.
- ID solicitado.
- Indicador `isAccepted()`.
- Información técnica segura para diagnóstico cuando corresponda.

Estados públicos mínimos:

```java
ACCEPTED
SPELL_NOT_FOUND
INVALID_REQUEST
CASTER_UNAVAILABLE
MISSING_REAGENTS
WRONG_WORLD
BLOCKED
CANCELLED
INTERNAL_ERROR
```

### 9.4 Reglas de la API

- Solo podrá utilizarse desde el hilo principal de Bukkit.
- Una llamada async no se reprogramará silenciosamente.
- No expondrá `Spell`, managers, configuraciones ni colecciones mutables internas.
- No permitirá registrar, reemplazar o eliminar hechizos.
- No proporcionará operaciones de aprendizaje, binding, mana o cooldown.
- No ejecutará comandos ni chat.
- No escribirá en disco durante `cast`.
- No comprobará permisos por hechizo.

La API se publicará mediante el `ServicesManager` de Bukkit después de una carga inicial válida y se retirará antes de deshabilitar el plugin.

### 9.5 Consulta sin mutación

Los plugins consumidores podrán:

- Comprobar si un ID existe.
- Consultar descriptores inmutables.
- Enumerar hechizos disponibles.
- Solicitar lanzamientos.
- Escuchar eventos públicos.

No podrán modificar el registro activo.

---

## 10. Semántica de ejecución

### 10.1 Flujo síncrono

```text
SpellCastRequest
    -> validar hilo y solicitud
    -> resolver ID
    -> validar caster
    -> validar mundo, región y modificadores
    -> comprobar reactivos
    -> emitir evento previo cancelable
    -> aceptar lanzamiento
    -> iniciar ejecución inmediata o diferida
    -> devolver SpellCastResult
```

### 10.2 Significado de `ACCEPTED`

`ACCEPTED` significa que MagicSpells aceptó e inició el lanzamiento. No significa que todos sus efectos hayan terminado.

Esto cubre:

- Hechizos inmediatos.
- Cast times.
- Proyectiles.
- Secuencias con `DELAY`.
- Buffs y efectos prolongados.

### 10.3 Interrupciones posteriores

Una vez devuelto `ACCEPTED`, una interrupción posterior no revierte el cooldown administrado por RPGItems. Esto incluye movimiento, daño, muerte, desconexión o cancelación posterior de un cast time.

Si la solicitud se rechaza antes de `ACCEPTED`, RPGItems no debe confirmar el cooldown.

### 10.4 Errores

- Una excepción síncrona antes de la aceptación devolverá `INTERNAL_ERROR` y no aceptará el cast.
- Una excepción posterior a `ACCEPTED` se reportará con el contexto de ejecución y no restaurará el cooldown externo.
- Ninguna excepción deberá propagarse sin contexto al loop principal del servidor.

---

## 11. Eventos públicos

La API proporcionará eventos que no expongan tipos internos:

- Evento previo cancelable.
- Evento de lanzamiento aceptado.
- Evento de lanzamiento rechazado.
- Evento de cast time interrumpido.

No se definirá un evento universal de “hechizo completamente terminado”. Proyectiles, buffs y multi-spells tienen semánticas de finalización diferentes; presentar una abstracción única produciría información incorrecta.

---

## 12. Integración recomendada con RPGItems

Esta sección define la integración que se documentará para el equipo de RPGItems. No autoriza cambios en su codebase dentro de este proyecto.

### 12.1 Dependencia

RPGItems deberá declarar MagicSpells como dependencia obligatoria y utilizar `magicspells-api` como dependencia de compilación `compileOnly`.

Durante enable deberá obtener `MagicSpellsApi` mediante `ServicesManager`. La ausencia o incompatibilidad del servicio deberá impedir que RPGItems inicie en un estado parcialmente funcional.

### 12.2 Poder recomendado

Se recomendará un poder específico `magicspell`:

```yaml
powers:
  '0':
    powerName: magicspell
    spell: 2XiCPuApHPKg9aivwRii
    cooldown: 12000
    display: Magia de Oscuridad
    isRight: true
```

La migración elimina:

```yaml
command: cast 2XiCPuApHPKg9aivwRii
permission: magicspells.grant.2XiCPuApHPKg9aivwRii
```

### 12.3 Flujo recomendado

```text
activar RPGItem
    -> validar objeto y trigger
    -> comprobar disponibilidad del cooldown sin consumirlo
    -> construir SpellCastRequest
    -> MagicSpellsApi.cast(request)
    -> si ACCEPTED: confirmar cooldown
    -> si es rechazado: conservar cooldown disponible
```

RPGItems no deberá:

- Crear `PermissionAttachment`.
- Conceder permisos temporales.
- Convertir al jugador en operador.
- Ejecutar `player.chat()`.
- Despachar `/cast`.
- Consultar clases internas de MagicSpells.

### 12.4 Validación de configuración

RPGItems debería validar cada ID de hechizo durante la carga de sus objetos mediante `findSpell`. Un ID inexistente debe generar un error de configuración temprano, no descubrirse durante combate.

---

## 13. Comandos administrativos

El único comando raíz será `/magicspells`, con alias `/ms`.

```text
/ms validate
/ms reload
/ms cast <spell> [args...]
/ms cast --player <player> <spell> [args...]
/ms list
/ms info <spell>
/ms status
```

### 13.1 Semántica

- `validate`: construye y valida un candidato sin modificar el runtime.
- `reload`: ejecuta una recarga global transaccional.
- `cast`: realiza un lanzamiento administrativo normal.
- `list`: enumera IDs disponibles.
- `info`: muestra origen, clase, dependencias y datos operativos de un hechizo.
- `status`: muestra versión, API, integraciones, cantidad de hechizos, ejecuciones activas y último reload.

Si un jugador ejecuta `cast` sin `--player`, él será el caster. Desde consola, `--player` será obligatorio.

No existirá `forcecast`. El lanzamiento administrativo respetará las mismas validaciones normales de mundo, región, modificadores y reactivos.

### 13.2 Permisos

```text
magicspells.admin.validate
magicspells.admin.reload
magicspells.admin.cast
magicspells.admin.list
magicspells.admin.info
magicspells.admin.status
magicspells.admin.*
```

Los permisos administrativos tendrán `default: op`. No habrá permisos por hechizo.

Se eliminarán `/cast`, `/c`, `/mana`, `/magicxp` y cualquier ruta de lanzamiento para jugadores normales.

---

## 14. Reload transaccional

La validación y el reload serán siempre globales. No habrá recarga por archivo ni por hechizo.

### 14.1 Construcción del candidato

```text
descubrir archivos
    -> leer configuración
    -> construir definiciones inactivas
    -> resolver referencias
    -> verificar dependencias
    -> validar el conjunto completo
```

La construcción deberá estar libre de efectos secundarios. No podrá registrar listeners, programar tareas ni modificar el snapshot activo.

### 14.2 Fallo

Si el candidato es inválido:

- El snapshot anterior permanece activo.
- Las ejecuciones actuales continúan.
- No se cancelan tareas ni efectos.
- La API permanece disponible con la generación anterior.
- Se emite un reporte completo.

### 14.3 Éxito

Si el candidato es válido:

```text
cerrar admisión en la generación anterior
    -> cancelar runtime anterior
    -> limpiar tareas, listeners, buffs, proyectiles y trackers
    -> intercambiar el snapshot activo
    -> activar la nueva generación
    -> reabrir admisión
```

Todo el cambio se realizará en el hilo principal, evitando casts concurrentes durante el intercambio.

---

## 15. Dependencias

### 15.1 Obligatorias

- PandaSpigot API 1.8.8.
- EffectLib mientras continúe sosteniendo capacidades centrales del motor.

### 15.2 Opcionales

- WorldGuard y WorldEdit.
- ProtocolLib.
- LibsDisguises.
- PlaceholderAPI.
- Vault.

### 15.3 Política de disponibilidad

- Integración ausente y no utilizada: MagicSpells inicia normalmente.
- Integración ausente y utilizada por una definición: se rechaza la carga completa.
- Integración disponible: se activa una vez y queda registrada para validación y ejecución.
- Nunca se permitirá cargar una definición que solo pueda fallar posteriormente por una dependencia ausente.

### 15.4 Build

El build eliminará los JARs locales de múltiples versiones de Spigot y utilizará PandaSpigot como API principal:

```groovy
repositories {
    mavenCentral()
    maven {
        url = 'https://repo.hpfxd.com/releases/'
    }
}

dependencies {
    compileOnly 'com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT'
}
```

Las dependencias opcionales serán `compileOnly`. Las dependencias embebidas se limitarán a las estrictamente necesarias y deberán reubicarse cuando exista riesgo de colisión.

---

## 16. Rendimiento y modelo de ejecución

### 16.1 Camino caliente

Un lanzamiento normal deberá realizar:

- Búsqueda O(1) del hechizo.
- Validaciones en memoria.
- Ejecución directa del motor.

No deberá realizar:

- Acceso a disco.
- Escritura de spellbooks.
- Mutación de permisos.
- Recalculo del árbol de permisos.
- Chat simulado.
- Dispatch de comandos.
- Detección de versión.
- Resolución repetida de integraciones.
- Persistencia de cooldowns.

### 16.2 Threading

Las operaciones Bukkit y los lanzamientos se ejecutarán en el hilo principal. No se introducirán operaciones async alrededor de entidades, mundos, inventarios o scheduler.

La recarga es una operación administrativa poco frecuente. El tamaño esperado de los YAML no justifica añadir concurrencia compleja al loader.

### 16.3 Estado por jugador

Cuando una capacidad conservada requiera estado temporal por jugador, utilizará UUID y deberá retirarlo en quit, reload y disable.

---

## 17. Mensajes y ownership de experiencia

MagicSpells conservará los mensajes pertenecientes al hechizo:

- Lanzamiento exitoso.
- Falta de objetivo.
- Reactivos insuficientes.
- Mundo o región inválidos.
- Interrupción.

RPGItems será responsable de mensajes relacionados con:

- Cooldown del objeto.
- Restricciones del RPGItem.
- Durabilidad o usos del objeto.
- Disponibilidad de la integración.

La API devolverá estados técnicos y no mensajes de gameplay ya formateados.

---

## 18. Logging y observabilidad

### 18.1 Errores de configuración

Cada error deberá incluir cuando sea aplicable:

- Archivo.
- Ruta YAML.
- ID del hechizo.
- Clase solicitada.
- Valor problemático.
- Causa concreta.
- Definiciones que lo referencian.
- Acción recomendada.

Ejemplo:

```text
[MagicSpells] Configuration load rejected.
File: spells-regular.yml
Path: spells.old_mana_spell.spell-class
Value: .instant.ManaSpell
Reason: ManaSpell was removed with the mana subsystem.
Referenced by: legendary_weapon_multi
Action: Remove or replace this spell definition.
The previous spell registry remains active.
```

### 18.2 Errores de runtime

Los errores de ejecución incluirán:

- ID de ejecución.
- ID del hechizo.
- UUID del caster.
- Plugin solicitante.
- Generación del runtime.
- Fase de ejecución.
- Excepción original.

Los errores repetitivos deberán limitarse o agruparse para proteger la consola.

### 18.3 Estado operativo

`/ms status` mostrará como mínimo:

- Versión del plugin.
- Versión del contrato API.
- Estado del servicio.
- Cantidad de hechizos cargados.
- Integraciones disponibles.
- Número de ejecuciones activas.
- Resultado y fecha del último reload.

No se enviará telemetría fuera del servidor.

---

## 19. Seguridad

La seguridad no dependerá de ocultar nombres de hechizos.

MagicSpells 4.0 eliminará:

- El comando público `/cast`.
- Autocompletado público de hechizos.
- Permisos temporales de grant.
- Aprendizaje automático por permisos.
- Uso del pipeline de chat y comandos para integración.

Los IDs ofuscados podrán conservarse por compatibilidad, pero no se considerarán secretos.

La API será accesible únicamente para plugins instalados y confiables. Los comandos administrativos estarán protegidos por permisos generales y no existirán permisos por hechizo.

---

## 20. Estrategia de validación

### 20.1 Pruebas automatizadas permitidas

Se crearán pruebas aisladas para:

- Compatibilidad del formato YAML heredado.
- Construcción de clases de hechizo conservadas.
- Resolución de referencias entre archivos.
- Reporte de propiedades heredadas.
- Rechazo de clases eliminadas.
- IDs duplicados y referencias inválidas.
- Dependencias opcionales ausentes y presentes.
- Reload transaccional.
- Inmutabilidad del snapshot.
- Limpieza del runtime.
- Resultados de la API.
- Separación entre API e implementación.
- Compatibilidad con bytecode Java 8.

### 20.2 Validación manual

La validación final deberá realizarse en el servidor real PandaSpigot 1.8.8 e incluir:

- Startup con configuraciones reales.
- Validate y reload exitosos y fallidos.
- Lanzamiento administrativo.
- Integración implementada por el equipo de RPGItems.
- Cast inmediato y diferido.
- Proyectiles y multi-spells.
- Reactivos.
- Restricciones WorldGuard.
- Interrupciones.
- Reload con ejecuciones activas.
- Disable y limpieza.
- Observación de TPS, errores y comportamiento durante combate.

No se crearán smoke-test scripts, servidores simulados, Docker ni procedimientos automáticos de arranque salvo solicitud explícita.

---

## 21. Plan de implementación

La implementación seguirá este orden para reducir el riesgo:

1. Establecer PandaSpigot 1.8.8 y Java 8 como única plataforma.
2. Convertir el proyecto en `magicspells-api` y `magicspells-plugin`.
3. Definir y probar el contrato público.
4. Separar loader, validator, registry, engine y runtime tracker.
5. Mantener operativas las clases de hechizos conservadas.
6. Eliminar addons y compatibilidad multiversión.
7. Eliminar mana, progresión, spellbooks, bindings, pasivos y cooldowns.
8. Añadir diagnóstico de propiedades heredadas.
9. Implementar reload global transaccional.
10. Implementar comandos administrativos.
11. Implementar integraciones opcionales validadas.
12. Completar pruebas automatizadas aisladas.
13. Documentar la integración recomendada con RPGItems.
14. Realizar validación manual en el servidor real.

Cada etapa deberá mantener el proyecto compilable y evitar mezclar una reestructuración completa con cambios funcionales no verificados en un único paso.

---

## 22. Criterios de aceptación

MagicSpells 4.0 se considerará completo cuando se cumplan todos los siguientes criterios:

1. Compila para Java 8 contra PandaSpigot 1.8.8.
2. Produce un artefacto API y un JAR instalable del plugin.
3. No contiene addons ni compatibilidad con otras versiones.
4. No contiene mana, progresión, spellbooks, binding, pasivos ni cooldowns internos.
5. Conserva el formato YAML de las capacidades soportadas.
6. Reporta propiedades retiradas sin romper la carga.
7. Rechaza configuraciones estructuralmente inválidas sin publicar registros parciales.
8. Expone `MagicSpellsApi` mediante `ServicesManager`.
9. Permite consultar y lanzar hechizos sin exponer implementación interna.
10. No permite mutar dinámicamente el registro.
11. No registra comandos públicos de cast.
12. Solo ofrece la superficie administrativa definida.
13. Un cast no crea permisos, no simula chat, no despacha comandos y no escribe en disco.
14. RPGItems puede administrar el cooldown utilizando `ACCEPTED` como punto de confirmación.
15. Un reload inválido conserva el runtime anterior.
16. Un reload válido cancela y limpia por completo la generación anterior.
17. Las dependencias opcionales se exigen únicamente cuando una definición las utiliza.
18. Los errores contienen contexto suficiente para ser auditados y corregidos.
19. Las pruebas aisladas pasan y el bytecode permanece compatible con Java 8.
20. La validación manual en PandaSpigot confirma el comportamiento esperado.

---

## 23. Decisión final

MagicSpells 4.0 será un motor de hechizos especializado para la infraestructura privada del servidor. Mantendrá las capacidades de creación y ejecución que aportan valor, conservará la estructura YAML existente y eliminará todos los sistemas que introducen progresión, activación pública, estado persistente innecesario o compatibilidad ajena a PandaSpigot 1.8.8.

La integración con RPGItems será directa, obligatoria y en memoria, pero su implementación se realizará en el proyecto de RPGItems por su equipo responsable. MagicSpells proporcionará un contrato público pequeño, estable, tipado y auditable.
