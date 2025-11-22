package repositories

import network.api.SixtApi
import utils.NetworkError
import utils.Result

/**
 * Implementation of CarControlRepository that uses SixtApi for car control operations.
 * This layer can be extended to add local state management or command queuing.
 */
class CarControlRepositoryImpl(
    private val api: SixtApi
) : CarControlRepository {
    
    override suspend fun lockCar(): Result<Unit, NetworkError> {
        return api.lockCar()
    }
    
    override suspend fun unlockCar(): Result<Unit, NetworkError> {
        return api.unlockCar()
    }
    
    override suspend fun blinkCar(): Result<Unit, NetworkError> {
        return api.blinkCar()
    }
}
