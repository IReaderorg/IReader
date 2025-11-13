package ireader.domain.utils.mappers

/**
 * Base interface for mapping between domain and data layer entities
 * 
 * @param Domain The domain model type
 * @param Data The data layer entity type
 */
interface EntityMapper<Domain, Data> {
    /**
     * Convert data layer entity to domain model
     */
    fun toDomain(data: Data): Domain
    
    /**
     * Convert domain model to data layer entity
     */
    fun toData(domain: Domain): Data
}

/**
 * Base interface for one-way mapping from data to domain
 */
interface DataToDomainMapper<Domain, Data> {
    fun toDomain(data: Data): Domain
}

/**
 * Extension function for mapping lists
 */
fun <Domain, Data> DataToDomainMapper<Domain, Data>.toDomainList(dataList: List<Data>): List<Domain> {
    return dataList.map { toDomain(it) }
}

/**
 * Extension function for mapping lists bidirectionally
 */
fun <Domain, Data> EntityMapper<Domain, Data>.toDomainList(dataList: List<Data>): List<Domain> {
    return dataList.map { toDomain(it) }
}

fun <Domain, Data> EntityMapper<Domain, Data>.toDataList(domainList: List<Domain>): List<Data> {
    return domainList.map { toData(it) }
}
