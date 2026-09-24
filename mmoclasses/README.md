# MMOClasses

Plugin de clases RPG para Paper, diseñado para funcionar junto a **MMOWeaponary**.
Trae 4 clases (Mago, Arquero, Guerrero y Clérigo), cada una con 4 habilidades y
una pasiva, además de stats, niveles, atributos, recursos (maná/energía/furia),
un HUD y menús.

> El diseño completo (clases, habilidades, números e ideas para ampliar) está en
> [DISENO.md](DISENO.md).

## Requisitos

- Paper **1.21.4** o superior (usa `Attribute.MAX_HEALTH`, que existe desde la 1.21.3).
- Java 21.
- MMOWeaponary es opcional (`softdepend`). Sin él, el plugin usa armas vanilla.

## Compilar

```sh
cd mmoclasses
mvn package
# -> target/MMOClasses-0.1.0.jar
```

Copia el jar a `plugins/` y reinicia el servidor. La primera vez se genera
`plugins/MMOClasses/config.yml`.

## Cómo se juega

1. `/clase` abre el menú y eliges una clase.
2. Matas monstruos para ganar XP. Cada clase guarda su propio nivel, así que
   puedes cambiar de clase sin perder progreso.
3. Cada nivel da **3 puntos de atributo**. Los repartes en `/clase stats`
   (click: +1, shift+click: +5).
4. Con el arma de tu clase en la mano, pulsa **F** para entrar en el
   **modo habilidades**. Las teclas **1-4** lanzan las habilidades y F sale del modo.
   Lleva el arma en las ranuras 5-9 para que las teclas 1-4 queden libres.
5. En la action bar ves la vida, el recurso y el nivel. En el modo habilidades
   también ves los enfriamientos.

## Comandos

| Comando | Qué hace |
|---|---|
| `/clase` | Menú de clase, o de personaje si ya tienes clase |
| `/clase elegir <clase>` | Elegir o cambiar de clase (`mago`, `arquero`, `guerrero`, `clerigo`) |
| `/clase info` | Nivel, XP, atributos y stats en el chat |
| `/clase stats` | Menú para repartir puntos |
| `/clase asignar <atributo> [cantidad]` | Repartir puntos desde el chat |
| `/clase habilidades` | Lista de habilidades con coste y enfriamiento |
| `/clase lanzar <1-4>` | Lanzar una habilidad (sirve para macros o items) |
| `/clase modo` | Activar o desactivar el modo habilidades |
| `/clase admin setnivel <jugador> <nivel> [clase]` | Fijar el nivel |
| `/clase admin darxp <jugador> <xp>` | Dar XP |
| `/clase admin reset <jugador>` | Borrar todo el progreso |
| `/clase admin recargar` | Recargar `config.yml` |

Alias: `/clases`, `/class`.

| Permiso | Por defecto | |
|---|---|---|
| `mmoclasses.use` | todos | Elegir clase y usar habilidades |
| `mmoclasses.cambiar` | todos | Cambiar de clase una vez elegida |
| `mmoclasses.admin` | op | Comandos de admin |

## Configuración

Todo el balance está en `config.yml` y se recarga con `/clase admin recargar`:

- `clases.<clase>`: armas permitidas, stats base y crecimiento por nivel.
- `atributos`: lo que da cada punto de Fuerza, Destreza, Inteligencia, Vitalidad y Espíritu.
- `habilidades.<id>`: nivel de desbloqueo, coste, enfriamiento, daño, radio, duración...
- `pasivas`: números de la pasiva de cada clase.
- `experiencia`: nivel máximo, curva de XP, puntos por nivel y XP de cada mob.
- `recursos.furia`: cuánta furia se gana y cuánto decae.
- `mensajes`: todos los textos, en formato [MiniMessage](https://docs.advntr.dev/minimessage/format.html).

Los datos de cada jugador se guardan en `plugins/MMOClasses/jugadores/<uuid>.yml`.

## Integración con MMOWeaponary

MMOClasses tiene que saber de cada arma **qué tipo es** (BACULO, ARCO, ESPADA...),
**qué stats da** y si pide una **clase o nivel**. Lo busca en este orden:

### Opción A (recomendada): registrar un `WeaponProvider`

En MMOWeaponary añade MMOClasses como dependencia `provided` (no la metas dentro
de tu jar) y `softdepend: [MMOClasses]` en su `plugin.yml`. Después, en `onEnable`:

```java
if (getServer().getPluginManager().isPluginEnabled("MMOClasses")) {
    MMOClassesAPI.registerWeaponProvider(this, item -> {
        MiArma arma = gestorDeArmas.leer(item);   // tu propio código
        if (arma == null) {
            return null;                          // no es un arma de MMOWeaponary
        }
        return new WeaponInfo(
                arma.getTipo(),                   // "BACULO", "ARCO", "ESPADA"...
                Map.of(Stat.PODER_MAGICO, arma.getPoderMagico(),
                       Stat.CRITICO_PROB, arma.getCritico()),
                null,                             // o PlayerClass.MAGO si es exclusiva
                arma.getNivelRequerido());
    });
}
```

### Opción B: claves en el PersistentDataContainer

Si MMOWeaponary ya guarda los datos en el PDC del item, basta con que las claves
coincidan con `mmoweaponary.pdc` en `config.yml`. Por defecto:

| Clave | Tipo | Ejemplo |
|---|---|---|
| `mmoweaponary:tipo` | STRING | `BACULO` |
| `mmoweaponary:clase` | STRING (opcional) | `mago` |
| `mmoweaponary:nivel` | INTEGER o DOUBLE (opcional) | `10` |
| `mmoweaponary:<stat>` | DOUBLE, INTEGER, FLOAT, LONG o STRING | `mmoweaponary:poder_magico = 15` |

```java
// Dentro de MMOWeaponary (namespace = nombre del plugin en minúsculas)
PersistentDataContainer pdc = meta.getPersistentDataContainer();
pdc.set(new NamespacedKey(this, "tipo"), PersistentDataType.STRING, "BACULO");
pdc.set(new NamespacedKey(this, "poder_magico"), PersistentDataType.DOUBLE, 15.0);
pdc.set(new NamespacedKey(this, "nivel"), PersistentDataType.INTEGER, 10);
```

### Opción C: materiales vanilla

Para probar sin MMOWeaponary: `BLAZE_ROD` = báculo, `BOW` = arco, espadas,
hachas, `GOLDEN_HOE` = cetro, `BOOK` = tomo, etc. (`mmoweaponary.vanilla` en
`config.yml`; desactívalo en producción).

### Stats que puede dar un arma

`vida_maxima`, `recurso_maximo`, `regen_recurso`, `dano_fisico`, `dano_distancia`,
`poder_magico`, `poder_curacion`, `critico_prob`, `critico_dano`, `defensa`,
`robo_vida`, `reduccion_enfriamiento`.

Las stats del arma solo cuentan si la llevas en la mano principal y es de tu clase
y nivel. Con un arma de otra clase pegas a la mitad de daño
(`general.multiplicador-arma-incorrecta`) y no puedes lanzar habilidades.

### API y eventos

`MMOClassesAPI`: `getPlayerClass`, `getLevel`, `getStat`, `getStats`,
`getResource`, `setResource`, `giveExperience`, `canUseWeapon`, `castAbility`.

Eventos:

- `ClassChangeEvent` (cancelable): antes de elegir o cambiar de clase.
- `ClassLevelUpEvent`: después de subir de nivel.
- `AbilityCastEvent` (cancelable, se puede cambiar el coste): justo antes de lanzar
  una habilidad. Sirve, por ejemplo, para armas legendarias que abaratan un hechizo.

## Estructura del código

```
com.kubson.mmoclasses
├── MMOClasses.java            plugin principal
├── Settings.java             config.yml ya leída
├── api/                      API pública: clases, stats, WeaponProvider, eventos
├── ability/                  framework de habilidades + impl/<clase>/ (16 habilidades)
├── combat/                   daño, curación, aliados/enemigos
├── stats/                    cálculo de stats y vida máxima
├── data/                     datos del jugador, guardado, XP y niveles
├── weapon/                   detección de armas (MMOWeaponary / PDC / vanilla)
├── listener/                 combate, modo habilidades, menús, jugador
├── hud/                      action bar y regeneración
├── gui/                      menús de clase y de personaje
└── command/                  /clase
```

Para añadir una habilidad: crea una clase que extienda `Ability`, regístrala en
`AbilityManager`, añade su id a la clase en `PlayerClass` y sus números en
`config.yml`.
