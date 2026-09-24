# Diseño de clases: MMOClases × MMOWeaponary

## 1. Por qué estas 4 clases

Pediste empezar con **Mago** y **Arquero**. Las dos hacen daño a distancia, así
que para un servidor con mazmorras y jefes faltan dos roles: alguien que **aguante
y atraiga a los monstruos** y alguien que **cure**. Por eso las otras dos son
**Guerrero** y **Clérigo**, la "trinidad" clásica de los MMO:

| Clase | Rol | Recurso | Armas (MMOWeaponary) | Atributos clave | Dificultad |
|---|---|---|---|---|---|
| 🔮 **Mago** | Daño mágico en área y control | Maná | Báculo, Varita | Inteligencia, Espíritu | Media |
| 🏹 **Arquero** | Daño físico a distancia y movilidad | Energía | Arco, Ballesta | Destreza, Vitalidad | Media |
| ⚔️ **Guerrero** | Tanque cuerpo a cuerpo | Furia | Espada, Hacha, Maza | Fuerza, Vitalidad | Fácil |
| ✨ **Clérigo** | Sanador y daño sagrado | Maná | Cetro, Tomo | Espíritu, Inteligencia | Media |

Cada clase se juega distinto gracias a su recurso:

- **Maná**: se regenera solo. El mago y el clérigo planifican cuándo gastar.
- **Energía**: poca cantidad pero se regenera muy rápido. El arquero dispara sin parar.
- **Furia**: empieza en 0. Se gana golpeando (+8) y recibiendo daño (+4) y se pierde
  fuera de combate. El guerrero tiene que meterse en la pelea para usar habilidades.

## 2. Sistema común

### Niveles
- Nivel máximo **50** (configurable). Cada clase guarda su propio nivel.
- XP por matar monstruos (zombi 10, enderman 25, wither 1500...). Los animales no dan XP.
- XP para subir = `50 × nivel^1.3` (nivel 1→2: 50 XP, 10→11: 998 XP, 20→21: 2456 XP).
- Cada nivel da **+3 puntos de atributo**, y al subir se recuperan la vida y el recurso.
- Las habilidades se desbloquean en los niveles **1, 5, 10 y 20** (la 4ª es la **definitiva**).

### Atributos (el jugador reparte los puntos)

| Atributo | Cada punto da |
|---|---|
| Fuerza | +1.5% daño físico |
| Destreza | +1.5% daño a distancia, +0.2% prob. de crítico |
| Inteligencia | +1.5% poder mágico, +2 de recurso máximo |
| Vitalidad | +1 vida máxima, +1 defensa |
| Espíritu | +1.5% poder de curación, +0.1 regeneración/s |

### Stats derivadas
Las stats salen de: **base de la clase + crecimiento por nivel + atributos + arma de MMOWeaponary**.

| Stat | Efecto |
|---|---|
| Vida máxima | Vida extra sobre los 20 de vanilla (los corazones en pantalla siguen siendo 10) |
| Recurso máximo / Regeneración | Tamaño y regeneración del maná/energía/furia |
| Daño físico % | Golpes cuerpo a cuerpo y habilidades del guerrero |
| Daño a distancia % | Flechas y habilidades del arquero |
| Poder mágico % | Habilidades del mago y daño del clérigo |
| Poder de curación % | Curas y escudos del clérigo |
| Prob. de crítico / Daño crítico | Crítico en golpes y habilidades (base ×1.5) |
| Defensa | Reducción = defensa / (defensa + 60). 20 de defensa = 25% menos daño |
| Robo de vida % | Curación al golpear cuerpo a cuerpo |
| Reducción de enfriamiento % | Máximo 40% |

Todas las habilidades usan la misma fórmula:
`daño = daño-base × (1 + stat% / 100) × crítico`.

### Controles
- **F** con el arma de la clase activa el **modo habilidades**. **1-4** lanzan las habilidades
  y **F** sale del modo. Se puede cambiar a **Shift+F** para que F siga sirviendo para el escudo.
- `/clase lanzar <1-4>` para macros, NPCs o items.

## 3. Las clases en detalle

### 🔮 Mago
*El que controla el campo de batalla: mucho daño en área pero poca vida.*

**Pasiva: Sobrecarga Arcana.** Cada 4º hechizo no cuesta maná.

| # | Habilidad | Nivel | Coste | Enfr. | Efecto |
|---|---|---|---|---|---|
| 1 | **Bola de Fuego** | 1 | 20 | 4s | Proyectil que explota al impactar: 8 de daño en radio 3 y quemadura 3s |
| 2 | **Parpadeo** | 5 | 25 | 10s | Teletransporte de 8 bloques hacia donde miras. No atraviesa paredes |
| 3 | **Nova de Escarcha** | 10 | 35 | 12s | 6 de daño en radio 5 alrededor y Lentitud III 3s. Sirve para escapar |
| 4 | **Meteoro** ⭐ | 20 | 60 | 45s | Tras 1.5s de aviso cae un meteoro: 20 de daño en radio 5, empuje y quemadura 5s |

*Estilo de juego:* Nova para congelar al grupo, Parpadeo para alejarte y Bola de Fuego desde lejos.
El Meteoro se lanza sobre enemigos lentos o atrapados.

### 🏹 Arquero
*Movilidad y daño sostenido. Castiga desde lejos.*

**Pasiva: Ojo de Halcón.** +1.5% de daño con flechas por cada bloque de distancia (máx. +45%).

| # | Habilidad | Nivel | Coste | Enfr. | Efecto |
|---|---|---|---|---|---|
| 1 | **Disparo Triple** | 1 | 25 | 3s | 3 flechas en abanico, 5 de daño cada una |
| 2 | **Salto Evasivo** | 5 | 30 | 8s | Salto hacia atrás, Velocidad II 3s y sin daño de caída |
| 3 | **Flecha Explosiva** | 10 | 40 | 10s | La flecha explota al impactar: 10 de daño en radio 4 y empuje (no rompe bloques) |
| 4 | **Lluvia de Flechas** ⭐ | 20 | 60 | 40s | Durante 3s caen flechas sobre la zona que apuntas (radio 4) |

*Estilo de juego:* abre distancia con Salto Evasivo para aprovechar Ojo de Halcón. La Lluvia
de Flechas sirve para bloquear pasillos y zonas de jefe.

### ⚔️ Guerrero
*El muro del grupo: atrae a los monstruos y aguanta.*

**Pasiva: Voluntad de Hierro.** Con menos del 35% de vida recibe un 25% menos de daño.
Además tiene 2% de robo de vida base y es la clase con más vida y defensa.

| # | Habilidad | Nivel | Coste | Enfr. | Efecto |
|---|---|---|---|---|---|
| 1 | **Tajo Giratorio** | 1 | 20 furia | 5s | Golpe circular: 8 de daño en radio 3.5 y empuje |
| 2 | **Carga** | 5 | 15 furia | 10s | Embestida: daña y aturde 1.5s a los enemigos que atropella |
| 3 | **Grito de Guerra** | 10 | 25 furia | 20s | Provoca a los monstruos en radio 10. Resistencia I + Absorción II 6s y Fuerza I a los aliados |
| 4 | **Golpe del Titán** ⭐ | 20 | 50 furia | 35s | Salto y golpe al suelo: 18 de daño en radio 6, lanza por los aires y ralentiza |

*Estilo de juego:* Carga para empezar la pelea, Grito para quedarte con los monstruos, Tajo
para generar y gastar furia, y Golpe del Titán para agrupar y aturdir.

### ✨ Clérigo
*Mantiene vivo al grupo y castiga a los no-muertos.*

**Pasiva: Luz Purificadora.** +50% de daño sagrado contra no-muertos (zombis, esqueletos, fantasmas, wither...).

| # | Habilidad | Nivel | Coste | Enfr. | Efecto |
|---|---|---|---|---|---|
| 1 | **Luz Sagrada** | 1 | 15 | 3s | Rayo: cura 5 al aliado o hace 7 de daño sagrado al enemigo. Sin objetivo, te cura a ti a la mitad |
| 2 | **Círculo de Sanación** | 5 | 35 | 15s | Zona de radio 5 durante 5s que cura 2.5 por segundo a los aliados |
| 3 | **Escudo Divino** | 10 | 30 | 18s | Absorción (más fuerte con más curación) y Resistencia a ti y a los aliados en radio 8 |
| 4 | **Juicio Celestial** ⭐ | 20 | 70 | 50s | Rayos sobre hasta 6 enemigos cercanos (16 de daño sagrado) y cura 8 a los aliados |

*Estilo de juego:* en solitario, Luz Sagrada hace daño y te cura. En grupo, pon el Círculo
debajo del guerrero y guarda el Escudo para el golpe fuerte del jefe.

## 4. Cómo encaja con MMOWeaponary

MMOWeaponary define el **arma** y MMOClases define **quién puede usarla y cómo escala**:

1. **Tipo de arma → clase.** Cada arma tiene un tipo (BACULO, ARCO, ESPADA...) y cada clase
   una lista de tipos permitidos (`clases.<clase>.armas`).
2. **Stats del arma → stats del jugador.** Un báculo con `poder_magico: 15` sube un 15% todo el
   daño del mago. Una espada con `robo_vida: 3` da robo de vida al guerrero.
3. **Requisitos.** Un arma puede pedir una clase concreta (`clase: mago`) y un nivel mínimo.
   Si no se cumplen: sin stats del arma, sin habilidades y la mitad de daño.
4. **Eventos.** Con `AbilityCastEvent` MMOWeaponary puede hacer armas legendarias que cambian
   habilidades, por ejemplo "Báculo del Fénix: Bola de Fuego cuesta 0 y deja fuego en el suelo".

Hay tres formas de conectarlos (API, claves PDC o materiales vanilla); están explicadas en el
[README](README.md#integración-con-mmoweaponary).

### Ideas de armas para MMOWeaponary por clase

| Clase | Común | Rara | Épica | Legendaria (efecto único) |
|---|---|---|---|---|
| Mago | Varita de aprendiz | Báculo de roble arcano | Báculo de la tormenta | **Báculo del Fénix**: la Bola de Fuego deja el suelo en llamas |
| Arquero | Arco corto | Arco largo élfico | Ballesta de repetición | **Susurro del Viento**: el Disparo Triple dispara 5 flechas |
| Guerrero | Espada de hierro | Hacha de guerra | Maza del juramento | **Rompemontañas**: el Golpe del Titán deja una grieta que ralentiza |
| Clérigo | Tomo de oraciones | Cetro bendito | Relicario solar | **Cáliz del Alba**: el Círculo de Sanación también limpia efectos negativos |

## 5. Ideas para ampliar (hoja de ruta)

**Fase 2: profundidad**
- **Especializaciones al nivel 30** (una por clase a elegir):
  - Mago → *Piromante* (quemaduras), *Criomante* (control y escudos de hielo), *Arcanista* (teletransportes y daño puro).
  - Arquero → *Francotirador* (disparo cargado), *Cazador* (mascota halcón o lobo y trampas), *Explorador* (veneno y movilidad).
  - Guerrero → *Paladín* (tanque sagrado con auras), *Berserker* (daño y robo de vida), *Guardián* (escudo y bloqueo).
  - Clérigo → *Sacerdote* (curación pura), *Inquisidor* (daño sagrado), *Oráculo* (buffs y escudos).
- **Árbol de talentos**: 1 punto cada 5 niveles para modificar habilidades
  (Parpadeo deja una Nova, Carga no gasta furia si mata...).
- **Sistema de grupo** (`/grupo`): XP compartida, curas y buffs solo al grupo y sin fuego amigo.
- **PlaceholderAPI**: `%mmoclases_clase%`, `%mmoclases_nivel%`, `%mmoclases_recurso%` para scoreboard y tab.

**Fase 3: contenido**
- **Mobs con nivel** (integración con MythicMobs) para que el nivel 50 tenga rivales.
- **Mazmorras por roles** con jefes que obligan a usar tanque, sanador y daño.
- **Armaduras por clase** (tela, cuero, malla, placas) y **sets** con bonus de clase.
- **Gemas o runas** en las armas de MMOWeaponary que cambian habilidades.
- **Misiones de clase**: la definitiva se desbloquea con una misión en vez de solo por nivel.
- **Prestigio** al nivel 50: reinicias con una bonificación permanente y un título.

**Fase 4: servidor grande**
- Guardado en **MySQL** para redes Velocity/BungeeCord.
- **Balance PvP separado** (multiplicadores de daño y curación en PvP).
- **Rankings** por clase, **arenas** y **temporadas**.
- **Cosméticos**: partículas alternativas para las habilidades y títulos.
- **Sinergias entre clases**: Nova de Escarcha + Golpe del Titán = "Hielo Roto" (+50% de daño),
  Grito de Guerra + Lluvia de Flechas, etc.

## 6. Clases para el futuro (5ª a 8ª)

| Clase | Rol | Arma | Recurso | Habilidades (1 · 2 · 3 · definitiva) |
|---|---|---|---|---|
| 🗡️ **Asesino** | Daño explosivo cuerpo a cuerpo | Dagas | Energía | Puñalada Trasera · Sigilo · Veneno · Danza de Sombras |
| 💀 **Nigromante** | Invocador y drenaje | Guadaña, bastón oscuro | Almas | Invocar Esqueletos · Drenar Vida · Maldición · Ejército de los Muertos |
| 🌿 **Druida** | Híbrido sanador/tanque | Báculo natural | Maná | Enredaderas · Rejuvenecer · Forma de Oso · Tormenta Salvaje |
| 🎵 **Bardo** | Soporte con buffs | Laúd, cuerno | Inspiración | Canción de Velocidad · Himno de Curación · Disonancia · Sinfonía Heroica |

Con la estructura del plugin, añadir una clase consiste en: una constante nueva en
`PlayerClass`, sus 4 habilidades en `ability/impl/<clase>/`, su registro en
`AbilityManager` y su sección en `config.yml`.
