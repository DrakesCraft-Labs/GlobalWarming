<p align="center"><img src="https://raw.githubusercontent.com/DrakesCraft-Labs/GlobalWarming/main/banner.svg" alt="GlobalWarming" width="100%"></p>

# GlobalWarming

> ### 🏰 ¡Únete a la Comunidad Oficial de DrakesCraft!
> 
> * 🎮 **IP del Servidor**: `play.drakescraft.net` *(Java 1.21.11 & Bedrock)*
> * 💬 **Discord Oficial**: [discord.gg/drakescraft](https://discord.gg/rR7FbfCt9Y)
> * 🌐 **Web & Guía**: [drakescraft.net](https://drakescraft.net) — 🛒 **Tienda**: [tienda.drakescraft.net](https://tienda.drakescraft.net)
> 
> *¡Juega con este addon y más de 80 expansiones optimizadas en vivo en nuestra network de supervivencia técnica!*

---

Contaminación y temperatura para Slimefun, adaptado al ecosistema de **DrakesCraft**
(Paper/Purpur 1.21.11, Java 21).

## Qué hace

Las máquinas contaminan. La contaminación sube la temperatura del mundo, y la temperatura depende
además del bioma y de la hora. El jugador lo consulta con un **Termómetro** —clic derecho para
cambiar entre Celsius, Fahrenheit y Kelvin— y con el **Medidor de Calidad del Aire**.

Para bajarla hay un **Compresor de Aire**, que llena Bombonas de CO₂ y saca la contaminación de
circulación, más Filtros y los materiales que hacen falta: Mercurio y Cinabrio.

## ⚠️ Los efectos que hacen daño vienen apagados

**A propósito.** Los valores del autor no lo estaban, y son bastante más agresivos de lo que se
espera al instalar un plugin de contaminación:

| Mecanismo | El autor | Aquí |
|---|---|---|
| Incendios forestales | activo, **10 bloques por segundo** por encima de 40 °C | apagado |
| Derretir hielo | activo | apagado |
| Prender fuego al jugador | activo, **80 %** de probabilidad | apagado |
| Ralentizar al jugador | activo, **80 %** de probabilidad | apagado |

En un servidor con construcciones, unos incendios a diez bloques por segundo arrasan terreno
ajeno. Lo que sí funciona de serie es todo el seguimiento: las máquinas contaminan, la temperatura
sube y se puede medir. Sube los mecanismos de uno en uno cuando quieras, y avisa antes.

## Qué cambiamos

Este repositorio **no es un fork**: es el código original integrado en el ecosistema de
DrakesCraft.

**Un fallo que solo aparecía al arrancar.** El addon guardaba las temperaturas por bioma en un
`EnumMap<Biome, …>`, y **`Biome` dejó de ser un enum en 1.21**: ahora es una interfaz sobre un
registro. Compilado contra una API vieja el código pasa, pero al arrancar la JVM lo rechaza con
`VerifyError: Bad type on operand stack`. Se cambió a `HashMap` y se compila contra `paper-api`
1.21.11, la misma versión que corre en producción, para que estas cosas salten en el build y no
en el servidor.

**Un mensaje que nunca funcionó.** En consola salía el literal `" + path + "` porque el upstream
tenía las comillas escapadas y la concatenación muerta. Ahora usa marcadores de posición.

**Fuera el autoactualizador**, que se descargaba el jar de un repositorio ajeno y se reemplazaba
solo al arrancar. Era además el único uso que hacía de GuizhanLib, así que desapareció también esa
dependencia.

**Todo en español**, incluidos los mensajes de consola.

## Nota sobre Terralith

DrakesCraft usa Terralith, que añade más de cien biomas. GlobalWarming no los tiene en sus mapas
de temperatura y les asigna el valor por defecto (15 °C). Se ve como un aviso al arrancar. No
rompe nada, pero si algún día se encienden los mecanismos conviene rellenar esos biomas antes.

## Instalación

Necesita Slimefun de DrakesCraft (`Slimefun4-Drake`). Se pone el jar en `plugins/` y listo. Sin
tocar la configuración, no cambia nada del juego más allá de poder medir.

## Crédito

El trabajo de fondo es de **poma123**. Licencia **MIT**, conservada sin modificar. Los detalles
están en [UPSTREAM.md](UPSTREAM.md).
