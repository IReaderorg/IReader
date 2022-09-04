

package org.ireader.core_api.source

import org.ireader.core_api.http.HttpClientsInterface
import org.ireader.core_api.prefs.PreferenceStore

class Dependencies(
    val httpClients: HttpClientsInterface,
    val preferences: PreferenceStore
)
