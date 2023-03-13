package ireader.data.di

import com.squareup.sqldelight.db.SqlDriver
import ireader.core.db.Transactions
import ireader.data.core.DatabaseDriverFactory
import ireader.data.core.DatabaseHandler
import ireader.data.core.DatabaseTransactions
import ireader.data.core.JvmDatabaseHandler
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

actual val dataPlatformModule: DI.Module = DI.Module("desktopDataModule") {
    bindSingleton<DatabaseHandler> { JvmDatabaseHandler(instance(),instance()) }
    bindSingleton<SqlDriver> { DatabaseDriverFactory().create() }
    bindSingleton<Transactions> { DatabaseTransactions(instance()) }

}