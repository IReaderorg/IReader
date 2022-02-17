package org.ireader.domain.source

import org.ireader.core.okhttp.HttpClients
import org.ireader.core.prefs.PreferenceStore

class Dependencies(
  val httpClients: HttpClients,
  val preferences: PreferenceStore,
)