

package org.ireader.core_api.source

import org.ireader.core_api.http.HttpClients
import org.ireader.core_api.prefs.PreferenceStore

/**
 * TODO(inorichi): maybe this should be a class inside HttpSource since most (all?) extensions
 *   extend from a HttpSource. If more types of sources are introduced, they could have its own
 *   Dependencies object, then the extension should tell the app which kind of source it's
 *   inheriting from in order to provide the correct Dependencies object.
 */
class Dependencies(
    val httpClients: HttpClients,
    val preferences: PreferenceStore
)
