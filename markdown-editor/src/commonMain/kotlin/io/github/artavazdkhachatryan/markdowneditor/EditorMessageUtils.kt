package io.github.artavazdkhachatryan.markdowneditor

/**
 * Builds a JS call that passes a JSON message to MilkdownEditor.handleMessage.
 *
 * Two escaping levels are required:
 *  1. JSON string escaping  — \ → \\, " → \", newlines → \n etc.
 *  2. JS single-quoted string literal — each \ from step 1 doubled again, ' → \'
 *
 * Without level 2, the JS engine consumes the level-1 backslashes before JSON.parse
 * ever sees them, causing JSON.parse to fail on payloads with backslashes (e.g. LaTeX).
 */
internal fun buildEditorJsCall(type: String, payload: String): String {
    // Step 1: escape payload as a JSON string value
    val jsonEscaped = payload
        .replace("\\", "\\\\")   // \ → \\   (must be first)
        .replace("\"", "\\\"")   // " → \"
        .replace("\n", "\\n")    // newline → \n
        .replace("\r", "\\r")    // CR → \r

    val json = """{"type":"$type","payload":"$jsonEscaped"}"""

    // Step 2: escape the JSON for embedding in a JS single-quoted string literal
    val jsLiteral = json
        .replace("\\", "\\\\")   // double every backslash again
        .replace("'", "\\'")     // escape single quotes

    return "MilkdownEditor.handleMessage('$jsLiteral')"
}
