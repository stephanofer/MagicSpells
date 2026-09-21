# MagicSpells 4.0 — Plan de trabajo

## 1. Propósito

Este documento organiza la implementación de MagicSpells 4.0 en tres bloques de trabajo amplios, ordenados según sus dependencias técnicas. El objetivo es permitir que varios integrantes del equipo trabajen simultáneamente dentro de cada bloque sin comenzar funcionalidades que dependan de bases todavía inexistentes ni interrumpir trabajo avanzado para resolver requisitos previos.

Este plan no reemplaza ni repite la especificación del producto. Todo el detalle funcional, arquitectónico, contractual y operativo de cada bloque se encuentra en [`MAGIC-SPELLS-4-DESIGN.md`](./MAGIC-SPELLS-4-DESIGN.md), que será la fuente de verdad durante la implementación. Aquí se establece qué debe quedar resuelto en cada bloque, en qué orden y bajo qué condiciones puede avanzarse al siguiente.

El documento de diseño representa la formalización de las ideas y la solución acordada inicialmente por el equipo. No debe tratarse como una restricción ciega frente a evidencia obtenida durante el desarrollo. Si aparece un problema no previsto o una alternativa demostrablemente más simple, segura o eficiente, deberá comunicarse al equipo antes de aplicarse, documentar su impacto y actualizar la decisión correspondiente. No se introducirán desviaciones locales o silenciosas.

## 2. Forma de ejecución

Los tres bloques se ejecutarán secuencialmente. No se abrirá formalmente un bloque hasta cumplir la puerta de salida del anterior.

Dentro de cada bloque podrán trabajar varios integrantes en paralelo sobre frentes distintos. Todos los frentes deberán respetar los contratos y límites compartidos del bloque, integrar cambios con frecuencia y mantener el proyecto compilable. La división interna del trabajo no deberá crear implementaciones alternativas, registros paralelos ni mecanismos privados de lifecycle o limpieza.

```text
Bloque 1: Fundaciones y carga confiable
    -> Bloque 2: Motor funcional y runtime controlado
        -> Bloque 3: Operación transaccional e integración final
```

## 3. Bloque 1 — Fundaciones, contratos y carga confiable

### 3.1 Objetivo

Construir la nueva base del proyecto antes de modificar profundamente el motor. Al finalizar este bloque deberá existir una plataforma compilable y estable sobre la cual el resto del equipo pueda implementar y migrar funcionalidades sin depender de estructuras destinadas a desaparecer.

### 3.2 Alcance

- Fijar Java 8 y PandaSpigot 1.8.8 como única plataforma soportada.
- Dividir el proyecto en los módulos `magicspells-api` y `magicspells-plugin`.
- Producir un artefacto independiente para la API y un JAR instalable que incluya sus clases.
- Eliminar del build los addons, la compilación multiversión y las dependencias que no pertenecen al producto objetivo.
- Definir los contratos públicos estables: API, request, result, estados, descriptor y eventos, sin exponer tipos de implementación.
- Inventariar y clasificar las clases, propiedades y capacidades existentes como conservadas, heredadas sin efecto, eliminadas o dependientes de integraciones opcionales.
- Implementar el descubrimiento de archivos, la lectura YAML y la construcción inactiva de candidatos.
- Separar las responsabilidades de loader, validator, registro de hechizos y registro de integraciones.
- Validar IDs, tipos, valores, referencias, ciclos, clases retiradas y dependencias requeridas.
- Conservar archivo y ruta YAML para producir diagnósticos precisos.
- Implementar el snapshot inmutable del registro y la búsqueda O(1) por ID interno.
- Detectar las integraciones disponibles una sola vez durante startup o reload.
- Incorporar pruebas aisladas para el formato YAML, las referencias, los duplicados, los ciclos, las propiedades heredadas, las clases retiradas, las dependencias y la inmutabilidad.
- Mantener el proyecto compilable durante toda la transición.

### 3.3 Trabajo paralelo

Los frentes de build y módulos, API pública, inventario de compatibilidad y pipeline de carga pueden desarrollarse simultáneamente. Todos deberán compartir la misma clasificación de capacidades y propiedades. Esta clasificación deberá acordarse antes de consolidar el loader y el validator para evitar que distintos frentes tomen decisiones incompatibles.

### 3.4 Puerta de salida

El bloque se considerará terminado cuando:

- El proyecto compile para Java 8 contra PandaSpigot 1.8.8.
- Se generen correctamente el artefacto de API y el JAR instalable del plugin.
- Los addons y la compatibilidad multiversión hayan salido del build objetivo.
- El contrato público no exponga clases internas ni permita mutar el registro.
- Un conjunto completo de archivos pueda construirse y validarse sin listeners, tareas, efectos ni mutaciones del runtime.
- Un candidato inválido sea rechazado completamente con un reporte estructurado.
- El registro resultante sea inmutable y permita resolver hechizos por ID en O(1).
- Las pruebas aisladas correspondientes al bloque pasen.

La ejecución completa de hechizos todavía no necesita considerarse terminada en esta etapa.

## 4. Bloque 2 — Motor funcional y runtime controlado

### 4.1 Objetivo

Trasladar al nuevo núcleo únicamente las capacidades válidas, implementar el lanzamiento mediante la API pública y eliminar definitivamente los subsistemas que ya no pertenecen al producto.

### 4.2 Alcance

- Implementar el spell engine y el flujo completo de `SpellCastRequest`.
- Validar hilo, solicitud, ID, caster, mundo, región, modificadores y reactivos antes de aceptar un lanzamiento.
- Implementar la semántica de `SpellCastResult`, incluido el punto exacto en el que se devuelve `ACCEPTED`.
- Implementar los eventos públicos definidos por la API sin exponer tipos internos.
- Mantener operativas las capacidades conservadas: hechizos inmediatos y dirigidos, proyectiles, tracking, multi-spells, `DELAY`, cast times, interrupciones, buffs, debuffs, canalizaciones, efectos, targeting, regiones, variables, modificadores y reactivos admitidos.
- Conservar la compatibilidad requerida con el formato YAML y los nombres relativos de `spell-class`.
- Eliminar mana, progresión, spellbooks, binding, activación interna, hechizos pasivos y cooldowns propios de MagicSpells.
- Eliminar los listeners, comandos, permisos, archivos persistentes y clases cuya única responsabilidad pertenecía a los subsistemas retirados.
- Reconocer las propiedades heredadas sin ejecutarlas, reportándolas mediante diagnósticos agregados y localizables.
- Implementar el runtime tracker por generación para tareas, listeners temporales, cast times, proyectiles, buffs, debuffs, efectos repetitivos y cualquier recurso que necesite limpieza.
- Asociar el estado temporal por jugador mediante UUID y retirarlo en quit, reload y disable.
- Asegurar que el camino normal de lanzamiento solo realice resolución y validaciones en memoria.
- Impedir acceso a disco, mutación de permisos, chat simulado, dispatch de comandos, detección repetida de integraciones o trabajo Bukkit asíncrono durante `cast`.
- Contextualizar las excepciones anteriores y posteriores a `ACCEPTED` sin propagarlas sin control al loop principal.
- Incorporar pruebas aisladas para resultados de la API, construcción y ejecución de clases conservadas, interrupciones, recursos activos y limpieza del runtime.

### 4.3 Trabajo paralelo

Las familias de hechizos conservadas pueden distribuirse entre distintos integrantes mientras otros trabajan en el engine, los eventos, la eliminación de subsistemas y el runtime tracker. Todas las ejecuciones deberán entrar por el mismo engine y registrar sus recursos en el mismo tracker. No se aceptarán mecanismos de limpieza privados o lifecycle paralelo dentro de clases particulares.

Las eliminaciones deberán realizarse usando el inventario acordado en el bloque anterior. Una clase solo se conservará cuando aporte una capacidad incluida en el diseño; una dependencia exclusiva de un subsistema retirado deberá eliminarse junto con él.

### 4.4 Puerta de salida

El bloque se considerará terminado cuando:

- Un plugin confiable pueda consultar y lanzar cualquier hechizo soportado mediante la API.
- Los resultados públicos representen correctamente la aceptación o el motivo de rechazo.
- Los hechizos conservados funcionen a través del nuevo engine.
- No existan rutas públicas de lanzamiento mediante comandos, chat, objetos vinculados o progresión interna.
- Mana, progresión, spellbooks, binding, pasivos y cooldowns internos hayan sido retirados.
- Un cast no cree permisos, no simule chat, no despache comandos y no escriba en disco.
- Toda ejecución activa y cada recurso temporal pertenezcan a una generación identificable y completamente limpiable.
- Las propiedades retiradas se diagnostiquen sin romper por sí solas la carga.
- Las pruebas aisladas correspondientes al bloque pasen.

## 5. Bloque 3 — Operación transaccional, administración e integración final

### 5.1 Objetivo

Convertir el motor funcional en un producto operable, auditable y listo para integrarse con RPGItems y validarse en el servidor real.

### 5.2 Alcance

- Implementar startup, reload y disable sobre el registro inmutable y el runtime por generaciones.
- Publicar `MagicSpellsApi` mediante `ServicesManager` únicamente después de una carga inicial válida y retirarla antes del disable.
- Garantizar que un reload inválido preserve íntegramente el snapshot, el servicio y las ejecuciones de la generación anterior.
- Garantizar que un reload válido cierre la admisión, limpie la generación anterior, intercambie el snapshot, active la nueva generación y reabra la admisión en el hilo principal.
- Implementar exclusivamente el comando raíz `/magicspells`, con alias `/ms`, y sus operaciones `validate`, `reload`, `cast`, `list`, `info` y `status`.
- Aplicar los permisos administrativos definidos y retirar los comandos y permisos públicos anteriores.
- Hacer que el lanzamiento administrativo respete las mismas validaciones normales que la API.
- Completar los diagnósticos de configuración con archivo, ruta, ID, clase, valor, causa, referencias y acción recomendada cuando correspondan.
- Completar el contexto de errores de runtime y limitar o agrupar errores repetitivos.
- Exponer mediante `status` la información operativa definida en el diseño.
- Verificar la política de integraciones opcionales: la ausencia será válida cuando ninguna definición use la integración y rechazará la carga completa cuando sea requerida.
- Documentar para el equipo de RPGItems la dependencia, obtención del servicio, validación de IDs y confirmación del cooldown únicamente tras recibir `ACCEPTED`.
- Completar las pruebas automatizadas aisladas, incluida la compatibilidad con bytecode Java 8 y la separación entre API e implementación.
- Ejecutar la validación manual final en el servidor PandaSpigot 1.8.8 real.
- Revisar individualmente todos los criterios de aceptación establecidos en el documento de diseño.

### 5.3 Trabajo paralelo

Los comandos administrativos, la observabilidad, la documentación de integración y las pruebas finales pueden desarrollarse en paralelo una vez estabilizado el lifecycle transaccional. El equipo responsable del reload deberá coordinar el contrato de admisión y limpieza con quienes implementen comandos y estado operativo; ninguna de esas superficies deberá saltarse el engine o manipular directamente el registro.

La integración con RPGItems se documentará en este proyecto, pero su implementación permanecerá bajo responsabilidad del equipo de RPGItems. Este bloque no autoriza modificaciones a su codebase.

### 5.4 Puerta de salida

El bloque y la actualización MagicSpells 4.0 se considerarán terminados cuando:

- Startup, reload y disable respeten la semántica transaccional definida.
- El servicio público se publique y retire correctamente.
- La superficie de comandos y permisos coincida con el diseño.
- Las dependencias opcionales se validen antes de activar un candidato.
- Los errores y el estado operativo sean suficientes para auditar y depurar el sistema.
- La integración pueda ser consumida por RPGItems mediante el contrato documentado.
- Todas las pruebas aisladas pasen y el bytecode permanezca compatible con Java 8.
- La validación manual confirme el comportamiento esperado en PandaSpigot 1.8.8.
- Se cumplan todos los criterios de aceptación de [`MAGIC-SPELLS-4-DESIGN.md`](./MAGIC-SPELLS-4-DESIGN.md).

No se crearán servidores simulados, smoke-test scripts, harnesses automáticos, entornos Docker ni procedimientos automáticos de arranque salvo solicitud explícita.

## 6. Control de cambios y decisiones

Los descubrimientos realizados durante la implementación podrán justificar cambios respecto de la solución inicialmente propuesta cuando mejoren de manera comprobable la corrección, simplicidad, rendimiento, mantenibilidad o capacidad de diagnóstico.

Antes de aplicar una variación con impacto arquitectónico, contractual o funcional se deberá:

1. Describir el problema encontrado con evidencia del código o del comportamiento real.
2. Explicar la solución propuesta y sus efectos sobre los demás frentes.
3. Comunicar la decisión al equipo responsable.
4. Actualizar el documento de diseño y, cuando corresponda, este plan.
5. Implementar el cambio después de acordar el nuevo contrato.

Las decisiones locales que no alteren contratos compartidos podrán resolverse dentro del frente correspondiente, manteniendo los principios de mínima complejidad, eficiencia, auditabilidad y comportamiento determinista del proyecto.

## 7. Condición general de avance

La cantidad de tareas terminadas no determina el avance entre bloques. La condición real es que la puerta de salida del bloque esté demostrablemente satisfecha mediante código compilable, pruebas aisladas aplicables y revisión de los contratos compartidos.

Si una tarea de un bloque revela una dependencia perteneciente a un bloque anterior, esa dependencia deberá resolverse y estabilizarse antes de continuar. No se implementarán soluciones temporales destinadas a ser reemplazadas inmediatamente en el bloque siguiente.
