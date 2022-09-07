

package ireader.core.api.source

import ireader.core.api.http.HttpClientsInterface
import ireader.core.api.prefs.PreferenceStore

class Dependencies(
    val httpClients: HttpClientsInterface,
    val preferences: PreferenceStore
)
