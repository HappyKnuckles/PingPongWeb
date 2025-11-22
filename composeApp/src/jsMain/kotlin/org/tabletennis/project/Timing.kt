package org.tabletennis.project
package org.tabletennis.project

/**
 * JavaScript-Implementierung der Zeitfunktion, verwendet Date.now()
 */
actual fun currentTimeMillis(): Long {
    return kotlin.js.Date.now().toLong()
}
/**
 * JS-Implementierung der currentTimeMillis-Funktion
 */
actual fun currentTimeMillis(): Long {
    return kotlin.js.Date.now().toLong()
}
